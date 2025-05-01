import styled from '@emotion/styled';
import React from 'react';

import LoginModal from '@/components/Common/LoginModal';
import { useModal } from '@/hooks/Common';
import { useUserStore } from '@/stores/userStore';

interface HeaderProps {
  title: string;
}

export default function Header({ title }: HeaderProps) {
  const user = useUserStore((s) => s.user);
  const loading = useUserStore((s) => s.loading);
  const hasHydrated = useUserStore((s) => s.hasHydrated);

  const { isShowing, toggle } = useModal();

  if (!hasHydrated) {
    return (
      <HeaderWrapper>
        <SubTitle>{title}</SubTitle>
      </HeaderWrapper>
    );
  }

  return (
    <HeaderWrapper>
      <SubTitle>{title}</SubTitle>
      <MenuWrapper>
        <LightMode src="/assets/icons/ic_light.svg" alt="light mode" />
        <Alarm src="/assets/icons/ic_alarm.svg" alt="alarm" />
        <Profile onClick={toggle}>
          <ProfileImg
            src={user?.avatarUrl || '/assets/user.png'}
            alt="profile"
          />
          {loading ? '...' : user?.name || 'SSAFY'}
        </Profile>
        {isShowing && <LoginModal onClose={toggle} />}
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

  border-bottom: 1px solid ${({ theme }) => theme.colors.LightGray1};
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
