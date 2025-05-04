import axios from 'axios';

const client = axios.create({
  baseURL: process.env.NEXT_PUBLIC_APP_IP,
  headers: {
    'Content-type': 'application/json',
    'Access-Control-Allow-Origin': process.env.NEXT_PUBLIC_APP_IP,
  },
});

client.interceptors.request.use((config) => {
  const accessToken = localStorage.getItem('accessToken');
  if (!accessToken) return config;

  if (config.url?.endsWith('/token/refresh')) {
    // eslint-disable-next-line no-param-reassign
    config.headers.Refresh = `${localStorage.getItem('refreshToken')}`;
  } else {
    // eslint-disable-next-line no-param-reassign
    config.headers.Authorization = `Bearer ${accessToken}`;
  }

  // console.log('Axios 요청 헤더:', config.headers);
  return config;
});

// client.interceptors.response.use(
//   function (res) {
//     return res;
//   },
//   async (err) => {
//     const { config, response } = err;

//     // 엑세스 토큰 만료시
//     if (response.data.status === 401) {
//       if (response.data.message === 'Unauthorized') {
//         const originalRequest = config;

//         // 새로운 엑세스 토큰 저장
//         const data = null;
//         await client.post(`/token/refresh`, data).then((res) => {
//           try {
//             if (res.status === 200) {
//               const accessToken = res.headers.authorization;
//               localStorage.setItem('accessToken', accessToken);
//               originalRequest.headers.Authorization = `Bearer ${accessToken}`;
//             }
//             return axios(originalRequest);
//           } catch (error) {
//             console.log('토큰 재발급 실패');
//             console.log(error);
//             window.location.href = '/';
//             throw error;
//           }
//         });
//       }
//       // 리프레시 만료시
//       if (
//         response.data.message ===
//         'Refresh 토큰이 만료되었습니다. 로그인이 필요합니다.'
//       ) {
//         localStorage.removeItem('accessToken');
//         localStorage.removeItem('refreshToken');
//         console.log('리프레시 만료');
//         window.location.href = '/';
//         return;
//       }
//     }

//     console.log('response error', err);
//   },
// );

const ai = axios.create({
  baseURL: process.env.NEXT_PUBLIC_AI_IP,
});

const mainGetFetcher = (url: string) => client.get(url).then((res) => res.data);

const aiGetFetcher = (url: string) => ai.get(url).then((res) => res.data);

export { ai, aiGetFetcher, client, mainGetFetcher };
