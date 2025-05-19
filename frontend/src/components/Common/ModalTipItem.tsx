import styled from '@emotion/styled';

import { useThemeStore } from '@/stores/themeStore';

interface TipItemProps {
  text: string;
  important?: boolean;
  help?: boolean;
  openModal?: () => void;
}

export default function ModalTipItem({
  text,
  important = false,
  help = false,
  openModal,
}: TipItemProps) {
  const { mode } = useThemeStore();

  if (mode === null) return null;

  return (
    <StWrapper important={important}>
      <TipLabel important={important}>TIP</TipLabel>
      {text}

      {help && (
        <IcIcon
          onClick={openModal}
          src={
            important
              ? `/assets/icons/ic_help_important.svg`
              : `/assets/icons/ic_help_light.svg`
          }
          alt="help_icon"
        />
      )}
    </StWrapper>
  );
}

const StWrapper = styled.li<{ important?: boolean }>`
  width: 100%;
  max-width: fit-content;

  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;

  ${({ theme }) => theme.fonts.Body3}

  color: ${({ important, theme }) =>
    important ? theme.colors.Red1 : theme.colors.Black};
`;

const TipLabel = styled.span<{ important?: boolean }>`
  padding: 0.5rem 1rem;

  color: ${({ theme }) => theme.colors.White};
  ${({ theme }) => theme.fonts.Title6}

  background-color: ${({ important, theme }) =>
    important ? theme.colors.Red1 : theme.colors.Black};
  border-radius: 1.5rem;
`;

const IcIcon = styled.img`
  cursor: pointer;
`;
