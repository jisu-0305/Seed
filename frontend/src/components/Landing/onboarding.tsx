import styled from '@emotion/styled';
import { useRouter } from 'next/navigation';
import React, { useState } from 'react';

import { registerPat } from '@/apis/user';
import TipItem from '@/components/Common/TipItem';
import { useModal } from '@/hooks/Common';
import { useThemeStore } from '@/stores/themeStore';

import ModalWrapper from '../Common/Modal/ModalWrapper';
import InformPATModal from './InformPATModal';

export default function OnBoarding() {
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
      <Header>
        <Logo src="/assets/cactus.png" alt="SEED Logo" />
        <TitleGroup>
          <LogoText
            src={
              mode === 'light'
                ? '/assets/icons/ic_logoText.svg'
                : '/assets/icons/ic_logoText_white.svg'
            }
            alt="logoText"
          />
          <SubTitle>배포가 어려운 당신을 위한 솔루션</SubTitle>
        </TitleGroup>
      </Header>

      <Content>
        <ContentText>
          환영합니다! SEED 서비스에 오신 것을 환영합니다.
        </ContentText>
        <ContentText>
          원활한 서비스 이용을 위해 GitLab 개인 액세스 토큰(PAT)을 등록해주세요.
        </ContentText>

        <TipArea>
          <TipItem
            text="GitLab에서 개인 액세스 토큰(PAT)을 발급받아주세요."
            help
            openModal={patTip.toggle}
          />
        </TipArea>
      </Content>

      <Form onSubmit={handleSubmit}>
        <FormLabel>토큰 등록하기</FormLabel>
        <InputGroup>
          <TokenInput
            type="text"
            placeholder="vgnYDPpStmDgv"
            value={token}
            onChange={(e) => setToken(e.target.value)}
          />
          <SubmitButton type="submit">등록하기</SubmitButton>
        </InputGroup>
        <NoteList>
          발급받은 토큰은 SEED 서비스를 위한 액세스 권한에만 사용됩니다.
          <br />
          토큰은 외부에 유출되지 않도록 안전하게 보관해주세요.
        </NoteList>
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
  margin: auto;
  max-width: 90rem;
  height: calc(100vh - 7rem);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
`;

const Header = styled.header`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2rem;
`;

const Logo = styled.img`
  width: 20rem;
  height: 20rem;
  object-fit: contain;
`;

const TitleGroup = styled.div`
  display: flex;
  flex-direction: column;
`;

const LogoText = styled.img`
  height: 10rem;

  padding-top: 0.2rem;
`;

const SubTitle = styled.h2`
  margin: 1.5rem 0 0;
  ${({ theme }) => theme.fonts.EnTitle1};
  color: ${({ theme }) => theme.colors.Main_Carrot};
`;

const Content = styled.div`
  padding: 0rem 10rem;
  margin-bottom: 5rem;
  margin-top: 5rem;
`;

const ContentText = styled.div`
  ${({ theme }) => theme.fonts.Head7};
  margin-top: 1rem;
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
  border: 2px solid ${({ theme }) => theme.colors.Main_Carrot};
  border-radius: 1rem;
  outline: none;

  &:focus {
    border-color: ${({ theme }) => theme.colors.Carrot2};
  }
`;

const SubmitButton = styled.button`
  padding: 1rem 2rem;
  ${({ theme }) => theme.fonts.Body4};
  color: ${({ theme }) => theme.colors.White};
  background-color: ${({ theme }) => theme.colors.Main_Carrot};
  border: none;
  border-radius: 1rem;
  cursor: pointer;

  &:hover {
    background-color: ${({ theme }) => theme.colors.Carrot2};
  }
`;

const NoteList = styled.div`
  margin: 0;
  padding-left: 1.25rem;
  ${({ theme }) => theme.fonts.Body4};
  color: ${({ theme }) => theme.colors.Gray3};
`;
