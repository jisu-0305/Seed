import styled from '@emotion/styled';
import React from 'react';

interface SmallModalProps {
  title: string;
  isShowing: boolean;
  handleClose: () => void;
  children: React.ReactNode;
}

const SmallModal = ({
  title,
  isShowing,
  handleClose,
  children,
}: SmallModalProps) => {
  return (
    isShowing && (
      <StLargeModalWrapper>
        <StTitle>
          {title}
          <IcIcon
            src="/assets/icons/ic_close.svg"
            alt="close icon"
            onClick={handleClose}
          />
        </StTitle>

        {children}
      </StLargeModalWrapper>
    )
  );
};

export default SmallModal;

const StLargeModalWrapper = styled.div`
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);

  width: 57rem;
  height: fit-content;

  padding: 2rem 3rem;

  border-radius: 2rem;
  background-color: ${({ theme }) => theme.colors.White};
`;

const StTitle = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;

  width: 100%;
  margin-top: 4rem;
  margin-bottom: 2.5rem;

  ${({ theme }) => theme.fonts.Head1};
  color: ${({ theme }) => theme.colors.Black1};
`;

const IcIcon = styled.img`
  position: absolute;
  right: 2rem;
  top: 2rem;

  cursor: pointer;
`;
