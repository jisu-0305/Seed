// 해당 경로에서 로딩 페이지가 필요한 경우 loading.tsx라고 작성하면 알아서 로딩시 처리해줌

'use client';

import styled from '@emotion/styled';

export default function Loading() {
  return <LoadingWrapper>Loading...</LoadingWrapper>;
}

const LoadingWrapper = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  font-size: 2rem;
  color: white;
`;
