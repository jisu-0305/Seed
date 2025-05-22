import styled from '@emotion/styled';
import React from 'react';

import { useThemeStore } from '@/stores/themeStore';

export default function Header() {
  const { mode, toggleMode } = useThemeStore();

  if (mode === null) return null;

  return (
    <HeaderWrapper>
      <Logo>
        <LogoImage src={`/assets/icons/ic_logo_${mode}.svg`} alt="logo" />
        <LogoText
          src={
            mode === 'light'
              ? '/assets/icons/ic_logoText.svg'
              : '/assets/icons/ic_logoText_white.svg'
          }
          alt="logoText"
        />
      </Logo>
      <MenuWrapper>
        <LightMode
          onClick={toggleMode}
          src={`/assets/icons/ic_${mode}.svg`}
          alt="light mode"
        />
        <AlarmWrapper>
          <Alarm src={`/assets/icons/ic_alarm_${mode}.svg`} alt="alarm" />
        </AlarmWrapper>
        <Profile>
          <ProfileImg src="/assets/user.png" alt="profile" />
          SSAFY
        </Profile>
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

const Logo = styled.div`
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 0.5rem;

  height: 4rem;
  cursor: pointer;
`;

const LogoImage = styled.img`
  height: 90%;
`;

const LogoText = styled.img`
  height: 3rem;
  width: 8rem;

  padding-top: 0.2rem;
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

const AlarmWrapper = styled.div`
  position: relative;
  display: inline-block;
  cursor: pointer;
`;

const Alarm = styled.img`
  cursor: pointer;
`;
