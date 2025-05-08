import styled from '@emotion/styled';

import MainModal from '@/components/Common/Modal/MainModal';

interface Props {
  isShowing: boolean;
  handleClose: () => void;
}

const InformIpModal = ({ isShowing, handleClose }: Props) => {
  return (
    isShowing && (
      <MainModal
        title="EC2 IP 주소 입력"
        isShowing={isShowing}
        link="https://docs.aws.amazon.com/ko_kr/AWSEC2/latest/UserGuide/managing-network-interface-ip-addresses.html"
        handleClose={handleClose}
      >
        <StModalWrapper>
          <DescribeText>
            EC2 인스턴스에 접속하려면 퍼블릭 IP 또는 탄력적 IP 주소가 필요해요.
          </DescribeText>
          <Image src="/assets/informModal/ip.png" alt="pem.key example" />
          <DetailText>
            <SubTitle>
              <IcIcon src="/assets/icons/ic_check.svg" alt="check" />
              퍼블릭 IP
            </SubTitle>
            <Ul>
              <li>인스턴스 재시작 시 IP 변경됨</li>
              <li>변경 시 IP 관련 환경 변수 수정 必</li>
            </Ul>
          </DetailText>
          <DetailText>
            <SubTitle>
              <IcIcon src="/assets/icons/ic_check.svg" alt="check" />
              탄력적 IP
            </SubTitle>
            <Ul>
              <li>인스턴스 재시작 후에도 IP 고정 유지</li>
              <li>
                인스턴스에 연결되지 않은 상태로 유지되면 <strong>과금 </strong>
                발생
              </li>
            </Ul>
          </DetailText>
        </StModalWrapper>
      </MainModal>
    )
  );
};

export default InformIpModal;

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
  width: 35rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: start;

  color: ${({ theme }) => theme.colors.Black1};
  ${({ theme }) => theme.fonts.Head6};

  margin-bottom: 1.8rem;
`;

const IcIcon = styled.img`
  margin-right: 0.5rem;
`;

const SubTitle = styled.div`
  color: ${({ theme }) => theme.colors.Black1};
  ${({ theme }) => theme.fonts.Head3};

  margin-bottom: 0.5rem;
`;

const Ul = styled.ul`
  display: flex;
  flex-direction: column;
  gap: 0.4rem;

  margin-left: 4rem;

  li {
    list-style-type: disc;
    color: ${({ theme }) => theme.colors.Black1};
    ${({ theme }) => theme.fonts.Body3};

    strong {
      color: ${({ theme }) => theme.colors.Red1};
      ${({ theme }) => theme.fonts.Body3};
    }
  }
`;
