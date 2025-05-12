import styled from '@emotion/styled';
import React, { useState } from 'react';

import TipItem from '@/components/Common/TipItem';
import { useModal } from '@/hooks/Common';

import ModalWrapper from '../Common/Modal/ModalWrapper';
import InformPATModal from './InformPATModal';

export default function OnBoarding() {
  const [token, setToken] = useState('');
  const patTip = useModal();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // TODO: 토큰 등록 로직 호출
    console.log('등록할 토큰:', token);
  };

  return (
    <PageWrapper>
      <Header>
        <Logo src="/assets/cactus.png" alt="SEED Logo" />
        <TitleGroup>
          <MainTitle>SEED</MainTitle>
          <SubTitle>배포가 어려운 당신을 위한 솔루션</SubTitle>
        </TitleGroup>
      </Header>

      <Content>
        <p>환영합니다! SEED 서비스에 오신 것을 환영합니다.</p>
        <p>
          원활한 서비스 이용을 위해 GitLab 개인 액세스 토큰(PAT)을 등록해주세요.
        </p>

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
            placeholder="glet-s4Ks4marvgnYDPpStmDgv"
            value={token}
            onChange={(e) => setToken(e.target.value)}
          />
          <SubmitButton type="submit">등록하기 &gt;</SubmitButton>
        </InputGroup>
        <NoteList>
          <li>
            발급받은 토큰은 SEED 서비스를 위한 액세스 권한에만 사용됩니다.
          </li>
          <li>토큰은 외부에 유출되지 않도록 안전하게 보관해주세요.</li>
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
  padding: 4rem;
  margin: 0 auto;
  max-width: 100rem;
  font-family: 'Noto Sans KR', sans-serif;
  color: #333;
`;

const Header = styled.header`
  display: flex;
  align-items: center;
  gap: 2rem;
  margin-bottom: 3rem;
`;

const Logo = styled.img`
  width: 8rem;
  height: 8rem;
  object-fit: contain;
`;

const TitleGroup = styled.div`
  display: flex;
  flex-direction: column;
`;

const MainTitle = styled.h1`
  margin: 0;
  font-size: 4rem;
  font-weight: bold;
`;

const SubTitle = styled.h2`
  margin: 0.5rem 0 0;
  font-size: 1.5rem;
  color: #ffac2f;
`;

const Content = styled.section`
  line-height: 1.6;
  font-size: 1.125rem;
  margin-bottom: 3rem;

  p + p {
    margin-top: 1rem;
  }
`;

const TipArea = styled.div`
  margin-top: 1.5rem;
`;

const Form = styled.form`
  display: flex;
  flex-direction: column;
`;

const FormLabel = styled.label`
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1rem;
`;

const InputGroup = styled.div`
  display: flex;
  gap: 1rem;
  align-items: center;
  margin-bottom: 1rem;
`;

const TokenInput = styled.input`
  flex: 1;
  padding: 1rem;
  font-size: 1rem;
  border: 2px solid #ffac2f;
  border-radius: 0.5rem;
  outline: none;

  &:focus {
    border-color: #e6952b;
  }
`;

const SubmitButton = styled.button`
  padding: 1rem 2rem;
  font-size: 1rem;
  background-color: #ffac2f;
  color: #fff;
  border: none;
  border-radius: 0.5rem;
  cursor: pointer;

  &:hover {
    background-color: #e6952b;
  }
`;

const NoteList = styled.ul`
  margin: 0;
  padding-left: 1.25rem;
  font-size: 0.875rem;
  color: #666;

  li + li {
    margin-top: 0.5rem;
  }
`;
