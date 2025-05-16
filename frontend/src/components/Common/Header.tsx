import styled from '@emotion/styled';
import React from 'react';

import LoginModal from '@/components/Common/LoginModal';
import NotificationModal from '@/components/Common/NotificationModal';
import { useModal } from '@/hooks/Common';
import { useNotifications } from '@/hooks/Common/useNotifications';
import { useThemeStore } from '@/stores/themeStore';
import { useUserStore } from '@/stores/userStore';

interface HeaderProps {
  title: string;
}

export default function Header({ title }: HeaderProps) {
  const user = useUserStore((s) => s.user);
  const hasHydrated = useUserStore((s) => s.hasHydrated);

  const login = useModal();
  const notif = useModal();
  const { notifications } = useNotifications();

  const { mode, toggleMode } = useThemeStore();

  if (!hasHydrated) {
    return (
      <HeaderWrapper>
        <SubTitle>{title}</SubTitle>
      </HeaderWrapper>
    );
  }

  if (mode === null) return null;

  return (
    <HeaderWrapper>
      <SubTitle>{title}</SubTitle>
      <MenuWrapper>
        <LightMode
          onClick={toggleMode}
          src={`/assets/icons/ic_${mode}.svg`}
          alt="light mode"
        />
        <AlarmWrapper onClick={notif.toggle}>
          <Alarm src={`/assets/icons/ic_alarm_${mode}.svg`} alt="alarm" />
          {notifications.length > 0 && <Badge>{notifications.length}</Badge>}
        </AlarmWrapper>
        <Profile onClick={login.toggle}>
          <ProfileImg
            src={user?.profileImageUrl || '/assets/user.png'}
            alt="profile"
          />
          {user?.userName || 'SSAFY'}
        </Profile>
        {notif.isShowing && <NotificationModal onClose={notif.toggle} />}
        {login.isShowing && <LoginModal onClose={login.toggle} />}
      </MenuWrapper>
    </HeaderWrapper>
  );
}

const HeaderWrapper = styled.div`
  height: 7rem;

  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;

  padding: 0 2rem;

  border-bottom: 1px solid ${({ theme }) => theme.colors.BorderDefault};
`;

const SubTitle = styled.div`
  ${({ theme }) => theme.fonts.Body3};
`;

const MenuWrapper = styled.div`
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 1rem;
`;

const LightMode = styled.img`
  cursor: pointer;
`;

const Alarm = styled.img`
  cursor: pointer;
`;

const Profile = styled.div`
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 1rem;

  ${({ theme }) => theme.fonts.Body4};

  cursor: pointer;
`;

const ProfileImg = styled.img`
  width: 2.5rem;
  height: 2.5rem;
  object-fit: cover;
  border-radius: 50%;
`;

export const AlarmWrapper = styled.div`
  position: relative;
  display: inline-block;
  cursor: pointer;
`;

export const Badge = styled.span`
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 1.2rem;
  height: 1.2rem;
  padding: 0 0.2rem;
  background-color: ${({ theme }) => theme.colors.RedBtn};
  color: white;
  font-size: 0.75rem;
  font-weight: bold;
  line-height: 1.2rem;
  border-radius: 0.6rem;
  text-align: center;
  pointer-events: none;
`;
