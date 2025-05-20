import styled from '@emotion/styled';
import React, { useState } from 'react';

import FileInput from '@/components/Common/FileInput';
import SmallModal from '@/components/Common/Modal/SmallModal';
import ModalTipItem from '@/components/Common/ModalTipItem';
import InformPemKeyModal from '@/components/NewProject/Modal/InformPemKeyModal';
import { useModal } from '@/hooks/Common';
import { EC2Config } from '@/types/config';

interface PemModalProps {
  isShowing: boolean;
  handleClose: () => void;
  onSubmit: (config: EC2Config) => void;
}

const PemModal: React.FC<PemModalProps> = ({
  isShowing,
  handleClose,
  onSubmit,
}) => {
  const [pemFile, setPemFile] = useState<File | null>(null);
  const pemTip = useModal();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!pemFile) {
      alert('pem 파일을 업로드해주세요!');
      return;
    }

    onSubmit({ pem: pemFile });
    setPemFile(null);
  };

  const handlePemChange = (file: File) => {
    if (file) {
      setPemFile(file);
    }
  };

  return (
    <SmallModal
      title="EC2 세팅하기"
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

        <SubmitButton type="submit">설정하기</SubmitButton>

        <InformPemKeyModal
          isShowing={pemTip.isShowing}
          handleClose={pemTip.toggle}
        />
        <ModalTipItem
          text="pem 파일은 AWS EC2에서 생성해주세요"
          help
          openModal={pemTip.toggle}
        />
      </Form>
    </SmallModal>
  );
};

export default PemModal;

// 스타일 컴포넌트는 기존 그대로
const Form = styled.form`
  display: flex;
  flex-direction: column;
  gap: 2rem;
  padding: 2rem;
`;

const Label = styled.label`
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 2rem;
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
  opacity: ${({ disabled }) => (disabled ? 0.5 : 1)};
`;
