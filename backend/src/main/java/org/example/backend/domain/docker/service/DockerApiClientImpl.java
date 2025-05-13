package org.example.backend.domain.docker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.cache.DockerTokenCacheManager;
import org.example.backend.common.util.DockerUriBuilder;
import org.example.backend.controller.request.docker.DockerContainerLogRequest;
import org.example.backend.controller.response.docker.DemonContainerStateCountResponse;
import org.example.backend.controller.response.docker.ImageResponse;
import org.example.backend.domain.docker.dto.*;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class DockerApiClientImpl implements DockerApiClient {

    private final WebClient dockerHubWebClient;
    private final WebClient dockerWebClient;
    private final WebClient dockerRegistryWebClient;
    private final DockerUriBuilder uriBuilder;
    private final ObjectMapper objectMapper;
    private final DockerTokenCacheManager tokenCache;
    private static final HttpClient REDIRECTING_HTTP_CLIENT = HttpClient.create().followRedirect(true);
    private final WebClient.Builder webClientBuilder;

    @Override
    public ImageResponse getImages(String query, int page, int pageSize) {
        URI uri = uriBuilder.buildSearchRepositoriesUri(query, page, pageSize);
        log.debug(">> Docker Hub 이미지 검색 URI: {}", uri);

        return fetchMono(dockerHubWebClient, uri, ImageResponse.class, ErrorCode.DOCKER_SEARCH_API_FAILED);
    }

    @Override
    public DockerTag getTags(String namespace, String repo, int page, int pageSize) {
        URI uri = uriBuilder.buildHubTagsUri(namespace, repo, page, pageSize);
        log.debug(">> Docker Hub 태그 조회 URI: {}", uri);

        return fetchMono(dockerHubWebClient, uri, DockerTag.class, ErrorCode.DOCKER_TAGS_API_FAILED);
    }

    @Override
    public DemonContainerStateCountResponse getInfo() {
        URI uri = uriBuilder.buildInfoUri();
        log.debug(">> Docker 데몬 정보 조회 URI: {}", uri);

        return fetchMono(dockerWebClient, uri, DemonContainerStateCountResponse.class, ErrorCode.DOCKER_HEALTH_API_FAILED);
    }

    @Override
    public List<ContainerDto> getContainersByStatus(List<String> statuses) {
        URI uri = uriBuilder.buildContainersByStatusUri(statuses);
        log.debug(">> 상태별 컨테이너 조회 URI: {}", uri);

        return fetchFlux(dockerWebClient, uri, ContainerDto.class, ErrorCode.DOCKER_HEALTH_API_FAILED);
    }

    @Override
    public List<ContainerDto> getContainersByName(String serverIp, String nameFilter) {
        // 1) Build the absolute base URL once
        String engineBaseUrl = "http://" + serverIp + ":3789";

        // 2) Ask your builder for the perfectly encoded, absolute URI
        URI uri = uriBuilder.buildContainersByNameUri(engineBaseUrl, nameFilter);
        log.debug(">> 이름 기반 컨테이너 조회 URI: {}", uri);

        // 3) Use a vanilla WebClient and hand it the URI directly
        WebClient client = webClientBuilder
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        return client.get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(ContainerDto.class)
                .collectList()
                .block();
    }

    @Override
    public List<String> getContainerLogs(
            String serverIp,
            String containerId,
            DockerContainerLogRequest filter
    ) {
        WebClient client = webClientBuilder
                .baseUrl("http://" + serverIp + ":3789")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();

        return client.get()
                .uri(b -> {
                    b.path("/containers/{id}/logs")
                            .queryParam("stdout", true)
                            .queryParam("stderr", true)
                            .queryParam("timestamps", true);
                    if (filter.sinceSeconds() != null) {
                        b.queryParam("since", filter.sinceSeconds());
                    }
                    if (filter.untilSeconds() != null) {
                        b.queryParam("until", filter.untilSeconds());
                    }
                    return b.build(containerId);
                })
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .flatMap(db -> demultiplex(db.asByteBuffer()))
                .collectList()
                .block();
    }

    /**
     * 디폴트 포트 처리 로직
     * <br>
     *  - namespace, imageName, tag에 해당하는 도커 이미지의 디폴트 포트 목록 가져옴
     *
     *  <br><br>
     *  - (인증 -> 매니페스트(이미지_컨테이너 의 메타데이터) 조회 -> digest(컨테이너 메타데이터에 있는 id값) 결정 -> blob 조회 -> 포트 추출)
     *
     * @param namespace
     * @param imageName
     * @param tag
     * @return : imageBlob
     */
    @Override
    public List<String> getImageDefaultPorts(String namespace, String imageName, String tag) {
        try {
            WebClient dockerAuthClient = makeDockerAuthClient(namespace, imageName);
            ImageMetaData imageMetaData = fetchImageMetaData(dockerAuthClient, namespace, imageName, tag);
            String blobHashId = fetchImageBlobHashId(dockerAuthClient, namespace, imageName, imageMetaData);
            BlobMetaData blobMetaData = fetchBlobMetaData(dockerAuthClient, namespace, imageName, blobHashId);
            return extractPorts(blobMetaData);
        } catch (Exception ex) {
            log.warn("Registry 접근/인증 실패 ({}), 빈 리스트 반환합니다.", ex.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * - 레지스트리 인증 api를 먼저 호출해서 Bearer 토큰을 얻고, 그 토큰을 헤더에 담아 이미지를 조회할 WebClient를 구성 <br>
     * - 토큰이 있어야 레지스트리 api 사용 가능하기 때문.  <br>
     * - (우리는 공식 이미지만 지원할 것이므로 사용자가 계정 없어도 접근 가능함)  <br>
     * - 도커에서는 익명 자격으로 pull 권한만 가진 짧은 수명의 토큰을 발급해 주는데, 이걸로 공식 이미지 정보 가져올 수 있음.
     *
     * @param namespace
     * @param image
     * @return
     */
    private WebClient makeDockerAuthClient(String namespace, String image) {

        // 1) 레디스 캐싱으로 토큰 관리.
        String dockerAuthToken = tokenCache.getAnonymousToken(namespace, image);

        // 2) webClient 만들기
        return dockerRegistryWebClient.mutate()
                .clientConnector(new ReactorClientHttpConnector(REDIRECTING_HTTP_CLIENT))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + dockerAuthToken)
                .build();
    }

    /**
     * - 인증된 WebClient 를 사용해 특정 이미지·태그의 매니페스트(JSON_메타데이터)를 조회함.
     *
     * @param dockerAuthClient
     * @param namespace
     * @param imageName
     * @param tag
     * @return
     */
    private ImageMetaData fetchImageMetaData(WebClient dockerAuthClient, String namespace, String imageName, String tag) {
        return dockerAuthClient.get()
                .uri(uriBuilder.buildRegistryManifestUri(namespace, imageName, tag))
                .header(HttpHeaders.ACCEPT,
                        "application/vnd.docker.distribution.manifest.list.v2+json," +
                                "application/vnd.docker.distribution.manifest.v2+json")
                .retrieve()
                .bodyToMono(ImageMetaData.class)
                .block();
    }

    /**
     * - 매니페스트에서 이미지 설정(config) blob의 digest(id)를 결정. <br>
     * - 즉, 이미지 메타데이터들 중, port정보를 조회할 수 있게 해주는 id값.
     *
     * <h4>상세 설명</h4>
     * 1. 매니페스트에 ImageBlobDetail.imageBlobHashId(도커 api에서는 config.digest임.) 필드가 이미 있으면 그대로 반환 <br>
     * 2. 없으면(멀티 아키 매니페스트라는 의미)<br>
     *      -> manifests 리스트를 순회하면서 linux/amd64 플랫폼 항목을 찾기 <br>
     *      -> 찾은 항목의 id를 기반으로 다시 fetchImageMetaData(...) 호출 <br>
     *      -> 해당 manifest의 imageBlobHashInfo.imageBlobHashId 반환 <br>
     *
     * @param dockerAuthClient
     * @param namespace
     * @param image
     * @param imageMetaData
     * @return
     */
    private String fetchImageBlobHashId(WebClient dockerAuthClient, String namespace, String image, ImageMetaData imageMetaData) {
        return Optional.ofNullable(imageMetaData.getImageBlobHashInfo())
                .map(ImageBlobDetail::getImageBlobHashId)
                .orElseGet(() -> {
                    String additionalPlatformId = imageMetaData.getAdditionalImagePlatformAndId().stream()
                            .filter(this::isTargetPlatform)
                            .findFirst()
                            .orElseThrow(() -> new BusinessException(ErrorCode.DOCKER_DEFAULT_PORT_API_FAILED, "해당 플랫폼의 manifest를 찾을 수 없습니다."))
                            .getImageHashId();

                    ImageMetaData pm = fetchImageMetaData(dockerAuthClient, namespace, image, additionalPlatformId);
                    return pm.getImageBlobHashInfo().getImageBlobHashId();
                });
    }

    /**
     * - 멀티 아키일 경우, 플랫폼 정보가 우리가 원하는(linux + amd64) 조합인지 검사
     *
     * @param imagePlatformAndId
     * @return
     */
    private boolean isTargetPlatform(ImagePlatformAndId imagePlatformAndId) {
        Platform platform = imagePlatformAndId.getImagePlatform();
        return "linux".equals(platform.getOs())
                && "amd64".equals(platform.getCpuArchitecture());
    }

    /**
     * - 최종적으로 얻은 configDigest 를 사용해서 이미지 설정 blob을 바이너리(byte[])로 다운로드 <br>
     * -> json으로 파싱해서 RegistryConfig 객체로 반환한다.
     *
     * @param dockerAuthClient
     * @param namespace
     * @param imageName
     * @param blobHashId
     * @return
     */
    private BlobMetaData fetchBlobMetaData(WebClient dockerAuthClient, String namespace, String imageName, String blobHashId) {
        byte[] blobData = dockerAuthClient.get()
                .uri(uriBuilder.buildRegistryBlobUri(namespace, imageName, blobHashId))
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

        if (blobData == null) {
            throw new BusinessException(ErrorCode.DOCKER_DEFAULT_PORT_API_FAILED, "Config blob을 가져오지 못했습니다.");
        }

        try {
            return objectMapper.readValue(blobData, BlobMetaData.class);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.DOCKER_DEFAULT_PORT_API_FAILED, "Config blob 파싱에 실패했습니다.", e);
        }
    }

    private List<String> extractPorts(BlobMetaData blobMetaData) {
        Map<String, ?> portsMap = Optional.ofNullable(blobMetaData.getBlobMetaDataInfo())
                .map(BlobMetaDataInfo::getDefaultPorts)
                .orElse(Collections.emptyMap());

        return new ArrayList<>(portsMap.keySet());
    }

    /* 공통 로직 */
    private <T> T fetchMono(WebClient client, URI uri, Class<T> clazz, ErrorCode errorCode) {
        try {
            return client.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(clazz)
                    .block();
        } catch (Exception e) {
            log.error(">> fetchMono 실패 - URI: {}, Error: {}", uri, e.getMessage(), e);
            throw new BusinessException(errorCode);
        }
    }

    private <T> List<T> fetchFlux(WebClient client, URI uri, Class<T> clazz, ErrorCode errorCode) {
        try {
            return client.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToFlux(clazz)
                    .collectList()
                    .block();
        } catch (Exception e) {
            log.error(">> fetchFlux 실패 - URI: {}, Error: {}", uri, e.getMessage(), e);
            throw new BusinessException(errorCode);
        }
    }

    /**
     * <h4>메서드 내용</h4>
     * Flux로 스트리밍 받은 Docker 로그 데이터를 올바르게 파싱하기 위한 메서드.
     * <br><br>
     * <h4>메서드 설명</h4>
     * <p>Docker Engine API에서 로그를 가져올 때는 stdout, stderr를 하나의 TCP 스트림 위에
     * multiplex된 바이너리 프레임 형식으로 전송함. <br>
     * WebClient의 * {@code .bodyToFlux(DataBuffer.class)}를 사용하면 이 전체 바이너리 덩어리를
     * 프레임 경계 없이 한 번에 받아오기 때문에, 각 프레임을 분리하고
     * utf-8 문자열로 디코딩해야만 사람 읽을 수 있는 로그를 얻을 수 있음.</p>
     *
     * <p>multiplex 처리를 자동으로 해 주는 라이브러리를 도입할 수도 있지만, 이 기능을
     * 사용하는 곳이 하나뿐이므로 별도의 의존성 추가보다 메서드 구현으로 처리하는 것이
     * 더 깔끔하다고 판단하여 작업함.</p>
     *
     * <h4>Reactive 처리 이유</h4>
     * <ul>
     *   <li>Spring WebFlux의 WebClient는 비동기·논블로킹 API 기반으로 동작함. 그래서
     *       단순 {@code .block()} 호출만 사용하는 것보다 Flux/Mono 파이프라인을 통해
     *       자연스러운 흐름을 구현할 수 있음.</li>
     *   <li>로그는 실시간 스트리밍(follow 모드)으로 받아올 가능성이 있으므로,
     *       Flux를 사용해 확장성을 확보해둠.</li>
     *   <li>최종적으로는 {@code .block()}을 호출해서 동기처럼 결과를 얻지만,
     *       내부적으로는 (네트워크 호출 → 파싱 → 변환 과정)을 논블로킹 파이프라인으로
     *       구성해둠.</li>
     * </ul>
     *
     * <h4>메서드 로직 요약</h4>
     * <ul>
     *   <li>입력된 {@code ByteBuffer}를 순회하며 8바이트 헤더(1바이트 stream type +
     *       3바이트 reserved + 4바이트 payload length)를 먼저 읽음.</li>
     *   <li>헤더에서 얻은 payload 길이만큼 바이트 배열을 꺼내서 {@code new String(..., UTF-8)}
     *       으로 디코딩.</li>
     *   <li>디코딩된 문자열을 {@code List<String>}에 담고, 이걸 {@code Flux.fromIterable()}
     *       으로 래핑 후 반환.</li>
     * </ul>
     *
     * @param buf
     * Docker 엔진에서 받은 '원본' 로그 데이터가 들어 있는 버퍼(ByteBuffer). <br>
     * 이 버퍼는 내부에 읽기 위치(`position`)라는 포인터가 있는데,
     * 이 메서드 안에서 데이터를 꺼내올 때마다 그 포인터가 앞으로 이동함.
     * 그래서 같은 버퍼를 다시 읽으려면 포인터를 초기화해야 합니다.
     *
     * @return
     * 파싱된 로그 조각(각 프레임의 payload)을 UTF‑8 문자열로 변환한 ->
     * Reactor의 Flux<String> 형태로 돌려줌. <br>
     * 참고 ) Flux는 '문자열 여러 개를 순차적으로' 비동기 스트리밍하는 타입이고,
     * 여기서는 각각의 로그 청크(조각)가 Flux를 통해 하나씩 발행됩니다.
     */
    private Flux<String> demultiplex(ByteBuffer buf) {
        List<String> out = new ArrayList<>();
        while (buf.remaining() >= 8) {
            buf.get(); // stream type
            buf.get(); buf.get(); buf.get(); // reserved
            int len = buf.getInt(); // payload 길이
            if (buf.remaining() < len) {
                break; // 남은 데이터가 payload보다 작으면 중단
            }
            byte[] chunk = new byte[len];
            buf.get(chunk);              // payload 읽기
            out.add(new String(chunk, StandardCharsets.UTF_8));
        }
        return Flux.fromIterable(out);
    }

    // 사용자 serverIp로 접속하는 client 생성
    private WebClient maseUserServerWebClient(String serverIp) {
        return webClientBuilder
                .baseUrl("http://" + serverIp + ":3789")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
