// src/components/LoadingSpinner.tsx
import { keyframes } from '@emotion/react';
import styled from '@emotion/styled';
import React from 'react';

interface LoadingSpinnerProps {
  /** 로딩 메시지 (생략 시 스피너만 표시) */
  message?: string;
}

export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({ message }) => (
  <Container>
    <Spinner />
    {message && <Text>{message}</Text>}
  </Container>
);

const spin = keyframes`
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
`;

const Container = styled.div`
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 98%;
  gap: 0.5rem;
  padding: 0.5rem;

  /* 부모 overflow 영향 없이 보이기 위해 */
  position: relative;
  z-index: 10;
`;

const Spinner = styled.div`
  width: 3rem;
  height: 3rem;
  border: 0.5rem solid ${({ theme }) => theme.colors.BorderDefault};
  border-top: 0.5rem solid ${({ theme }) => theme.colors.Main_Carrot};
  border-radius: 50%;
  animation: ${spin} 0.8s linear infinite;
`;

const Text = styled.div`
  ${({ theme }) => theme.fonts.Body4};
  color: ${({ theme }) => theme.colors.Text};
`;
