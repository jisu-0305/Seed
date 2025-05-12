package org.example.backend.domain.docker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.util.DockerUriBuilder;
import org.example.backend.controller.request.docker.DockerContainerLogRequest;
import org.example.backend.controller.response.docker.DemonContainerStateCountResponse;
import org.example.backend.controller.response.docker.ImageResponse;
import org.example.backend.domain.docker.dto.ContainerDto;
import org.example.backend.domain.docker.dto.DockerTag;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DockerApiClientImpl implements DockerApiClient {

    private final WebClient dockerHubWebClient;
    private final WebClient dockerWebClient;
    private final DockerUriBuilder uriBuilder;

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
    public List<ContainerDto> getContainersByName(String nameFilter) {
        URI uri = uriBuilder.buildContainersByNameUri(nameFilter);
        log.debug(">> 이름 기반 컨테이너 조회 URI: {}", uri);

        return fetchFlux(dockerWebClient, uri, ContainerDto.class, ErrorCode.DOCKER_HEALTH_API_FAILED);
    }

    /**
     * 지정한 컨테이너의 로그를 조회하여 문자열 리스트로 반환하는 메서드.
     * <p>
     * 1. DockerContainerLogRequest로 부터 uri 생성 <br>
     * 2. WebClient로 raw DataBuffer Flux를 수신 <br>
     * 3. {@link #demultiplex(ByteBuffer)}로 multiplexed 바이너리 프레임을 UTF‑8 문자열로 분리<br>
     * 4. 리스트로 수집(block).
     *
     * @param containerId 조회할 Docker 컨테이너의 ID
     * @param filter      로그 필터 옵션(출력 타입, tailLines, timestamps 등)
     * @return 파싱된 로그 라인들을 담은 List&lt;String&gt;
     */
    @Override
    public List<String> getContainerLogs(String containerId, DockerContainerLogRequest filter) {

        URI uri = uriBuilder.buildContainerLogsUri(
                containerId,
                filter.sinceSeconds(),
                filter.untilSeconds()
        );
        log.debug(">> 컨테이너 로그 조회 URI: {}", uri);

        // 1) Raw DataBuffer flux로 받기
        Flux<DataBuffer> rawFlux = dockerWebClient.get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(DataBuffer.class);

        // 2) Demultiplex and collect lines
        List<String> logLines = rawFlux
                .flatMap(dataBuffer -> demultiplex(dataBuffer.asByteBuffer()))
                .collectList()
                .block();

        return logLines;

    }

    /* 공통 로짘 */
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
}
