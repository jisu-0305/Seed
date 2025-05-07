import styled from '@emotion/styled';

interface ModalWrapperProps {
  isShowing: boolean;
  children: React.ReactNode;
}

const ModalWrapper: React.FC<ModalWrapperProps> = ({ isShowing, children }) => (
  <StModalWrapper $showing={isShowing}>{children}</StModalWrapper>
);

const StModalWrapper = styled.div<{ $showing: boolean }>`
  display: ${({ $showing }) => ($showing ? 'flex' : 'none')};
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1000;

  justify-content: center;
  align-items: center;

  width: 100vw;
  height: 100vh;

  background-color: rgba(0, 0, 0, 0.5);
`;

export default ModalWrapper;
