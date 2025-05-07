import styled from '@emotion/styled';
import React from 'react';

interface MainModalProps {
  title: string;
  isShowing: boolean;
  link: string;
  handleClose: () => void;
  children: React.ReactNode;
}

const MainModal = ({
  title,
  isShowing,
  link,
  handleClose,
  children,
}: MainModalProps) => {
  return (
    isShowing && (
      <StLargeModalWrapper>
        <StTitle>
          {title}
          <DetailBtn
            onClick={() => {
              window.open(link, '_blank');
            }}
          >
            자세히
          </DetailBtn>
          <IcIcon
            src="/assets/icons/ic_close.svg"
            alt="close icon"
            onClick={handleClose}
          />
        </StTitle>

        {children}

        <ConfirmButton onClick={handleClose}>확인</ConfirmButton>
      </StLargeModalWrapper>
    )
  );
};

export default MainModal;

const StLargeModalWrapper = styled.div`
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);

  width: 54rem;
  height: fit-content;

  padding: 3rem 7rem;

  border-radius: 2rem;
  background-color: ${({ theme }) => theme.colors.White};
`;

const StTitle = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;

  width: 100%;
  margin-bottom: 5rem;

  ${({ theme }) => theme.fonts.Head1};
  color: ${({ theme }) => theme.colors.Black1};
`;

const DetailBtn = styled.button`
  padding: 0.5rem 1.5rem;
  border-radius: 10rem;

  ${({ theme }) => theme.fonts.Body4}
  color: ${({ theme }) => theme.colors.White};
  background-color: ${({ theme }) => theme.colors.Main_Carrot};
`;

const IcIcon = styled.img`
  position: absolute;
  right: 2rem;
  top: 2rem;

  cursor: pointer;
`;

const ConfirmButton = styled.button`
  width: 100%;
  height: 5rem;
  padding: 0.5rem 1.5rem;
  margin-top: 3rem;

  border-radius: 1.6rem;

  ${({ theme }) => theme.fonts.Head3};
  color: ${({ theme }) => theme.colors.White};

  background-color: ${({ theme }) => theme.colors.Black};
`;
