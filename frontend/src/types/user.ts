export interface MeResponse {
  success: boolean;
  message: string;
  data: {
    name: string; // 실제 이메일
    username: string; // 깃랩 유저네임
    avatarUrl: string;
  };
}
