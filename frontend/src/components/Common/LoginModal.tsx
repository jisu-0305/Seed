// src/components/LoginModal.tsx

'use client';

import styled from '@emotion/styled';
import { useRouter } from 'next/navigation';
import React from 'react';

import { logout } from '@/apis/user';
import { useUserStore } from '@/stores/userStore';

const LoginModal: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const router = useRouter();
  const user = useUserStore((s) => s.user);
  const loading = useUserStore((s) => s.loading);
  const error = useUserStore((s) => s.error);
  const clearUser = useUserStore((s) => s.clearUser);

  const handleLogout = async () => {
    try {
      // API 로그아웃
      await logout();
    } catch (err) {
      console.error('로그아웃 API 실패', err);
    } finally {
      // 로컬 스토리지 토큰 삭제
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      // Zustand 스토어 초기화
      clearUser();
      // 세션스토리지에서 persisted 데이터 삭제
      sessionStorage.removeItem('user-storage-session');

      onClose();
      router.replace('/');
    }
  };

  return (
    <>
      <Backdrop onClick={onClose} />
      <Modal>
        {loading && <p>로딩 중…</p>}
        {error && <p>유저 정보를 불러올 수 없습니다.</p>}

        {user && (
          <>
            <ProfileSection>
              <Avatar src={user.avatarUrl || '/assets/user.png'} alt="avatar" />
              <UserName>{user.name}</UserName>
            </ProfileSection>

            <List>
              <Item>
                <GitlabIcon src="/assets/icons/ic_gitlab.svg" alt="GitLab" />@
                {user.username}
              </Item>
              <Item onClick={handleLogout}>
                <Icon src="/assets/icons/ic_logout.svg" />
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
  top: 5.1rem;
  right: 1rem;
  width: 17rem;
  background: ${({ theme }) => theme.colors.White};
  border-radius: 1rem;
  padding: 1.5rem 3rem;
  box-shadow: 1px 2px 4px rgba(0, 0, 0, 0.1);
  z-index: 11;
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
  margin-right: 2rem;
`;

const UserName = styled.span`
  ${({ theme }) => theme.fonts.Title5};
`;

const List = styled.ul`
  list-style: none;
  padding: 0rem 2rem;
  margin: 0;
`;

const Item = styled.li`
  display: flex;
  align-items: center;
  padding: 0.7rem 0;
  ${({ theme }) => theme.fonts.Body5};

  cursor: pointer;
`;

const Icon = styled.img`
  width: 1.75rem;
  margin-left: 0.4rem;
  margin-right: 1.2rem;
`;

const GitlabIcon = styled.img`
  width: 2rem;
  margin-right: 1.2rem;
`;
