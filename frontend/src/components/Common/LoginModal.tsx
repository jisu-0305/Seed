import styled from '@emotion/styled';
import React from 'react';

interface Props {
  onClose: () => void;
}

const LoginModal: React.FC<Props> = ({ onClose }) => (
  <>
    <Backdrop onClick={onClose} />
    <Modal>
      <CloseBtn onClick={onClose}>&times;</CloseBtn>

      <ProfileSection>
        <Avatar src="/assets/images/avatar_placeholder.png" alt="avatar" />
        <UserName>김싸피입니다</UserName>
        <EditIcon>✏️</EditIcon>
      </ProfileSection>

      <List>
        <Item>
          <GitlabIcon src="/assets/icons/ic_gitlab.svg" alt="GitLab" />
          ssafy@gmail.com
        </Item>
        <Item
          onClick={() => {
            /* 로그아웃 로직 */
          }}
        >
          <Icon as="span">⤴︎</Icon>
          로그아웃
        </Item>
      </List>
    </Modal>
  </>
);

export default LoginModal;

export const HeaderWrap = styled.header`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
`;

export const Logo = styled.h1`
  font-size: 1.5rem;
  font-weight: bold;
`;

export const Profile = styled.div`
  display: flex;
  align-items: center;
  cursor: pointer;
`;
export const ProfileImg = styled.img`
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  margin-right: 0.5rem;
`;

// Modal
const Backdrop = styled.div`
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 10;
`;

const Modal = styled.div`
  position: absolute;
  top: 4rem;
  right: 2rem;
  width: 18rem;
  background: #fff;
  border-radius: 0.5rem;
  padding: 1rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  z-index: 11;
`;

const CloseBtn = styled.button`
  position: absolute;
  top: 0.5rem;
  right: 0.75rem;
  background: transparent;
  border: none;
  font-size: 1.25rem;
  cursor: pointer;
`;

const ProfileSection = styled.div`
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
`;

const Avatar = styled.img`
  width: 3rem;
  height: 3rem;
  border-radius: 50%;
  background: #eef2f7;
  margin-right: 0.75rem;
`;

const UserName = styled.span`
  flex: 1;
  font-weight: 600;
  font-size: 1.125rem;
`;

const EditIcon = styled.span`
  cursor: pointer;
`;

const List = styled.ul`
  list-style: none;
  padding: 0;
  margin: 0;
`;

const Item = styled.li`
  display: flex;
  align-items: center;
  padding: 0.75rem 0;
  cursor: pointer;
  border-top: 1px solid #f0f0f0;

  &:first-child {
    border-top: none;
  }
`;

const Icon = styled.img`
  width: 1.25rem;
  height: 1.25rem;
  margin-right: 0.5rem;
`;

const GitlabIcon = styled.img`
  width: 3rem;
  margin-right: 1rem;
`;
