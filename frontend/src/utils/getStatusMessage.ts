type StatusCategory = 'build' | 'ai';

interface StatusInfo {
  message: string;
  progress: number;
  category: StatusCategory;
}

export const SERVER_STATUS_INFO: Record<string, StatusInfo> = {
  // 🚀 빌드(EC2 세팅) 영역
  INIT: {
    message: 'EC2 세팅 버튼을 눌러주세요',
    progress: 0,
    category: 'build',
  },
  SET_SWAP_MEMORY: {
    message: '스왑 메모리 설정 중',
    progress: 5,
    category: 'build',
  },
  UPDATE_PACKAGE: {
    message: '서버 패키지 업데이트 중',
    progress: 10,
    category: 'build',
  },
  INSTALL_JDK: { message: 'JDK 설치 중', progress: 20, category: 'build' },
  INSTALL_DOCKER: {
    message: 'Docker 설치 중',
    progress: 30,
    category: 'build',
  },
  RUN_APPLICATION: {
    message: '애플리케이션 실행 중',
    progress: 35,
    category: 'build',
  },
  INSTALL_NGINX: { message: 'Nginx 설치 중', progress: 40, category: 'build' },
  INSTALL_JENKINS: {
    message: 'Jenkins 설치 중',
    progress: 50,
    category: 'build',
  },
  INSTALL_JENKINS_PLUGINS: {
    message: 'Jenkins 플러그인 설치 중',
    progress: 60,
    category: 'build',
  },
  SET_JENKINS_INFO: {
    message: 'Jenkins 정보 설정 중',
    progress: 65,
    category: 'build',
  },
  CREATE_JENKINS_JOB: {
    message: 'Jenkins Job 생성 중',
    progress: 70,
    category: 'build',
  },
  CREATE_JENKINSFILE: {
    message: 'Jenkinsfile 생성 중',
    progress: 75,
    category: 'build',
  },
  CREATE_FRONTEND_DOCKERFILE: {
    message: '프론트 Dockerfile 생성 중',
    progress: 80,
    category: 'build',
  },
  CREATE_BACKEND_DOCKERFILE: {
    message: '백엔드 Dockerfile 생성 중',
    progress: 85,
    category: 'build',
  },
  CREATE_WEBHOOK: { message: '웹훅 생성 중', progress: 90, category: 'build' },
  FINISH: {
    message: 'EC2 세팅이 완료되었습니다!',
    progress: 100,
    category: 'build',
  },
  FAIL: {
    message: 'EC2 세팅 중 오류가 발생했어요 😢',
    progress: 0,
    category: 'build',
  },

  // 🤖 AI 자동 수정 영역
  JENKINS_BUILD_LOG: {
    message: 'Jenkins 빌드 로그 수집 중',
    progress: 5,
    category: 'ai',
  },
  COLLECTING_APP_INFO: {
    message: '애플리케이션 정보 및 Git 변경사항 수집 중',
    progress: 10,
    category: 'ai',
  },
  INFERING_ERROR_SOURCE: {
    message: '에러 원인을 AI가 분석 중',
    progress: 15,
    category: 'ai',
  },
  COLLECTING_LOGS_AND_TREES: {
    message: '로그 및 디렉토리 구조 수집 중',
    progress: 20,
    category: 'ai',
  },
  SUSPECT_FILE: {
    message: '문제의 원인 파일 추론 중',
    progress: 30,
    category: 'ai',
  },
  GET_ORIGINAL_CODE: {
    message: '원본 코드 가져오는 중',
    progress: 35,
    category: 'ai',
  },
  GET_INSTRUCTION: {
    message: '코드 수정 지시 생성 중',
    progress: 45,
    category: 'ai',
  },
  GET_FIXED_CODE: {
    message: '수정된 코드 생성 중',
    progress: 55,
    category: 'ai',
  },
  COMMITTING_FIXES: { message: '커밋 중', progress: 65, category: 'ai' },
  JENKINS_REBUILDING: { message: '재빌드 중', progress: 75, category: 'ai' },
  REBUILD_SUCCESS: {
    message: 'AI 수정 이후 빌드 성공! 🎉',
    progress: 80,
    category: 'ai',
  },
  REBUILD_FAIL: {
    message: 'AI 수정 이후 빌드 실패',
    progress: 80,
    category: 'ai',
  },
  CREATING_REPORT: {
    message: 'AI 보고서 생성 중',
    progress: 85,
    category: 'ai',
  },
  CREATE_PULL_REQUEST: {
    message: 'Merge 요청 생성 중',
    progress: 90,
    category: 'ai',
  },
  SAVING_REPORT: { message: '보고서 저장 중', progress: 95, category: 'ai' },
  FINISH_WITH_AI: {
    message: 'AI 자동 수정 및 배포 완료!',
    progress: 100,
    category: 'ai',
  },
  FAIL_WTIH_AI: {
    message: 'AI 자동 수정 완료, 배포 실패 😢😢',
    progress: 100,
    category: 'ai',
  },
  BUILD_FAIL_WITH_AI: {
    message: 'AI 자동 수정 중 오류 발생 😢',
    progress: 0,
    category: 'ai',
  },
};
