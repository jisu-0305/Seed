import styled from '@emotion/styled';

interface HeaderProps {
  title: string;
}

export default function Header({ title }: HeaderProps) {
  return (
    <HeaderWrapper>
      <SubTitle>{title}</SubTitle>
      <MenuWrapper>
        <LightMode src="/assets/icons/ic_light.svg" alt="light mode" />
        <Alarm src="/assets/icons/ic_alarm.svg" alt="alarm" />
        <Profile>
          <ProfileImg src="/assets/icons/ic_profile.svg" alt="profile image" />
          SSAFY
        </Profile>
      </MenuWrapper>
    </HeaderWrapper>
  );
}

const HeaderWrapper = styled.div`
  height: 5rem;

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

const ProfileImg = styled.img``;
