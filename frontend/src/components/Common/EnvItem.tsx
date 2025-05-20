import styled from '@emotion/styled';

import { useThemeStore } from '@/stores/themeStore';

interface EnvItemProps {
  envs: string[];
}

export default function EnvItem({ envs }: EnvItemProps) {
  const { mode } = useThemeStore();

  if (mode === null) return null;

  return (
    <StWrapper>
      <EnvLine>
        {envs.map((env) => (
          <EnvTag key={env}>{env}</EnvTag>
        ))}
      </EnvLine>
    </StWrapper>
  );
}

const StWrapper = styled.li<{ important?: boolean }>`
  width: 100%;
  max-width: fit-content;

  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  padding-left: 4rem;

  ${({ theme }) => theme.fonts.Body3}

  color: ${({ important, theme }) =>
    important ? theme.colors.Red1 : theme.colors.Text};
`;

const EnvLine = styled.div`
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
  margin-top: 0.5rem;
`;

const EnvTag = styled.span`
  padding: 0.4rem 0.8rem;
  border-radius: 1.2rem;
  background-color: ${({ theme }) => theme.colors.MenuBg};
  color: ${({ theme }) => theme.colors.MenuText};
  ${({ theme }) => theme.fonts.Body5};
  white-space: nowrap;
`;
