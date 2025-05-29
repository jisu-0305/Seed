# SEED (Super E-Easy Deployment : 서버 배포 자동화 서비스)
---

> ### 목차
1. [서비스 개요 및 배경](#1-서비스-개요-및-배경)
2. [주요 기능](#2-주요-기능)
3. [서비스 흐름 및 화면 구성](#3-서비스-흐름-및-화면-구성)
5. [팀 구성 및 역할](#4-팀-구성-및-역할)

## 1. 서비스 개요 및 배경
### 1-1. 서비스 개요
> **SEED** (Super E Easy Deployment)는 EC2 서버 세팅, GitLab 자동배포 CI/CD 구축·연결, 빌드 에러 대응까지 자동화한 서비스입니다.

[![영상포트폴리오](http://i.ytimg.com/vi/hQatrKALDUA/0.jpg)](https://www.youtube.com/watch?v=hQatrKALDUA)

### 1-2. 문제 정의
- **반복되는 서버 세팅 부담**
  매번 동일한 EC2 초기 환경 구성 → 시간 소모와 휴먼 에러 발생
- **인프라 학습 장벽**
  프론트·백엔드 개발자에게는 인프라 지식이 진입장벽으로 작용
- **수동적 에러 대응**
  빌드 실패 시 로그 확인·코드 수정·재빌드 과정을 모두 개발자가 직접 수행해야 함

### 1-3. SEED 서비스의 의의
1. **자동 서버 세팅 & CI/CD 구축**
   - EC2에 Docker, Docker-Compose, Jenkins 설치 및 초기화
   - GitLab/GitHub 연동 Webhook 설정 → 자동 빌드·배포 파이프라인 완성
2. **AI 기반 빌드 에러 수정 지원**
   - 실패 로그 분석 → 관련 코드·환경 변수 자동 수정 제안
   - 재빌드를 통한 검증 → 성공 시 MR 자동 생성

### 1-4. 기대 효과
- **개발 생산성 향상**: 인프라 설정에 쓰이는 시간을 절약하고 서비스 개발에 집중
- **빠른 장애 대응**: AI 분석 → 자동 수정 → 즉시 재배포
- **쉬운 접근성**: 복잡한 CLI나 GUI 없이도 직관적인 워크플로우 제공

## 2. 주요 기능
![주요 기능](images/seed_main_function.png)

### 2-1 원클릭 자동 배포 파이프라인인 구축
- EC2 서버에 Docker, Docker-Compose, Jenkins를 자동으로 설치·설정함  
- GitLab/GitHub 리포지토리 연동 Webhook 등록 및 Jenkins Job을 자동 생성·실행함  
- 코드 클론부터 컨테이너 빌드·배포, Nginx 설정·헬스체크까지 한 번에 처리함  

### 2-2 원클릭 HTTPS/도메인 적용
- Nginx 설치 및 리버스 프록시 기본 설정 자동화
- DNS 이름으로 사이트 이동 가능능  

### 2-2 AI 기반 빌드 에러 수정 지원
- 빌드 실패 로그를 수집하여 GPT로 분석함  
- 관련 코드·환경 변수 자동 수정안을 제안함  
- 수정 적용 후 재빌드를 시도하고, 성공 시 MR/PR을 자동 생성함  


## 3. 서비스 흐름 및 화면 구성
### 3-1. 서비스 흐름
![서비스 흐름](images/service-flow.png)
1. **배포 요청 접수**  
   - 사용자로부터 EC2 IP(포트 22), .pem 파일, 서비스명, 어플리케이션 목록(이름·포트) 등을 입력받음
2. **EC2 초기 설정**  
   - Docker, Jenkins, 사용자의 어플리케이션 설치 및 초기화 실행
3. **webhook 설정 및 Jenkins Job 생성**
4. **코드 클론 및 컨테이너 빌드**
   - Jenkins volume에 GitLab 코드 클론
   - 빌드용(node) 및 사용자 앱 컨테이너를 Docker Compose 파일로 정의·실행함.
5. **Nginx 설정 및 배포 완료**
   - EC2에 Nginx 설치 후 `default.conf`, `nginx.conf` 생성·적용함.
   - 배포 성공 여부를 헬스체크하여 결과를 사용자에게 응답함.
6. **에러 발생 시 자동 대응**
   - 빌드 실패 로그 수집 → AI Agent가 GPT로 분석함.
   - 코드 또는 환경 설정 자동 수정 후 재빌드 시도함.
   - 성공 시 최종 MR 생성 및 요약 정보 제공함.

### 3-2. AI 아키텍처
![서비스 흐름](images/seed_ai_architecture.png)

### 3-2. 화면 구성
> Light/Dark 모드 지원 (예시는 light 모드)

<table border="1" cellspacing="0" cellpadding="8">
  <tr>
    <td align="center">
      <img src="./images/랜딩페이지.svg" alt="렌딩 페이지" width="400" />
    </td>
    <td align="center">
      <img src="./images/로그인.svg" alt="로그인 페이지" width="400" />
    </td>
  </tr>
  <tr>
    <td align="center">렌딩페이지</td>
    <td align="center">로그인 페이지</td>
  </tr>

  <!-- 2번째 줄 -->
  <tr>
    <td align="center">
      <img src="./images/대시보드.svg" alt="대시보드" width="400" />
    </td>
    <td align="center">
      <img src="./images/씨앗 프로젝트 생성-기본정보-모노.svg" alt="기본정보-모노" width="120" />
      <img src="./images/씨앗 프로젝트 생성-서버 접속정보.svg" alt="서버 접속정보" width="120" />
      <br/>
      <img src="./images/씨앗 프로젝트 생성-어플리케이션 정보.svg" alt="어플리케이션 정보" width="120" />
      <img src="./images/씨앗 프로젝트 생성-환경변수.svg" alt="환경변수" width="120" />
      <img src="./images/씨앗 프로젝트 생성-최종.svg" alt="최종" width="120" />
    </td>
  </tr>
  <tr>
    <td align="center">대시보드</td>
    <td align="center">프로젝트 생성</td>
  </tr>

  <!-- 3번쨰 줄 -->
  <tr>
    <td align="center">
      <img src="./images/프로젝트 관리.svg" alt="프로젝트 관리" width="120" />
      <img src="./images/프로젝트 상세.svg" alt="프로젝트 상세" width="120" />
      <br/>
      <img src="./images/프로젝트 수정.svg" alt="프로젝트 수정" width="120" />
      <img src="./images/AI 보고서 - 리스트형식.svg" alt="AI 보고서" width="120" />
    </td>
    <td align="center">
      <img src="./images/유저 화면 - 프로필.svg" alt="프로필" width="400" />
    </td>
  </tr>
  <tr>
    <td align="center">프로젝트 상세</td>
    <td align="center">유저 화면</td>
  </tr>

</table>

<br>

## 4. 팀 구성 및 역할

|                                                                                     |                                                                                     |                                                                                     |
| :---------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------: |
| <img src="./images/재훈새싹.png" width="300"/><br>**이재훈**<br>INFRA & PM<br>[@potential1205](https://github.com/potential1205) | <img src="./images/지수새싹.png" width="300"/><br>**김지수**<br>INFRA & BE<br>[@jisu-0305](https://github.com/jisu-0305) | <img src="./images/유진새싹.png" width="300"/><br>**박유진**<br>BE<br>[GitHub](https://github.com/REPLACE_USERNAME)     |
| <img src="./images/승엽새싹.png" width="300"/><br>**강승엽**<br>AI & BE<br>[GitHub](https://github.com/REPLACE_USERNAME)  | <img src="./images/예슬새싹.png" width="300"/><br>**공예슬**<br>AI & FE<br>[GitHub](https://github.com/REPLACE_USERNAME) | <img src="./images/효승새싹.png" width="300"/><br>**이효승**<br>FE<br>[GitHub](https://github.com/REPLACE_USERNAME)     |






| Contributors | Role                       | Position                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| ------------ | -------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 이재훈      | 팀장, <br /> Infra       | - **프로젝트 일정 관리:** 전체 개발 로드맵 및 마일스톤 수립, 주요 단계별 일정 조율, 팀 간 커뮤니케이션 및 데일리 스크럼 주도로 진행 상황 모니터링 <br> - **시스템 아키텍처 설계:** Jenkins, GitLab, Docker, Nginx를 포함한 CI/CD 파이프라인 설계, 서비스 배포 흐름 정의 및 서버 환경 구성 표준화, GitLab/GitHub 및 다양한 빌드 도구(JDK 17·21, Maven·Gradle, Node.js 20·22) 호환성을 고려한 확장 가능 아키텍처 설계 <br>                                                                                        - **JSCH 기반 원클릭 자동 배포 파이프라인 구축 API 개발 :**  도커 기반 사용자 정의 어플리케이션 실행 로직 구현, 시스템 필수 어플리케이션(JDK, Docker, Nginx, Jenkins) 정의 및 실행 로직 구현, Jenkinsfile/Dockerfile 동적 생성 로직과 파이프라인 구축 로직 구현 <br> - **HTTPS 전환 API 설계 및 Nginx 설정 템플릿 작성 :**  Certbot 설치부터 SSL 인증서 발급, 기본 Nginx 설정 오버라이드, 최종 HTTPS 전환용 nginx.conf 템플릿 설계 및 구현    <br>     - **자동 배포 구축 및 인프라 관리 :**  Jenkins 기반 서비스 자동 배포 파이프라인 설계 및 구축, EC2 서버 환경 세팅 및 관리 <br>                                                                                                              |
| 김지수      | Backend, <br /> Infra       | - **AI 코드 수정 로직 설계:** Jenkins, Gitlab, Docker API등을 통해 API로 사용자의 서비스 코드를 변경가능하도록 설계  <br> - **사용자 프로젝트 브렌치 생성 및 커밋 생성 로직 구현 :** Util로 구현된 Gitlab의 로직과 AI가 변경한 코드들을 실제로 커밋 남기도록 해당 로직 설계 및 구현 <br> - **AI 보고서 생성로직 연결 및 엔티티 구현:** 사용자의 프로젝트 자동 코드 수정에 대한 AI 보고서 엔티티 정의 및 로직 연결 <br> - **Java Jsch  통신 가이드라인 작성**: 사용자의 서버에 접속하기 위한 Jsch 통신 방식 정의 및 초반 인프라 설계 진행                                                                                                                                                                                                                                                                                                                                                 |
| 박유진 | Backend | - **Docker API 유틸 모듈 개발:** 컨테이너 로그 조회, Health Check, 이미지 태그 및 정보 자동 수집 등 Docker Engine API 활용 기능 구현<br> - **GitLab API 기능 확장:** 브랜치 생성, Merge Request 생성, Git Diff 비교, Webhook 등록 등 CI/CD 자동화를 위한 GitLab API 유틸 통합<br> - **Mock 프로젝트 기반 통합 테스트 환경 구축:** FCM, 프론트엔드 및 백엔드 빌드 테스트를 위한 Mock 프로젝트 템플릿 설계 및 구축<br>                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| 강승엽       | Backend      | - **Jenkins 자동화 API 개발:** Jenkins 설치, 사용자 등록, 플러그인 설정, CI/CD 파이프라인 생성 등 전체 자동화 API 구현 <br> - **HTTPS 설정 자동화 및 로그 기능 구현:** Nginx + Certbot 기반 HTTPS 전환 로직 개발 및 단계별 실행 로그 DB 기록 API 구현 <br> - **FCM 기반 알림 시스템 개발:** 프로젝트 초대, 빌드 실패 등 상황별 FCM 메시지 전송 시스템 설계 및 구현 <br> - **JWT 기반 초대 API 개발:** 사용자 검색, 초대/수락/거절 처리 등 협업 기능 구현 <br> - **멀티파트 기반 프로젝트 생성/수정 API 구현:** .env, .pem 파일 포함한 요청 처리 로직 설계 <br>  <br>                                                                                                                                                                                                                                                                                                           |
| 공예슬       | AI, <br> Frontend      | - **AI MultiAgent 개발:** 빌드 에러를 바탕으로 코드를 수정하는 멀티 에이전트 개발 <br> - **프로젝트 상세 화면:** 빌드 상태에 따른 실시간 현황을 확인할 수 있도록 화면 구현 <br> - **피그마 및 화면 설계:** 프로젝트에 필요한 화면 및 UI/UX 설계 및 구현현 <br>  |
| 이효승       | Frontend | - **예시 소제목 1:** 예시 내용1 <br> - **예시 소제목 2:** 예시 내용2 <br> <br>                                                                                                                                                                                                                                                                                                             |
