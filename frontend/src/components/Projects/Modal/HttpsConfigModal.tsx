import styled from '@emotion/styled';
import React, { useState } from 'react';

import FileInput from '@/components/Common/FileInput';
import SmallModal from '@/components/Common/Modal/SmallModal';
import ModalTipItem from '@/components/Common/ModalTipItem';
import InformPemKeyModal from '@/components/NewProject/Modal/InformPemKeyModal';
import { useModal } from '@/hooks/Common';
import { HttpsConfig } from '@/types/config';

interface HttpsModalProps {
  isShowing: boolean;
  handleClose: () => void;
  onSubmit: (config: HttpsConfig) => void;
}

const HttpsConfigModal: React.FC<HttpsModalProps> = ({
  isShowing,
  handleClose,
  onSubmit,
}) => {
  const [domain, setDomain] = useState('');
  const [email, setEmail] = useState('');
  const [pemFile, setPemFile] = useState<File | null>(null);
  const pemTip = useModal();

  const sanitizeDomain = (rawDomain: string) => {
    return rawDomain
      .trim()
      .replace(/^https?:\/\//, '') // http:// 또는 https:// 제거
      .replace(/\/$/, ''); // 끝에 / 제거
  };

  const cleanedDomain = sanitizeDomain(domain);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!pemFile) {
      alert('pem 파일을 업로드해주세요!');
      return;
    }

    onSubmit({ domain: cleanedDomain, email, pem: pemFile });
  };

  const handlePemChange = (file: File) => {
    if (file) {
      setPemFile(file);
    }
  };

  return (
    <SmallModal
      title="HTTPS 세팅 정보"
      isShowing={isShowing}
      handleClose={handleClose}
    >
      <Form onSubmit={handleSubmit}>
        <Label>
          .pem 파일
          <FileInput
            handleFileChange={handlePemChange}
            accept=".pem"
            placeholder="key.pem"
            inputType="pem"
          />
        </Label>
        <Label>
          Domain
          <Input
            type="text"
            value={domain}
            onChange={(e) => setDomain(e.target.value)}
            placeholder="example.com 으로 입력해주세요"
            required
          />
        </Label>

        <Label>
          Email
          <Input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="you@example.com"
            required
          />
        </Label>

        <SubmitButton type="submit">HTTPS 시작하기</SubmitButton>

        <ModalTipItem
          text="pem 파일은 AWS EC2에서 생성해주세요"
          help
          openModal={pemTip.toggle}
        />
        <InformPemKeyModal
          isShowing={pemTip.isShowing}
          handleClose={pemTip.toggle}
        />
        <ModalTipItem text="서비스의 DNS 이름과 소유자의 이메일 주소를 입력해주세요" />
      </Form>
    </SmallModal>
  );
};

export default HttpsConfigModal;

const Form = styled.form`
  display: flex;
  flex-direction: column;
  gap: 2rem;
  padding: 2rem;
`;

const Label = styled.label`
  display: flex;
  flex-direction: column;
  ${({ theme }) => theme.fonts.Title5};
  color: ${({ theme }) => theme.colors.Black};
`;

const SubmitButton = styled.button`
  align-self: flex-end;
  ${({ theme }) => theme.fonts.Body2};
  padding: 0.8rem 1.2rem;
  border-radius: 1rem;
  background-color: ${({ theme }) => theme.colors.Black};
  color: ${({ theme }) => theme.colors.White};
  cursor: pointer;
`;

const Input = styled.input`
  width: 100%;
  height: 2.5rem;
  margin-top: 0.5rem;
  padding: 0.8rem;
  ${({ theme }) => theme.fonts.Body1};
  color: ${({ theme }) => theme.colors.Black};
  border: 1px solid ${({ theme }) => theme.colors.BorderDefault};
  border-radius: 0.5rem;
`;
