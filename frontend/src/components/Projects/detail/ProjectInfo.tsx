import styled from '@emotion/styled';

import { useThemeStore } from '@/stores/themeStore';

interface ProjectInfoProps {
  folder: string;
  clientDir: string;
  serverDir: string;
  nodeVersion: string;
  jdkVersion: string;
  buildTool: string;
}

export function ProjectInfo({
  folder,
  clientDir,
  serverDir,
  nodeVersion,
  jdkVersion,
  buildTool,
}: ProjectInfoProps) {
  const { mode } = useThemeStore();

  return (
    <Card>
      <HeaderRow>
        <Label>폴더 구조</Label>
        <MonoValue>{folder}</MonoValue>
      </HeaderRow>

      <Row>
        <Label>Client</Label>
        <Value>
          <FolderIcon
            src={`/assets/icons/ic_folder_${mode}.svg`}
            alt="folder"
          />
          {clientDir}
        </Value>
      </Row>
      <Row>
        <Label>Server</Label>
        <Value>
          <FolderIcon
            src={`/assets/icons/ic_folder_${mode}.svg`}
            alt="folder"
          />
          {serverDir}
        </Value>
      </Row>

      <Divider />

      <Row>
        <Label>Node</Label>
        <Value>{nodeVersion}</Value>
      </Row>
      <Row>
        <Label>JDK</Label>
        <Value>{jdkVersion}</Value>
      </Row>
      <Row>
        <Label>빌드도구</Label>
        <Value>{buildTool}</Value>
      </Row>
    </Card>
  );
}

const Card = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: ${({ theme }) => theme.colors.DetailFieldBg};
  width: 22rem;
  height: 25rem;
  padding: 1rem 2rem;
  border: 0.15rem solid ${({ theme }) => theme.colors.DetailBorder1};
  border-radius: 1.5rem;
`;

const HeaderRow = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.2rem;
`;

const Row = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;

  &:last-of-type {
    margin-bottom: 0;
  }
`;

const Label = styled.div`
  ${({ theme }) => theme.fonts.Head5};
  color: ${({ theme }) => theme.colors.Text};
`;

const MonoValue = styled.div`
  ${({ theme }) => theme.fonts.Body3};
`;

const Value = styled.div`
  ${({ theme }) => theme.fonts.Body3};
  display: flex;
  align-items: center;
  gap: 0.5rem;
`;

const FolderIcon = styled.img`
  width: 2rem;
`;

const Divider = styled.hr`
  border: none;
  border-top: 0.15rem solid ${({ theme }) => theme.colors.DetailBorder1};
  margin: 2rem 0;
`;
