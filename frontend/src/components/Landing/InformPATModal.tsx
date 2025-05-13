import styled from '@emotion/styled';

import MainModal from '@/components/Common/Modal/MainModal';

interface Props {
  isShowing: boolean;
  handleClose: () => void;
}

const InformPATModal = ({ isShowing, handleClose }: Props) => {
  return (
    isShowing && (
      <MainModal
        title="GitLab 토큰 발급받기 (PAT)"
        isShowing={isShowing}
        link="https://lab.ssafy.com/"
        handleClose={handleClose}
      >
        <StModalWrapper>
          <DetailText>
            <IcIcon src="/assets/icons/ic_check.svg" alt="check" />
            GitLab → 우측 상단 프로필 → Preferences → Access Tokens
          </DetailText>

          <ImageContainer>
            <Image src="/assets/informModal/token.png" alt="PAT example" />
          </ImageContainer>

          <DescribeText>
            SEED가 여러분의 저장소와 안전하게 연동되기 위해 PAT가 필요해요.
          </DescribeText>

          <InstructionText>
            <IcIcon src="/assets/icons/ic_check.svg" alt="check" />
            유효기간 없음 (권장) → api 선택 → Create personal access token
          </InstructionText>

          <StCautionWrapper>
            <IcIcon src="/assets/icons/ic_caution.svg" alt="caution" />
            생성된 토큰은 한 번만 표시되므로 복사해두세요.
          </StCautionWrapper>
        </StModalWrapper>
      </MainModal>
    )
  );
};

export default InformPATModal;

const StModalWrapper = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  & > ul {
    align-self: flex-start;
  }
`;

const DescribeText = styled.div`
  color: ${({ theme }) => theme.colors.Black1};
  ${({ theme }) => theme.fonts.Title5};

  margin-bottom: 1rem;
`;

const ImageContainer = styled.div`
  width: 100%;

  margin-bottom: 3.5rem;
  border: 1px solid ${({ theme }) => theme.colors.Gray3};
  border-radius: 1.5rem;
`;

const Image = styled.img`
  width: 90%;
  margin: 2rem;
`;

const InstructionText = styled.div`
  display: flex;
  align-items: center;

  color: ${({ theme }) => theme.colors.Black1};
  ${({ theme }) => theme.fonts.Head4};

  margin-bottom: 1.8rem;
`;

const DetailText = styled.div`
  display: flex;
  align-items: center;

  color: ${({ theme }) => theme.colors.Black1};
  ${({ theme }) => theme.fonts.Head3};

  margin-bottom: 1.8rem;
`;

const IcIcon = styled.img`
  width: 3rem;

  margin-right: 0.5rem;
`;

const StCautionWrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;

  width: 100%;
  padding: 1rem;

  color: ${({ theme }) => theme.colors.Black1};
  ${({ theme }) => theme.fonts.Head5};

  background-color: ${({ theme }) => theme.colors.Red4};
  border-radius: 1rem;
`;
