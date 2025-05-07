import styled from '@emotion/styled';

import MainModal from '@/components/Common/Modal/MainModal';

interface Props {
  isShowing: boolean;
  handleClose: () => void;
}

const InformPemKeyModal = ({ isShowing, handleClose }: Props) => {
  return (
    isShowing && (
      <MainModal
        title="키 페어 생성 (.pem)"
        isShowing={isShowing}
        link="https://docs.aws.amazon.com/ko_kr/AWSEC2/latest/UserGuide/create-key-pairs.html"
        handleClose={handleClose}
      >
        <StPemKeyModalWrapper>
          <DescribeText>
            키 페어는 EC2 인스턴스에 원격으로 접속하기 위해 필수적으로 생성해야
            해요.
          </DescribeText>
          <Image src="/assets/informModal/keyPem.png" alt="pem.key example" />
          <DetailText>
            <IcIcon src="/assets/icons/ic_check.svg" alt="check" />키 페어 이름
            설정 → 키 페어 생성 → 생성된 키 페어 다운로드
          </DetailText>
          <StCautionWrapper>
            <IcIcon src="/assets/icons/ic_caution.svg" alt="caution" />키 페어는
            한 번 생성하면 다시 다운로드 받을 수 없기 때문에 잘 보관해주세요.
          </StCautionWrapper>
        </StPemKeyModalWrapper>
      </MainModal>
    )
  );
};

export default InformPemKeyModal;

const StPemKeyModalWrapper = styled.div`
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
  padding: 1rem;

  color: ${({ theme }) => theme.colors.Black1};
  ${({ theme }) => theme.fonts.Head5};

  background-color: ${({ theme }) => theme.colors.Red4};
  border-radius: 1rem;
`;
