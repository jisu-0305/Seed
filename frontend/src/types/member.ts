// 도메인 단위로 ts 파일 만들어서 type 선언하여 관리하기

export interface MemberListInfo {
  members: MemberInfo[];
}

export interface MemberInfo {
  memberId: number;
  memberEmail: string;
  githubId: string;
  nickname: string;
  message?: string;
  memberMessage: string;
  status: boolean;
  isMember: string;
}

export interface ProfileDataInfo {
  member: ProfileInfo;
}

export interface ProfileInfo {
  imageUrl: string;
  nickname: string;
  memberMessage?: string;
}
