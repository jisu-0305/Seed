'use client';

import styled from '@emotion/styled';
import { usePathname, useRouter } from 'next/navigation';
import React from 'react';

import { logout } from '@/apis/user';
import { useThemeStore } from '@/stores/themeStore';
import { useUserStore } from '@/stores/userStore';
import { clearUserData } from '@/utils/auth';

const LoginModal: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const router = useRouter();
  const pathname = usePathname();
  const user = useUserStore((s) => s.user);
  const { mode } = useThemeStore();

  const isOnboarding = pathname.startsWith('/onboarding');

  if (mode === null) return null;

  const handleLogout = async () => {
    try {
      await logout();
    } catch (err) {
      console.error('로그아웃 API 실패', err);
    } finally {
      clearUserData();
      onClose();
      router.replace('/');
    }
  };

  const goGitlab = () => {
    const url = `https://lab.ssafy.com/${user?.userIdentifyId ?? ''}`;
    window.open(url, '_blank', 'noopener,noreferrer');
  };

  return (
    <>
      <Backdrop onClick={onClose} />
      <Modal>
        {user && (
          <>
            <ProfileSection>
              <Avatar
                src={user.profileImageUrl || '/assets/user.png'}
                alt="avatar"
              />
              <UserName>{user.userName}</UserName>
              {!isOnboarding && (
                <EditButton
                  onClick={() => {
                    router.replace('/user');
                  }}
                >
                  토큰
                  <PenIcon src={`/assets/icons/ic_pen_${mode}.svg`} alt="pen" />
                </EditButton>
              )}
            </ProfileSection>

            <List>
              <Item onClick={goGitlab}>
                <GitlabIcon src="/assets/icons/ic_gitlab.svg" alt="GitLab" />@
                {user.userIdentifyId}
              </Item>
              <Item onClick={handleLogout}>
                <Icon src={`/assets/icons/ic_logout_${mode}.svg`} />
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
  top: 7.1rem;
  right: 0.5rem;
  width: 18rem;
  background: ${({ theme }) => theme.colors.ModalBg};
  border-radius: 1rem;
  padding: 1.5rem 3rem;
  box-shadow: 1px 2px 4px rgba(0, 0, 0, 0.1);
  z-index: 11;
`;

const ProfileSection = styled.div`
  display: flex;
  align-items: center;
  width: 100%;
  margin-bottom: 1rem;
  gap: 2rem;
`;

const Avatar = styled.img`
  width: 3rem;
  height: 3rem;
  border-radius: 50%;
`;

const UserName = styled.div`
  width: fit-content;
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

const EditButton = styled.button`
  display: flex;
  align-items: center;
  padding: 0.3rem 0.8rem;
  ${({ theme }) => theme.fonts.Body5};
  border-radius: 5rem;
  background-color: ${({ theme }) => theme.colors.MenuBg};
  color: ${({ theme }) => theme.colors.MenuText};
`;

const PenIcon = styled.img`
  width: 1.5rem;
  height: 1.5rem;
  margin-left: 0.4rem;
`;
