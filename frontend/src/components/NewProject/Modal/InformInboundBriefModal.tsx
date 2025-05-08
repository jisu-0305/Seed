import styled from '@emotion/styled';

import MainModal from '@/components/Common/Modal/MainModal';

interface Props {
  isShowing: boolean;
  handleClose: () => void;
}

const InformInboundBriefModal = ({ isShowing, handleClose }: Props) => {
  return (
    isShowing && (
      <MainModal
        title="EC2 보안 그룹 설정"
        isShowing={isShowing}
        link="https://docs.aws.amazon.com/ko_kr/AWSEC2/latest/UserGuide/changing-security-group.html#add-remove-security-group-rules"
        handleClose={handleClose}
      >
        <StModalWrapper>
          <DescribeText>
            보안 그룹의 인바운드 규칙은 EC2 인스턴스로 들어오는 트래픽을
            제어해요.
          </DescribeText>
          <Image src="/assets/informModal/inbound.png" alt="pem.key example" />
          <DetailText>
            <IcIcon src="/assets/icons/ic_check.svg" alt="check" />
            네트워크 및 보안 → 보안 그룹 → 인바운드 규칙 → 인바운드 규칙 편집
          </DetailText>
          <StCautionWrapper>
            <IcIcon src="/assets/icons/ic_caution.svg" alt="caution" />
            어플리케이션에 접속하기 위해 해당 포트를 EC2 보안 그룹에서
            열어주세요.
          </StCautionWrapper>
        </StModalWrapper>
      </MainModal>
    )
  );
};

export default InformInboundBriefModal;

const StModalWrapper = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
`;

const DescribeText = styled.div`
  color: ${({ theme }) => theme.colors.Black1};
  ${({ theme }) => theme.fonts.Title5};

  margin-bottom: 1rem;
`;

const Image = styled.img`
  width: 90%;

  margin-bottom: 3.5rem;
`;

const DetailText = styled.div`
  display: flex;
  align-items: center;

  color: ${({ theme }) => theme.colors.Black1};
  ${({ theme }) => theme.fonts.Head3};
  font-size: 1.7rem;

  margin-bottom: 1.8rem;
`;

const IcIcon = styled.img`
  width: 3rem;

  margin-right: 0.5rem;
`;

const StCautionWrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-evenly;

  width: 100%;
  padding: 1rem 0;

  color: ${({ theme }) => theme.colors.Black1};
  ${({ theme }) => theme.fonts.Head5};

  background-color: ${({ theme }) => theme.colors.Red4};
  border-radius: 1rem;
`;
