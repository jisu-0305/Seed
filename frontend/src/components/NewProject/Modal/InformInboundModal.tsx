import styled from '@emotion/styled';

import MainModal from '@/components/Common/Modal/MainModal';

interface Props {
  isShowing: boolean;
  handleClose: () => void;
}

const InformInboundModal = ({ isShowing, handleClose }: Props) => {
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
          <Table>
            <thead>
              <tr>
                <Th>포트번호</Th>
                <Th>설명</Th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <Td>
                  <strong>22</strong>
                </Td>
                <Td>터미널을 통해 서버에 접속할 수 있도록 허용</Td>
              </tr>
              <tr>
                <Td>
                  <strong>80</strong>
                </Td>
                <Td>일반 웹사이트 접속을 위한 포트 (http://)</Td>
              </tr>
              <tr>
                <Td>
                  <strong>443</strong>
                </Td>
                <Td>보안 연결을 위한 웹 포트 (https://)</Td>
              </tr>
              <tr>
                <Td>
                  <strong>3789</strong>
                </Td>
                <Td>Docker Health 체크를 위한 포트</Td>
              </tr>
              <tr>
                <Td>
                  <strong>8080</strong>
                </Td>
                <Td>Spring Boot 접속을 위한 포트</Td>
              </tr>
              <tr>
                <Td>
                  <strong>9090</strong>
                </Td>
                <Td>Jenkins 접속을 위한 포트</Td>
              </tr>
            </tbody>
          </Table>
        </StModalWrapper>
      </MainModal>
    )
  );
};

export default InformInboundModal;

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

const Table = styled.table`
  width: 90%;
  border-collapse: separate;
  border-spacing: 0;
  border: 1px solid ${({ theme }) => theme.colors.Gray3};
  border-radius: 1rem;
  overflow: hidden;

  thead {
    display: table;
    width: 100%;
    table-layout: fixed;
  }

  tbody {
    display: block;
    max-height: 15rem;
    overflow-y: auto;
    width: 100%;
  }

  tr {
    display: table;
    width: 100%;
    table-layout: fixed;
  }
`;

const Th = styled.th`
  padding: 1.2rem 1.6rem;
  text-align: center;
  ${({ theme }) => theme.fonts.Title6};
  color: ${({ theme }) => theme.colors.Black1};
  border-bottom: 1px solid ${({ theme }) => theme.colors.Gray3};

  &:first-of-type {
    width: 25%;
  }

  &:last-of-type {
    width: 75%;
  }
`;

const Td = styled.td`
  padding: 1.2rem 1.6rem;
  text-align: center;
  ${({ theme }) => theme.fonts.Body3};
  color: ${({ theme }) => theme.colors.Black1};
  border-bottom: 1px solid ${({ theme }) => theme.colors.Gray3};
  word-break: keep-all;

  &:first-of-type {
    width: 25%;
  }

  &:last-of-type {
    width: 75%;
  }

  strong {
    ${({ theme }) => theme.fonts.Title6};
    color: ${({ theme }) => theme.colors.Black1};
  }
`;
