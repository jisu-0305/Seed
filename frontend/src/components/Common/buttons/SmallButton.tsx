import styled from '@emotion/styled';
import { ReactNode } from 'react';

interface Props {
  children: ReactNode;
  onClick?: () => void;
  variant?: 'cancel' | 'next';
  disabled?: boolean;
}

export default function SmallButton({
  children,
  onClick,
  variant = 'cancel',
  disabled = false,
}: Props) {
  return (
    <Button onClick={onClick} disabled={disabled} variant={variant}>
      {children}
    </Button>
  );
}

const Button = styled.button<{ variant: 'cancel' | 'next' }>`
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  align-items: center;

  padding: 1rem 1.5rem;
  border-radius: 0.8rem;
  width: 10rem;

  ${({ theme }) => theme.fonts.Title5}
  color: ${({ theme }) => theme.colors.White};

  background-color: ${({ variant, theme }) =>
    variant === 'next' ? theme.colors.Gray0 : theme.colors.Gray3};

  &:disabled {
    cursor: not-allowed;
  }
`;
