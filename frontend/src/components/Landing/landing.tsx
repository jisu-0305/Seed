import styled from '@emotion/styled';
import { useRouter } from 'next/navigation';

export default function Landing() {
  const router = useRouter();

  const goLogin = () => {
    router.push('/login');
  };

  return (
    <Container>
      <Cactus src="/assets/cactus.png" alt="Cactus" />
      <LogoText
        src="/assets/icons/ic_logoText_white.svg"
        alt="SEED text logo"
      />
      <SubTitle>배포가 어려운 당신을 위한 솔루션</SubTitle>
      <StartButton onClick={goLogin}>
        시작하기
        <Arrow src="/assets/icons/ic_arrow_start.svg" alt="→" />
      </StartButton>
      <Footer>
        © 2025 SSAFY12. All Rights Reserved. Made with love by SEED!
      </Footer>
    </Container>
  );
}

const Container = styled.div`
  background-color: ${({ theme }) => theme.colors.Black1};
  color: ${({ theme }) => theme.colors.White};
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rem;
`;

const Cactus = styled.img`
  width: 25rem;
  height: auto;
`;

const LogoText = styled.img`
  width: 35rem;
  height: auto;
`;

const SubTitle = styled.p`
  color: ${({ theme }) => theme.colors.Main_Carrot};
  ${({ theme }) => theme.fonts.Title1};
`;

const StartButton = styled.button`
  display: flex;
  align-items: center;
  padding: 1rem 15rem;
  ${({ theme }) => theme.fonts.Title3};
  color: ${({ theme }) => theme.colors.White};
  background: transparent;
  border: 2px solid ${({ theme }) => theme.colors.Main_Carrot};
  border-radius: 1.6rem;
  cursor: pointer;

  &:hover {
    background: ${({ theme }) => theme.colors.Main_Carrot};
  }
`;

const Arrow = styled.img`
  width: 2.5rem;
  height: auto;
  margin-left: 1rem;
`;

const Footer = styled.footer`
  position: absolute;
  bottom: 1rem;
  font-size: 1rem;
  color: #888;
`;
