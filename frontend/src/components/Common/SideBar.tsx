import styled from '@emotion/styled';
import { usePathname, useRouter } from 'next/navigation';
import { useEffect } from 'react';

import { useProjectStore } from '@/stores/projectStore';
import { useThemeStore } from '@/stores/themeStore';

export default function SideBar() {
  const router = useRouter();
  const pathname = usePathname();
  const { mode } = useThemeStore();

  const isProjectsActive = pathname.startsWith('/projects');

  const match = pathname.match(/^\/projects\/(\d+)(\/|$)/);
  const currentProjectId = match ? Number(match[1]) : null;

  const { projects, loadProjects } = useProjectStore();

  const handleMovePage = (url: string) => {
    router.push(url);
  };

  useEffect(() => {
    loadProjects();
  }, [loadProjects]);

  if (mode === null) return null;

  return (
    <SideWrapper>
      <Logo onClick={() => handleMovePage('/')}>
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

      <Menu>
        <MenuItem
          active={pathname === '/dashboard'}
          onClick={() => handleMovePage('/dashboard')}
        >
          <IcDashBoard
            src={
              pathname === '/dashboard'
                ? `/assets/icons/ic_board_${mode}_true.svg`
                : `/assets/icons/ic_board_${mode}_false.svg`
            }
            alt="대시보드 아이콘"
          />
          대시보드
        </MenuItem>

        <MenuItem
          active={pathname.startsWith('/create')}
          onClick={() => handleMovePage('/create/gitlab')}
        >
          <IcCreate
            src={
              pathname.startsWith('/create')
                ? `/assets/icons/ic_create_${mode}_true.svg`
                : `/assets/icons/ic_create_${mode}_false.svg`
            }
            alt="새 프로젝트 아이콘"
          />
          새 프로젝트
        </MenuItem>

        <MenuItem
          active={isProjectsActive}
          onClick={() => handleMovePage('/projects')}
        >
          <IcProject
            src={
              isProjectsActive
                ? `/assets/icons/ic_project_${mode}_true.svg`
                : `/assets/icons/ic_project_${mode}_false.svg`
            }
            alt="프로젝트 관리 아이콘"
          />
          프로젝트 관리
          <IcArrow src="/assets/icons/ic_arrow_right.svg" alt="화살표" />
        </MenuItem>
        {isProjectsActive && projects.length > 0 && (
          <SubMenu>
            {projects.map((p) => (
              <SubMenuItem
                key={p.id}
                active={p.id === currentProjectId}
                onClick={() => handleMovePage(`/projects/${p.id}`)}
              >
                {p.projectName}
              </SubMenuItem>
            ))}
          </SubMenu>
        )}
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

  border-right: 1px solid ${({ theme }) => theme.colors.BorderDefault};
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

  ${({ active, theme }) => (active ? theme.fonts.Title6 : theme.fonts.Body3)}

  &:hover {
    background-color: ${({ theme }) => theme.colors.NavSelected};

    ${({ theme }) => theme.fonts.Title6};
  }

  cursor: pointer;
`;

const SubMenu = styled.ul`
  list-style: none;
  margin: 0;
  padding: 0 0 0 4rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  gap: 2rem;
`;

const SubMenuItem = styled.li<{ active?: boolean }>`
  list-style: none;
  cursor: pointer;
  ${({ theme, active }) => (active ? theme.fonts.Title6 : theme.fonts.Body4)};
  color: ${({ theme, active }) =>
    active ? theme.colors.Main_Carrot : theme.colors.Text};

  &:hover {
    ${({ theme }) => theme.fonts.Title6};
  }
`;

const IcDashBoard = styled.img`
  width: 24px;
`;
const IcCreate = styled.img``;
const IcProject = styled.img``;
const IcArrow = styled.img``;
