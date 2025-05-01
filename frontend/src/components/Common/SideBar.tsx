import styled from '@emotion/styled';
import { usePathname, useRouter } from 'next/navigation';

export default function SideBar() {
  const router = useRouter();
  const pathname = usePathname();

  const handleMovePage = (url: string) => {
    router.push(url);
  };

  return (
    <SideWrapper>
      <Logo>
        <LogoImage src="/assets/icons/ic_logo.svg" alt="logo" />
        <LogoText src="/assets/icons/ic_logoText.svg" alt="logoText" />
      </Logo>

      <Menu>
        <MenuItem
          active={pathname === '/dashboard'}
          onClick={() => handleMovePage('/dashboard')}
        >
          <IcDashBoard
            src={
              pathname === '/dashboard'
                ? '/assets/icons/ic_board_true.svg'
                : '/assets/icons/ic_board_false.svg'
            }
            alt="대시보드 아이콘"
          />
          대시보드
        </MenuItem>

        <MenuItem
          active={pathname.startsWith('/create')}
          onClick={() => handleMovePage('/create')}
        >
          <IcCreate
            src={
              pathname.startsWith('/create')
                ? '/assets/icons/ic_create_true.svg'
                : '/assets/icons/ic_create_false.svg'
            }
            alt="새 프로젝트 아이콘"
          />
          새 프로젝트
        </MenuItem>

        <MenuItem
          active={pathname.startsWith('/projects')}
          onClick={() => handleMovePage('/projects')}
        >
          <IcProject
            src={
              pathname.startsWith('/projects')
                ? '/assets/icons/ic_project_true.svg'
                : '/assets/icons/ic_project_false.svg'
            }
            alt="프로젝트 관리 아이콘"
          />
          프로젝트 관리
          <IcArrow src="/assets/icons/ic_arrow_right.svg" alt="화살표" />
        </MenuItem>
      </Menu>
    </SideWrapper>
  );
}

const SideWrapper = styled.div`
  display: flex;
  flex-direction: column;
  gap: 2rem;

  min-width: 18rem;
  padding: 2rem;

  border-right: 1px solid ${({ theme }) => theme.colors.LightGray1};
`;

const Logo = styled.div`
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 0.5rem;

  height: 4rem;
`;

const LogoImage = styled.img`
  height: 90%;
`;

const LogoText = styled.img`
  height: 60%;

  padding-top: 0.2rem;
`;

const Menu = styled.ul`
  display: flex;
  flex-direction: column;

  gap: 1rem;
`;

const MenuItem = styled.li<{ active?: boolean }>`
  list-style: none;

  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 1.5rem;

  height: 4rem;
  padding: 0.5rem 1rem;

  background-color: ${({ active }) =>
    active ? 'theme.colors.LightGray3' : 'transparent'};
  border-radius: 0.8rem;

  color: ${({ theme }) => theme.colors.Black1};
  ${({ active, theme }) => (active ? theme.fonts.Title6 : theme.fonts.Body3)}

  &:hover {
    background-color: ${({ theme }) => theme.colors.LightGray3};

    ${({ theme }) => theme.fonts.Title6};
  }

  cursor: pointer;
`;

const IcDashBoard = styled.img`
  width: 24px;
`;
const IcCreate = styled.img``;
const IcProject = styled.img``;
const IcArrow = styled.img``;
