// zustand store 예시
// 페이지 or 도메인 단위로 나눠서 쓰자!
// import { create } from 'zustand';

// //타입 선언
// interface UserState {
//   userName: string;
//   isLoggedIn: boolean;
//   login: (name: string) => void;
//   logout: () => void;
// }

// export const useUserStore = create<UserState>((set) => ({
//   userName: '',
//   isLoggedIn: false,
//   login: (name) => set({ userName: name, isLoggedIn: true }),
//   logout: () => set({ userName: '', isLoggedIn: false }),
// }));
