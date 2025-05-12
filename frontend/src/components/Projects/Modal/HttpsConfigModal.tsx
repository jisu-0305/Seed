import styled from '@emotion/styled';
import React, { useState } from 'react';

import SmallModal from '@/components/Common/Modal/SmallModal';
import ModalTipItem from '@/components/Common/ModalTipItem';
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

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({ domain, email });
  };

  return (
    <SmallModal
      title="HTTPS 세팅 정보"
      isShowing={isShowing}
      handleClose={handleClose}
    >
      <Form onSubmit={handleSubmit}>
        <Label>
          Domain
          <Input
            type="text"
            value={domain}
            onChange={(e) => setDomain(e.target.value)}
            placeholder="example.com"
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

        <SubmitButton type="submit">설정하기</SubmitButton>
        <ModalTipItem text="서비스의 DNS와 소유자의 이메일 주소를 입력해주세요" />
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
  color: ${({ theme }) => theme.colors.Text};
  border: 1px solid ${({ theme }) => theme.colors.BorderDefault};
  border-radius: 0.5rem;
`;
