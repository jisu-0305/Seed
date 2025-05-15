export interface MeResponse {
  success: boolean;
  message: string;
  data: {
    userId: number;
    userIdentifyId: string; // 실제 이메일
    userName: string; // 깃랩 유저네임
    profileImageUrl: string;
    hasGitlabPersonalAccessToken: boolean;
  };
}
