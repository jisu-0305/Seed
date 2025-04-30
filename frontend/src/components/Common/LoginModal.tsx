// src/components/LoginModal.tsx

'use client';

import styled from '@emotion/styled';
import React from 'react';

import { useUserStore } from '@/stores/userStore';

const LoginModal: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const user = useUserStore((s) => s.user);
  const loading = useUserStore((s) => s.loading);
  const error = useUserStore((s) => s.error);

  return (
    <>
      <Backdrop onClick={onClose} />
      <Modal>
        <CloseBtn onClick={onClose}>X</CloseBtn>

        {loading && <p>로딩 중…</p>}
        {error && <p>유저 정보를 불러올 수 없습니다.</p>}

        {user && (
          <>
            <ProfileSection>
              <Avatar
                src={user.avatarUrl || '/assets/images/avatar_placeholder.png'}
                alt="avatar"
              />
              <UserName>{user.name}</UserName>
            </ProfileSection>

            <List>
              <Item>
                <GitlabIcon src="/assets/icons/ic_gitlab.svg" alt="GitLab" />@
                {user.username}
              </Item>
              <Item
                onClick={() => {
                  /* 로그아웃 */
                }}
              >
                <Icon as="span">⤴︎</Icon>
                로그아웃
              </Item>
            </List>
          </>
        )}
      </Modal>
    </>
  );
};

export default LoginModal;

const Backdrop = styled.div`
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0);
  z-index: 10;
`;

const Modal = styled.div`
  position: absolute;
  top: 5rem;
  right: 1rem;
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
  &:first-of-type {
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
