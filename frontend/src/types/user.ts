export interface MeResponse {
  success: boolean;
  message: string;
  data: {
    userIdentifyId: string; // 실제 이메일
    userName: string; // 깃랩 유저네임
    avatarUrl: string;
  };
}

export interface User {
  id: number;
  name: string;
  avatarUrl: string;
}
