import styled from '@emotion/styled';
import { ReactNode } from 'react';

interface Props {
  children: ReactNode;
  onClick?: () => void;
  variant?: 'cancel' | 'next' | 'complete' | 'delete';
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

const Button = styled.button<{
  variant: 'cancel' | 'next' | 'complete' | 'delete';
}>`
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  align-items: center;

  padding: 1rem 1.5rem;
  border-radius: 0.8rem;
  min-width: 10rem;
  width: fit-content;

  ${({ theme }) => theme.fonts.Title5}
  color: ${({ variant, theme }) =>
    variant === 'next'
      ? theme.colors.BtnNextText
      : variant === 'cancel'
        ? theme.colors.BtnPrevText
        : theme.colors.White};

  background-color: ${({ variant, theme }) =>
    variant === 'next'
      ? theme.colors.BtnNextBg
      : variant === 'cancel'
        ? theme.colors.BtnPrevBg
        : variant === 'delete'
          ? theme.colors.Red2
          : theme.colors.Main_Carrot};

  &:disabled {
    cursor: not-allowed;
  }
`;
