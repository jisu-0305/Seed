# 1. 개요

---

인프라 세팅부터 빌드 오류 해결까지 자동화하는 DevOps 플랫폼  “SEED”

# 2. 사용 도구

---

- 이슈 관리 : Jira
- 형상 관리 : Git, GitLab
- 커뮤니케이션 : Notion, MatterMost
- 디자인 : Figma
- CI/CD : Jenkins, EC2

# 3. IDE

---

- Intellij : 2024.3.1.1
- vscode: 1.100.2

# 4. 개발 환경

---

### Frontend

| 항목 | 버전 |
| --- | --- |
| **Node.js** | 22.13.0 |
| **npm** | 10.9.2 |
| **Next.js** | 14.2.28 |
| **TypeScript** | 5.8.3 |

### Backend

| 항목 | 버전 |
| --- | --- |
| **Java (OpenJDK)** | 17 |
| **Spring Boot** | 3.4.4 |
| **Gradle** | 8.13 |
| **Spring Dependency Management** | 1.1.7 |

### AI

| 항목 | 버전 |
| --- | --- |
| **Python** | 3.13.1 |
| **pip** | 25.0.1 |
| **Fast API** | 0.115.12 |

### 외부 서비스

| 항목 | 용도 |
| --- | --- |
| **Firebase Cloud Message** | 서버 상태 |
| **Gitlab Oauth, Gitlab API** | Gitlab 소셜 로그인
Gitlab 레포지토리 연동 |
| **Open AI API 
- GPT 4.1
- O4 mini** | 오류 코드 분석
  오류 코드 수정
  AI 보고서 작성 |

# 5. 환경변수

---

### Frontend

- **local**

    ```jsx
    #서버 base URL
    NEXT_PUBLIC_APP_IP=http://localhost:8080/api
    
    # FCM
    NEXT_PUBLIC_FIREBASE_API_KEY=AIzaSyBjlgZuUBQQj8WT_zF6bbXe9Kt90iBd9SI
    NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=seed-98d39.firebaseapp.com
    NEXT_PUBLIC_FIREBASE_PROJECT_ID=seed-98d39
    NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET=seed-98d39.appspot.com
    NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=61810633453
    NEXT_PUBLIC_FIREBASE_APP_ID=1:61810633453:web:91652c26bbe899bcfc3ae4
    NEXT_PUBLIC_FIREBASE_MEASUREMENT_ID=G-DW6YNCXD65
    
    #fast api 서버 URL
    NEXT_PUBLIC_AI_IP=http://localhost:8001/ai
    ```

- **server**

    ```bash
    #서버 base URL
    NEXT_PUBLIC_APP_IP=https://seedinfra.store/api
    
    # FCM
    NEXT_PUBLIC_FIREBASE_API_KEY=AIzaSyBjlgZuUBQQj8WT_zF6bbXe9Kt90iBd9SI
    NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=seed-98d39.firebaseapp.com
    NEXT_PUBLIC_FIREBASE_PROJECT_ID=seed-98d39
    NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET=seed-98d39.appspot.com
    NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=61810633453
    NEXT_PUBLIC_FIREBASE_APP_ID=1:61810633453:web:91652c26bbe899bcfc3ae4
    NEXT_PUBLIC_FIREBASE_MEASUREMENT_ID=G-DW6YNCXD65
    
    #fast api 서버 URL
    NEXT_PUBLIC_AI_IP=https://seedinfra.store/ai
    ```


### Backend

- **local**

    ```jsx
    # Application
    SPRING_APPLICATION_NAME=backend
    
    # DOMAIN
    FRONT_BASE_URL=http://localhost:3000
    DOMAIN_NAME=http://localhost:8080
    
    # MySQL
    SPRING_DATASOURCE_URL=jdbc:mysql://seedinfra.store:3306/seed?useSSL=false&serverTimezone=Asia/Seoul
    
    # Public Key Retrieval 에러 나는 경우 : 마지막에 allow 옵션 적어주면 됩니다
    # SPRING_DATASOURCE_URL=jdbc:mysql://seedinfra.store:3306/seed?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true
    
    SPRING_DATASOURCE_USERNAME=seed
    SPRING_DATASOURCE_PASSWORD=seed0206!
    SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
    
    # JPA
    SPRING_JPA_HIBERNATE_DDL_AUTO=none
    SPRING_JPA_HIBERNATE_DIALECT=org.hibernate.dialect.MySQLDialect
    
    # JWT
    JWT_SECRET=adasfdsfpiojs0239odsfkj23jewfpiwefglk23lksdksdfklve83jdjxlzp0q023jdaksk20wks90di2jdcxlsld20ejcjlsdkdk
    
    # GitLab
    GITLAB_APPLICATION_ID=9d12160e6b1ae8d530b1068412520340e904c8c27c5fd7ab7bea4fced3b272eb
    GITLAB_REDIRECT_URI=http://localhost:8080/api/users/oauth/gitlab/callback
    GITLAB_CLIENT_SECRET=gloas-02b510aef464edd1f0f4cbd42c4fee36ab1cc85dbf1aec09cf07470cf2292d0f
    GITLAB_API_BASE_URL=https://lab.ssafy.com/api/v4
    
    # Redis
    SPRING_DATA_REDIS_HOST=seedinfra.store
    SPRING_DATA_REDIS_PORT=6379
    SPRING_DATA_REDIS_PASSWORD=seed0206!
    
    # Firebase
    FIREBASE_CONFIG_PATH=firebase/serviceAccountKey.json
    
    # Docker Hub & Registry & Engine
    DOCKER_HUB_BASE_URL=https://hub.docker.com/v2
    DOCKER_TOKEN_PREFIX=docker:token:
    DOCKER_REGISTRY_API_BASE_URL=https://registry-1.docker.io/v2
    DOCKER_AUTH_API_BASE_URL=https://auth.docker.io
    DOCKER_ENGINE_API_PORT=3789
    
    # 파일 업로드 기본 경로
    FILE_BASE_PATH=uploads
    
    # 로깅 레벨
    LOGGING_LEVEL_REACTOR_NETTY_HTTP_CLIENT=DEBUG
    LOGGING_LEVEL_EXCHANGEFUNCTIONS=TRACE
    LOGGING_LEVEL_ORG_EXAMPLE_BACKEND=DEBUG
    
    # fastAPI
    FASTAPI_BASE_URL=http://seedinfra.store:8001
    
    ```

- **server**

    ```jsx
    # Application
    SPRING_APPLICATION_NAME=backend
    
    # DOMAIN
    FRONT_BASE_URL=https://seedinfra.store
    DOMAIN_NAME=https://seedinfra.store
    
    # MySQL
    SPRING_DATASOURCE_URL=jdbc:mysql://seedinfra.store:3306/seed?useSSL=false&serverTimezone=Asia/Seoul
    
    SPRING_DATASOURCE_USERNAME=seed
    SPRING_DATASOURCE_PASSWORD=seed0206!
    SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
    
    # JPA
    SPRING_JPA_HIBERNATE_DDL_AUTO=none
    SPRING_JPA_HIBERNATE_DIALECT=org.hibernate.dialect.MySQLDialect
    
    # JWT
    JWT_SECRET=adasfdsfpiojs0239odsfkj23jewfpiwefglk23lksdksdfklve83jdjxlzp0q023jdaksk20wks90di2jdcxlsld20ejcjlsdkdk
    
    # GitLab
    GITLAB_APPLICATION_ID=9d12160e6b1ae8d530b1068412520340e904c8c27c5fd7ab7bea4fced3b272eb
    GITLAB_REDIRECT_URI=https://seedinfra.store/api/users/oauth/gitlab/callback
    GITLAB_CLIENT_SECRET=gloas-02b510aef464edd1f0f4cbd42c4fee36ab1cc85dbf1aec09cf07470cf2292d0f
    GITLAB_API_BASE_URL=https://lab.ssafy.com/api/v4
    
    # Redis
    SPRING_DATA_REDIS_HOST=seedinfra.store
    SPRING_DATA_REDIS_PORT=6379
    SPRING_DATA_REDIS_PASSWORD=seed0206!
    
    # Firebase
    FIREBASE_CONFIG_PATH=firebase/serviceAccountKey.json
    
    # Docker Hub & Registry & Engine
    DOCKER_HUB_BASE_URL=https://hub.docker.com/v2
    DOCKER_TOKEN_PREFIX=docker:token:
    DOCKER_REGISTRY_API_BASE_URL=https://registry-1.docker.io/v2
    DOCKER_AUTH_API_BASE_URL=https://auth.docker.io
    DOCKER_ENGINE_API_PORT=3789
    
    # 파일 업로드 기본 경로
    FILE_BASE_PATH=uploads
    
    # 로깅 레벨
    LOGGING_LEVEL_REACTOR_NETTY_HTTP_CLIENT=DEBUG
    LOGGING_LEVEL_EXCHANGEFUNCTIONS=TRACE
    LOGGING_LEVEL_ORG_EXAMPLE_BACKEND=DEBUG
    
    # fastAPI
    FASTAPI_BASE_URL=http://seedinfra.store:8001
    
    ```


### AI

- **local**

    ```bash
    # app/.env
    API_HOST=127.0.0.1
    API_PORT=8001
    
    # CORS
    DOMAIN_URL=https://k12a206.p.ssafy.io
    LOCAL_URL=http://localhost:3000
    WS_URL=ws://localhost:3000
    
    # Redi
    REDIS_HOST=localhost
    REDIS_PORT=6379
    REDIS_DB=0
    REDIS_PREFIX=seed
    
    # Database (MySQL)
    DB_URL=jdbc:mysql://k12a206.p.ssafy.io:3306/seed?useSSL=false&serverTimezone=Asia/Seoul
    
    # 이미지 업로드 경로
    UPLOAD_DIR=app/uploads
    
    # GPT
    OPENAI_API_KEY=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
    
    ```

- **server**

    ```bash
    # app/.env
    API_HOST=127.0.0.1
    API_PORT=8001
    
    # CORS
    DOMAIN_URL=https://k12a206.p.ssafy.io
    LOCAL_URL=http://localhost:3000
    WS_URL=ws://localhost:3000
    
    # Redis
    REDIS_HOST=k12a206.p.ssafy.io
    REDIS_PORT=6379
    REDIS_DB=0
    REDIS_PREFIX=seed
    
    # Database (MySQL)
    DB_URL=jdbc:mysql://k12a206.p.ssafy.io:3306/seed?useSSL=false&serverTimezone=Asia/Seoul
    
    # 이미지 업로드 경로
    UPLOAD_DIR=app/uploads
    
    # GPT
    OPENAI_API_KEY=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
    
    ```


# 6. 서비스 구축 및 설정

---

### EC2 인스턴스 생성

```jsx
Ubuntu Server 22.04 LTS, SSD Volume Type
t3.xlarge (4 cpu, 16gb)
보안그룹 -> 인바운드 규칙 -> TCP 22, 80, 443, 3306, 6379 ,8080, 9090 필수 개방
```

### MySQL 설치 및 설정

- 사용자 이름, 비밀번호는 상황에 맞게 수정

```jsx

// mysql 설치 환인
which mysql

// 업데이트
sudo apt update

// mysql 설치
sudo apt install mysql-server -y

// [옵션] 보안 설정
sudo mysql_secure_installation

// mysql 실행
sudo systemctl start mysql

// mysql 실행 확인
sudo systemctl status mysql

// 방화벽 도구를 사용해 포트 열기
sudo ufw allow 3306

// 포트 상태 확인
sudo ufw status

// 방화벽 재실행
sudo ufw reload

// mysql 접속
sudo mysql

// 사용자 생성
create user 'seed'@'%' identified by 'seed0206!';

// 사용자에게 권한 부여
GRANT ALL PRIVILEGES ON *.* TO 'seed'@'%';

// 변경사항 적용
FLUSH PRIVILEGES;

// bind address 확인
SHOW VARIABLES LIKE 'bind_address';

// mysql 나와서 shell에서 설정 파일 열기 -> nano 편집기로 접근됨
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf

// bind address 수정 (외부에서 접근해야하니깐)
bind-address = 0.0.0.0
ctrl + O 저장
ctrl + X 나가기

// mysql 재실행
sudo systemctl restart mysql

```

### JDK 설치

```jsx
sudo ufw status
sudo ufw allow 8080
sudo ufw reload

sudo apt install openjdk-17-jdk -y
java --version
```

### Docker  설치

```jsx
sudo apt update
sudo apt install apt-transport-https ca-certificates curl software-properties-common gnupg lsb-release -y

// Docker GPG 키 다운로드
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

// 공식 저장소 등록
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update

// 도커 엔진 설치
sudo apt install docker-ce docker-ce-cli containerd.io -y
sudo docker --version
sudo systemctl enable --now docker
```

### Nginx 설치 및 설치

```jsx
sudo ufw allow 443/tcp
sudo ufw reload 
sudo ufw status

sudo apt upgrade -y
sudo apt install nginx -y
```

### HTTP 설정 (바로 HTPPS로 넘어가도 됨)

- 도메인 이름은 상황에 맞게 수정

```jsx
etc/nginx/sites-available/ : nginx와 설정과 관련된 모든 파일 저장
etc/nginx/sites-enabled/ : 활성화된 설정만 바로가기 형태로 존재 즉, available의 파일을 연결해줘야함

// nano 편집기 열기
sudo nano /etc/nginx/sites-available/seed

// 아래 내용 우클릭으로 붙여넣기 -> cntl + O 저장 -> ENTER -> cntl + X 닫기
server {
    listen 80;
    server_name k12a206.p.ssafy.io;

    location / {
        proxy_pass http://127.0.0.1:8080;
        include proxy_params;
    }
}

// NGINX 문법 검사
sudo nginx -t

// enabled에 있는 default 삭제
sudo rm /etc/nginx/sites-enabled/default

// available -> enable로 myapp 심볼릭 링크 생성 (바로가기)
sudo ln -s /etc/nginx/sites-available/seed /etc/nginx/sites-enabled/

// NGINX 재시작
sudo systemctl reload nginx

// [Optional] 80포트를 사용하여 어플리케이션에 접근하면되므로 8080포트 닫기
sudo ufw status numbered
sudo ufw delete [numbber]
```

### HTTPS 설정

- 도메인 이름, 이름은 상황에 맞게 수정

```jsx
sudo apt update
sudo apt install -y certbot python3-certbot-nginx
certbot --version

sudo certbot --nginx \
  -d [DOMAIN] \
  --email [EMAIL] \
  --agree-tos \
  --redirect \
  --non-interactive
 
// 성공했다면 /etc/nginx/sites-available/seed 내용 변경 확인 
```

### 최종 Nginx configuration 파일

- 도메인 이름은 상황에 맞게 수정

```jsx
server {
    listen 80;
    server_name k12a206.p.ssafy.io;

    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name k12a206.p.ssafy.io;

    ssl_certificate /etc/letsencrypt/live/k12a206.p.ssafy.io/fullchain.pem; # managed by Certbot
    ssl_certificate_key /etc/letsencrypt/live/k12a206.p.ssafy.io/privkey.pem; # managed by Certbot
    include /etc/letsencrypt/options-ssl-nginx.conf; # managed by Certbot
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # managed by Certbot
		
    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
    }

    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /swagger-ui/ {
        proxy_pass http://localhost:8080/swagger-ui/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    location /v3/api-docs {
        proxy_pass http://localhost:8080/v3/api-docs;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        add_header Access-Control-Allow-Origin *;
    }

    location /ws {
        proxy_pass http://localhost:8080/ws;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_read_timeout 86400;
    }
}

```

### Jenkins 설치 및 설정

```jsx
// 필수 패키지 및 Java 설치
sudo apt update

// Jenkins 공식 GPG 키 등록
curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key \
  | sudo tee /usr/share/keyrings/jenkins-keyring.asc > /dev/null

// Jenkins APT 리포지토리 추가
echo \
  "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] \
  https://pkg.jenkins.io/debian-stable binary/" \
  | sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null

// 패키지 목록 갱신 & Jenkins 설치
sudo apt update
sudo apt install -y jenkins

// Jenkins 서비스 활성화 및 시작
sudo systemctl enable --now jenkins

// Port 변경
sudo sed -i 's/^#*HTTP_PORT=.*/HTTP_PORT=9090/' /etc/default/jenkins
sudo sed -i 's/Environment="JENKINS_PORT=[0-9]\+"/Environment="JENKINS_PORT=9090"/' /usr/lib/systemd/system/jenkins.service

// systemd에게 단위 파일 변경 사항 다시 읽도록 지시
sudo systemctl daemon-reload

// jenkins에게 docker 권한 주기
sudo usermod -aG docker jenkins

// Jenkins 재시작
sudo systemctl restart jenkins

// 상태 확인
sudo systemctl status jenkins

// 방화벽(UFW)에서 포트 9090 열기 (필요 시)
sudo ufw allow 9090

// 설치 확인
// http://<EC2_PUBLIC_IP>:9090 로 접속

// 최초 로그인용 관리자 암호는 아래에서 확인
sudo cat /var/lib/jenkins/secrets/initialAdminPassword

// GUI 접속 후 사용자 계정 생성 및 플러그인 다운로드
```

### Jenkins Pipeline 구축

```jsx
// 필요한 플러그인 설치
- GitLab Plugin

// Jenkins 관리 -> System
- Jenkins Location
	- http://<도메인>:<포트> // 이 설정을 안해주면 jenkins 느려짐

- GitLab Webhook 생성
	- Enable authentication for '/project' end-point : 옵션 끄기
	- GitLab connections 추가
	  - Connection name // 별칭
	  - GitLaqb host URL // https://lab.ssafy.com/
	  - GitLab API token // GitLab에서 발급받은 read, write 권한을 가진 accessToken

// Freestyle project - job or pipeline 생성
gitlab connection : // 아까 생성한 connection 넣어주기

Triggers
- Build when a change is pushed to Gitlab 와 Push Evnets : 체크
- GitHub hook trigger for GITScm polling : 체크

Pipeline
- Pipeline script 작성
```

### Jenkins Pipeline Script

- 작업할 gitlab 디렉토리 REPO_URL로 설정
- credentials에서 환경변수 추가하기
    - backend (.env)
    - frontend(.env)
    - ai (.env)
    - firebase (fcm token)
        - 인증 .json파일

          [serviceAccountKey.json](attachment:bf1d61aa-ac6a-46a3-99fd-a2c6bbcb9f70:serviceAccountKey.json)

        - path: src > main > resources > firebase > serviceAccountKey.json

```jsx
pipeline {
    agent any

    environment {
        GIT_BRANCH = 'dev'
        REPO_URL   = 'https://[gitlab_username]:[gitlab_accesstoken]@lab.ssafy.com**/[gitlab_name_space]'**
    }

    stages {
        stage('Checkout') {
            steps {
                echo '1. 워크스페이스 정리 및 소스 체크아웃'
                deleteDir() 
                git branch: "${GIT_BRANCH}", url: "${REPO_URL}"
            }
        }

        stage('Build Backend') {
            when {
                changeset pattern: 'backend/.*', comparator: 'REGEXP'
            }
            steps {
                echo '2. Backend 변경 감지, 빌드 및 배포'
                
                withCredentials([file(credentialsId: "backend", variable: 'BACKEND_ENV')]) {
                  sh '''
                    set -e
                    echo "  - 복사: $BACKEND_ENV → ${WORKSPACE}/backend/.env"
                    cp "$BACKEND_ENV" "${WORKSPACE}/backend/.env"
                  '''
                }
                
                withCredentials([file(credentialsId: "firebase", variable: 'FIREBASE_ENV')]) {
                  sh '''
                    set -e
                    
                    echo "  - 폴더 생성: backend/src/main/resources/firebase"
                    mkdir -p "${WORKSPACE}/backend/src/main/resources/firebase"
                    
                    echo "  - 복사: $FIREBASE_ENV → ${WORKSPACE}/backend/src/main/resources/firebase/serviceAccountKey.json"
                    cp "$FIREBASE_ENV" "${WORKSPACE}/backend/src/main/resources/firebase/serviceAccountKey.json"
                  '''
                }
                
                dir('backend') {
                    sh '''
                        set -e
                        chmod +x ./gradlew
                        echo "  - Docker 이미지 빌드"
                        docker build --no-cache -t spring-app .
        
                        echo "  - 기존 컨테이너 제거"
                        docker stop my-spring-app || true
                        docker rm my-spring-app || true
        
                        echo "  - Docker 컨테이너 실행"
                        docker run -d -p 8080:8080 \
                          --env-file .env \
                          --name my-spring-app \
                          spring-app
                    '''
                }
                echo '[INFO] 백엔드 완료'
            }
        }

        stage('Build Frontend') {
            when {
                changeset pattern: 'frontend/.*', comparator: 'REGEXP'
            }
            steps {
                echo '3. Frontend 변경 감지, 빌드 및 배포'
                
                withCredentials([file(credentialsId: "front", variable: 'FRONT_ENV')]) {
                  sh '''
                    set -e
                    echo "  - 복사: $FRONT_ENV → ${WORKSPACE}/frontend/.env"
                    cp "$FRONT_ENV" "${WORKSPACE}/frontend/.env"
                  '''
                }
                
                dir('frontend') {
                    sh '''
                        set -e
                        echo "프론트엔드 Docker 이미지 빌드"
                        docker build --no-cache -t my-next-app .
                        
                        echo "이전 프론트엔드 컨테이너 종료 및 제거"
                        docker stop frontend || true
                        docker rm frontend || true
                        
                        echo "새 프론트엔드 컨테이너 실행"
                        docker run -d --restart unless-stopped --name frontend -p 3000:3000 my-next-app
                    '''
                }
                echo '[INFO] 프론트엔드 완료'
            }
        }

        stage('Build AI') {
            when {
                changeset pattern: 'ai/.*', comparator: 'REGEXP'
            }
            steps {
                echo '4. AI 변경 감지, 빌드 시작'
                
                withCredentials([file(credentialsId: "ai", variable: 'AI_ENV')]) {
                  sh '''
                    set -e
                    echo "  - 복사: $AI_ENV → ${WORKSPACE}/ai/.env"
                    cp "$AI_ENV" "${WORKSPACE}/ai/.env"
                  '''
                }
                
                dir('ai') {
                    sh '''
                        set -e
                        echo "AI Docker 이미지 빌드"
                        docker build --no-cache -t my-ai-app .
        
                        echo "이전 AI 컨테이너 종료 및 제거"
                        docker stop ai || true
                        docker rm ai || true
        
                        echo "AI 컨테이너 실행"
                        docker run -d --restart unless-stopped \
                            --name ai \
                            -p 8001:8001 \
                            -v $(pwd)/app/uploads:/app/uploads \
                            --env-file .env \
                            my-ai-app
                    '''
                }
                echo '[INFO] AI 완료'
            }
        }
    }
}
```

# 6. 핵심 API 사용법

---

### 로그인 (Gitlab Oauth 사용 - 존재하지 않는 회원의 경우 자동으로 회원가입 후 로그인)

GET : /api/users/oauth/gitlab/login

요청

```jsx
header
"Authorization" : [service access token]

body
해당없음
```

응답

```jsx
body
"success" : true
```

### Gitlab Personal AccessToken 저장

POST : /api/users/pat

**요청**

```jsx
header
"Authorization" : [service access token]

body      
"pat" : [gitlab personal access token]
```

**응답**

```jsx
body
"success" : true
```

### 프로젝트 생성

POST : /api/projects

**요청**

```jsx
header
"Authorization" : [service access token]

body
{
  "serverIP": "1.1.1.1",
  "repositoryUrl": "https://lab.ssafy.com/potential1205/seed0513.git",
  "structure": "MONO",
  "gitlabTargetBranch": "master",
  "frontendBranchName": "",
  "frontendDirectoryName": "frontend",
  "backendBranchName": "",
  "backendDirectoryName": "backend",
  "nodejsVersion": "22",
  "frontendFramework": "Vue.js",
  "jdkVersion": "21",
  "jdkBuildTool": "Gradle"
}
"clientEnvFile" : [frontend environment file]
"serverEnvFile" : [server environment file]
```

**응답**

```jsx
{
  "id": 123,
  "projectName": "My Awesome Project",
  "createdAt": "2025-05-20T12:34:56",
  "gitlabProjectId": 456789,
  "memberList": [
    {
      "userId": 1,
      "userName": "이재훈",
      "userIdentifyId": "jaehoonlee",
      "profileImageUrl": "https://example.com/avatars/1.png",
      "status": "ACCEPTED"
    },
    {
      "userId": 2,
      "userName": "김민수",
      "userIdentifyId": "minsu123",
      "profileImageUrl": "https://example.com/avatars/2.png",
      "status": "INVITED"
    }
  ],
  "autoDeploymentEnabled": true,
  "httpsEnabled": false,
  "buildStatus": "INIT",
  "lastBuildAt": null
}
```

### 자동 배포 파이프라인 구축 API

POST : /api/server/deployment

**요청**

```jsx
header
"Authorization" : [service access token]

body      
"pemFile" : [ec2 .pem file]
"projectId" : 4
```

**응답**

```jsx
body
"success" : true
```

### HTTPS 전환 API

POST : /api/server/deployment

**요청**

```jsx
header
"Authorization" : [service access token]

body
{
  "projectId": 4
  "domain": "mydomain.com",
  "email": "dlwogns1205@naver.com",
}
"pemFile" : [ec2 .pem file]
```

**응답**

```jsx
body
"success" : true
```

### **빌드 오류 자동 수정 API**

POST : /api/self-cicd/resolve

**요청**

```jsx
header
"Authorization" : [service access token]

body
"projectId": 4
"personalAccessToken": [gitlab personal access token]
"failType": "BUILD" // or RUNTIME
```

**응답**

```jsx
body
"success" : true
```