import styled from '@emotion/styled';
import { useRouter } from 'next/navigation';
import React, { useState } from 'react';

import { registerPat } from '@/apis/user';
import TipItem from '@/components/Common/TipItem';
import { useModal } from '@/hooks/Common';
import { useThemeStore } from '@/stores/themeStore';

import ModalWrapper from '../Common/Modal/ModalWrapper';
import InformPATModal from '../Landing/InformPATModal';

export default function MyPage() {
  const router = useRouter();
  const { mode } = useThemeStore();
  const [token, setToken] = useState('');
  const patTip = useModal();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      await registerPat(token);
      router.replace('/dashboard');
    } catch (err) {
      console.error('PAT 등록 실패', err);
    }
  };

  if (mode === null) return null;

  return (
    <PageWrapper>
      <Form onSubmit={handleSubmit}>
        <FormLabel>PAT 토큰 수정하기</FormLabel>
        <InputGroup>
          <TokenInput
            type="text"
            value={token}
            onChange={(e) => setToken(e.target.value)}
          />
          <SubmitButton type="submit">수정하기</SubmitButton>
        </InputGroup>
        <TipArea>
          <TipItem
            text="GitLab에서 개인 액세스 토큰(PAT)을 발급받아주세요."
            help
            openModal={patTip.toggle}
          />
        </TipArea>
      </Form>

      <ModalWrapper isShowing={patTip.isShowing}>
        <InformPATModal
          isShowing={patTip.isShowing}
          handleClose={patTip.toggle}
        />
      </ModalWrapper>
    </PageWrapper>
  );
}

const PageWrapper = styled.div`
  padding: 7rem;
  margin: auto;
  max-width: 90rem;
`;

const TipArea = styled.div`
  margin-top: 1.5rem;
`;

const Form = styled.form`
  display: flex;
  flex-direction: column;
  padding: 0rem 10rem;
`;

const FormLabel = styled.label`
  ${({ theme }) => theme.fonts.Title3};
  margin-bottom: 1rem;
`;

const InputGroup = styled.div`
  display: flex;
  align-items: center;
  width: 50rem;
  gap: 1.5rem;
  margin-bottom: 1rem;
`;

const TokenInput = styled.input`
  flex: 1;
  padding: 1rem;
  ${({ theme }) => theme.fonts.Body4};
  color: ${({ theme }) => theme.colors.Text};
  border: 2px solid ${({ theme }) => theme.colors.BorderDefault};
  border-radius: 1rem;
  outline: none;

  &:focus {
    border-color: ${({ theme }) => theme.colors.Carrot2};
  }
`;

const SubmitButton = styled.button`
  padding: 1rem 2rem;
  ${({ theme }) => theme.fonts.Body4};
  color: ${({ theme }) => theme.colors.MenuText};
  background-color: ${({ theme }) => theme.colors.MenuBg};
  border: none;
  border-radius: 1rem;
  cursor: pointer;

  &:hover {
    background-color: ${({ theme }) => theme.colors.Main_Carrot};
  }
`;
