// src/components/Common/ErrorMessage.tsx
import styled from '@emotion/styled';
import React from 'react';

const StyledErrorMessage = styled.div`
  padding: 2rem;
  text-align: center;
  ${({ theme }) => theme.fonts.Body1};
  color: ${({ theme }) => theme.colors.Red1};
  border-radius: 0.5rem;
`;

interface ErrorMessageProps {
  children: React.ReactNode;
}

export default function ErrorMessage({ children }: ErrorMessageProps) {
  return <StyledErrorMessage>{children}</StyledErrorMessage>;
}
