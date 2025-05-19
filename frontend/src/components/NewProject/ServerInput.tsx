/* eslint-disable react/no-array-index-key */
import styled from '@emotion/styled';
import { useEffect, useRef } from 'react';

import { useModal } from '@/hooks/Common';
import { useProjectInfoStore } from '@/stores/projectStore';

import ModalWrapper from '../Common/Modal/ModalWrapper';
import TipItem from '../Common/TipItem';
import InformInboundModal from './Modal/InformInboundModal';
import InformIpModal from './Modal/InformIpModal';

export default function ServerInput() {
  const { stepStatus, setServerStatus, setOnNextValidate, setOnNextSuccess } =
    useProjectInfoStore();
  const { server } = stepStatus;

  // const { pemFile, setPemFile } = useProjectFileStore();

  // useEffect(() => {
  //   setServerStatus({
  //     ...server,
  //     pem: Boolean(pemFile),
  //     pemName: pemFile?.name ?? '',
  //   });
  // }, [pemFile]);

  // const pemTip = useModal();
  const ipTip = useModal();
  const inboundTip = useModal();

  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  const handleKeyDown = (
    e: React.KeyboardEvent<HTMLInputElement>,
    index: number,
  ) => {
    if (e.key === 'Backspace' && !e.currentTarget.value && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handleIpChange = (index: number, value: string) => {
    const numeric = value.replace(/\D/g, '');
    if (numeric.length > 3 || (numeric !== '' && parseInt(numeric, 10) > 255))
      return;

    const ipParts = server.ip ? server.ip.split('.') : ['', '', '', ''];
    ipParts[index] = numeric;

    setServerStatus({
      ip: ipParts.map((p) => p || '').join('.'),
      pem: server.pem,
      pemName: server.pemName,
    });

    if (numeric.length === 3 && index < 3) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handleIpPaste = (e: React.ClipboardEvent<HTMLInputElement>) => {
    const pasteData = e.clipboardData.getData('Text');
    const ipRegex = /^(\d{1,3}\.){3}\d{1,3}$/;

    if (!ipRegex.test(pasteData)) return;

    e.preventDefault();

    const parts = pasteData.split('.');
    setServerStatus({
      ip: parts.join('.'),
      pem: server.pem,
      pemName: server.pemName,
    });

    // 🧠 마지막 칸으로 커서 이동
    setTimeout(() => {
      inputRefs.current[3]?.focus();
    }, 0);
  };

  // const handlePemChange = (file: File) => {
  //   if (file) {
  //     setServerStatus({ ip: server.ip, pem: !!file, pemName: file.name });
  //     setPemFile(file);
  //   }
  // };

  // 유효성 검사
  const isFormValid = () => {
    const ipParts = server.ip.split('.');
    const isValidIp =
      ipParts.length === 4 && ipParts.every((part) => part !== '');

    // return isValidIp && !!server.pem;
    return isValidIp;
  };

  // next 버튼 핸들러
  useEffect(() => {
    setOnNextValidate(isFormValid);

    setOnNextSuccess(() => {
      inboundTip.toggle();
    });
  }, [server]);

  const ipParts = server.ip ? server.ip.split('.') : ['', '', '', ''];

  return (
    <>
      <Container>
        <Title>
          IP 주소 <SubText>포트는 22번으로 고정됩니다.</SubText>
        </Title>

        <IpInputWrapper>
          {ipParts.map((val, idx) => (
            <StBoxWrapper key={idx}>
              <IpBox
                key={idx}
                ref={(el) => {
                  inputRefs.current[idx] = el;
                }}
                maxLength={3}
                value={val}
                onChange={(e) => handleIpChange(idx, e.target.value)}
                onKeyDown={(e) => handleKeyDown(e, idx)}
                onPaste={idx === 0 ? handleIpPaste : undefined}
              />
              {idx !== ipParts.length - 1 && '.'}
            </StBoxWrapper>
          ))}
        </IpInputWrapper>

        {/* <Title>.pem 파일</Title>
        <FileInput
          handleFileChange={handlePemChange}
          accept=".pem"
          placeholder="key.pem"
          inputType="pem"
        /> */}

        <TipList>
          <TipItem
            text="IP 주소는 퍼블릭(탄력적) IP를 입력해주세요"
            help
            openModal={ipTip.toggle}
          />
          {/* <TipItem
            text="pem 파일은 AWS EC2에서 생성해주세요"
            help
            openModal={pemTip.toggle}
          /> */}
          <TipItem
            text="EC2에서 22, 80, 443 포트를 열어주세요"
            important
            help
            openModal={inboundTip.toggle}
          />
        </TipList>
      </Container>
      <ModalWrapper isShowing={ipTip.isShowing || inboundTip.isShowing}>
        <InformIpModal isShowing={ipTip.isShowing} handleClose={ipTip.toggle} />
        {/* <InformPemKeyModal
          isShowing={pemTip.isShowing}
          handleClose={pemTip.toggle}
        /> */}
        <InformInboundModal
          isShowing={inboundTip.isShowing}
          handleClose={inboundTip.toggle}
        />
      </ModalWrapper>
    </>
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

const StBoxWrapper = styled.div``;

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
