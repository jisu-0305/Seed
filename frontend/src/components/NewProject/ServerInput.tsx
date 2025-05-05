import styled from '@emotion/styled';
import { ChangeEvent, useState } from 'react';

import { useProjectInfoStore } from '@/stores/projectStore';

export default function ServerInput() {
  const [pemFile, setPemFile] = useState<File | null>(null);

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

  const handlePemChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];

    if (file) {
      setPemFile(file);
      setServerStatus({ ip: server.ip, pem: file });
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
      <PemInputWrapper>
        <PemInput
          type="text"
          readOnly
          value={pemFile?.name || ''}
          placeholder="key.pem"
        />
        <UploadLabel htmlFor="pem-upload">
          <UploadIcon src="/assets/icons/ic_upload.svg" alt="upload" />
        </UploadLabel>
        <HiddenInput
          type="file"
          //   accept=".pem"
          id="pem-upload"
          onChange={handlePemChange}
        />
      </PemInputWrapper>

      <TipList>
        <TipItem>
          <TipLabel>TIP</TipLabel>
          IP 주소는 퍼블릭(탄력적) IP를 입력해주세요
          <IcIcon src="/assets/icons/ic_help.svg" alt="help_icon" />
        </TipItem>
        <TipItem>
          <TipLabel>TIP</TipLabel>
          pem 파일은 AWS EC2에서 생성해주세요
          <IcIcon src="/assets/icons/ic_help.svg" alt="help_icon" />
        </TipItem>
        <TipItem important>
          <TipLabel important>TIP</TipLabel>
          EC2에서 22, 80, 443 포트를 열어주세요
          <IcIcon src="/assets/icons/ic_help_important.svg" alt="help_icon" />
        </TipItem>
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

  ${({ theme }) => theme.fonts.Title5};
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
`;

const IpBox = styled.input`
  width: 6rem;
  padding: 1rem;
  text-align: center;

  ${({ theme }) => theme.fonts.Body1};

  background-color: ${({ theme }) => theme.colors.LightGray3};
  border: 1px solid ${({ theme }) => theme.colors.LightGray2};
  border-radius: 1rem;
`;

const PemInputWrapper = styled.div`
  display: flex;
  align-items: center;

  max-width: 25rem;
  padding: 1rem 2rem;

  background-color: ${({ theme }) => theme.colors.LightGray3};
  border: 1px solid ${({ theme }) => theme.colors.LightGray2};
  border-radius: 1rem;
`;

const PemInput = styled.input`
  flex: 1;
  ${({ theme }) => theme.fonts.Body1};

  cursor: default;
`;

const UploadIcon = styled.img`
  cursor: pointer;
`;

const UploadLabel = styled.label``;

const HiddenInput = styled.input`
  display: none;
`;

const TipList = styled.ul`
  display: flex;
  flex-direction: column;
  gap: 1.2rem;

  margin-top: 6rem;
`;

const TipItem = styled.li<{ important?: boolean }>`
  width: 90%;
  max-width: 40rem;

  display: flex;
  justify-content: space-between;
  align-items: center;

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
