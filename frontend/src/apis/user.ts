// useQuery: GET 요청
// useMutation: POST,PUT,DELETE 요청
// 캐싱, refresh가 필요없다고 판단되는 일회성 요청은 axios로만 작성

// import { useQuery, useMutation } from '@tanstack/react-query';
// import axios from 'axios';
// import { queryClient } from '@/libs/queryClient';

// // api 요청 함수
// const fetchUserInfo = async () => {
//   const { data } = await axios.get('/api/user-info');
//   return data;
// };

// // hook
// export const useUserInfo = () => {
//   return useQuery({
//     queryKey: ['userInfo'],
//     queryFn: fetchUserInfo,
//   });
// };

// const loginUser = async (payload: { email: string; password: string }) => {
//   const { data } = await axios.post('/api/login', payload);
//   return data;
// };

// // 로그인용 커스텀 훅
// export const useLoginMutation = () => {
//   return useMutation({
//     mutationFn: loginUser,
//     onSuccess: async () => {
//       console.log('로그인 성공');
//       // 로그인 성공 후 유저 정보 다시 가져오기
//       await queryClient.invalidateQueries({ queryKey: ['userInfo'] });
//     },
//     onError: (error) => {
//       console.error('로그인 실패', error);
//     },
//   });
// };
