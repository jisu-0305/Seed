import styled from '@emotion/styled';

import { useProjectInfoStore } from '@/stores/projectStore';

import FileInput from './FileInput';
import TipItem from './TipItem';

export default function ServerInput() {
  const { stepStatus, setServerStatus } = useProjectInfoStore();
  const { server } = stepStatus;

  const handleIpChange = (index: number, value: string) => {
    const numeric = value.replace(/\D/g, '');

    if (numeric !== '' && parseInt(numeric, 10) > 255) return;

    const ipParts = server.ip ? server.ip.split('.') : ['', '', '', ''];
    ipParts[index] = numeric;

    setServerStatus({
      ip: ipParts.map((p) => p || '').join('.'),
      pem: server.pem,
    });
  };

  const handlePemChange = (file: File) => {
    if (file) {
      setServerStatus({ ip: server.ip, pem: !!file });
    }
  };

  const ipParts = server.ip ? server.ip.split('.') : ['', '', '', ''];

  return (
    <Container>
      <Title>
        IP 주소 <SubText>포트는 22번으로 고정됩니다.</SubText>
      </Title>

      <IpInputWrapper>
        {ipParts.map((val, i) => (
          <>
            <IpBox
              // eslint-disable-next-line react/no-array-index-key
              key={i}
              maxLength={3}
              value={val}
              onChange={(e) => handleIpChange(i, e.target.value)}
            />
            {i !== ipParts.length - 1 && '.'}
          </>
        ))}
      </IpInputWrapper>

      <Title>.pem 파일</Title>
      <FileInput
        handleFileChange={handlePemChange}
        accept=".pem"
        placeholder="key.pem"
      />

      <TipList>
        <TipItem text="IP 주소는 퍼블릭(탄력적) IP를 입력해주세요" help />
        <TipItem text="pem 파일은 AWS EC2에서 생성해주세요" help />
        <TipItem text="EC2에서 22, 80, 443 포트를 열어주세요" important help />
      </TipList>
    </Container>
  );
}

const Container = styled.div`
  width: 100%;
  padding: 4rem;
`;

const Title = styled.h3`
  width: fit-content;
  margin: 4rem 0 2rem;

  &:first-of-type {
    margin-top: 0;
  }

  ${({ theme }) => theme.fonts.Head4};
`;

const SubText = styled.span`
  ${({ theme }) => theme.fonts.Body6};
  color: ${({ theme }) => theme.colors.Gray3};

  margin-left: 1rem;
`;

const IpInputWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
`;

const IpBox = styled.input`
  width: 6rem;
  padding: 1rem;
  text-align: center;

  ${({ theme }) => theme.fonts.Body1};
  color: ${({ theme }) => theme.colors.Text};

  background-color: ${({ theme }) => theme.colors.InputBackground};
  border: 1px solid ${({ theme }) => theme.colors.InputStroke};
  border-radius: 1rem;
`;

const TipList = styled.ul`
  display: flex;
  flex-direction: column;
  gap: 1.2rem;

  margin-top: 6rem;
`;
