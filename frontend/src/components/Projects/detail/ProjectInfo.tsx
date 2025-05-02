import styled from '@emotion/styled';

interface ProjectInfoProps {
  clientDir: string;
  serverDir: string;
  nodeVersion: string;
  jdkVersion: string;
  buildTool: string;
}

export function ProjectInfo({
  clientDir,
  serverDir,
  nodeVersion,
  jdkVersion,
  buildTool,
}: ProjectInfoProps) {
  return (
    <Card>
      <Row>
        <Label>Folder 구조</Label>
        <Value>모노</Value>
      </Row>
      <Row>
        <Label>Client</Label>
        <Value>
          <FolderIcon src="/assets/icons/ic_folder.svg" alt="folder" />
          {clientDir}
        </Value>
      </Row>
      <Row>
        <Label>Server</Label>
        <Value>
          <FolderIcon src="/assets/icons/ic_folder.svg" alt="folder" />
          {serverDir}
        </Value>
      </Row>
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
  background: #fff;
  padding: 2rem;
  border-radius: 1rem;
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
  ${({ theme }) => theme.fonts.Body4};
  color: ${({ theme }) => theme.colors.Black};
`;
const Value = styled.div`
  ${({ theme }) => theme.fonts.Body4};
  display: flex;
  align-items: center;
  gap: 0.5rem;
`;
const FolderIcon = styled.img`
  width: 1.5rem;
`;
