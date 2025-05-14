import styled from '@emotion/styled';
import { useState } from 'react';

import { useThemeStore } from '@/stores/themeStore';
import {
  ApplicationWithDefaults,
  EnvInfo,
  GitlabInfo,
  ServerInfo,
} from '@/types/project';
import { parseRepoName } from '@/utils/parseRepoName';

export interface StepSidebarProps {
  gitlab: GitlabInfo;
  server: ServerInfo;
  apps: ApplicationWithDefaults[];
  env: EnvInfo;
}

export default function StepSidebar({
  gitlab,
  server,
  apps,
  env,
}: StepSidebarProps) {
  const [isExpanded, setIsExpanded] = useState(false);
  const { mode } = useThemeStore();

  const appCnt = apps.length;
  const mainAppName = apps[0]?.imageName || '-';

  const toggleExpand = () => {
    setIsExpanded((prev) => !prev);
  };

  return (
    <SidebarWrapper>
      {/* GitLab 정보 */}
      <Section>
        <Row>
          <Label>GitLab Repo</Label>
          <Value>{parseRepoName(gitlab.repo) || '-'}</Value>
        </Row>
        <Row>
          <Label>폴더 구조</Label>
          <Value>{gitlab.structure}</Value>
        </Row>
      </Section>

      <Divider />

      {/* 서버 정보 */}
      <Section>
        <Row>
          <Label>IP</Label>
          <Value>{server.ip || '-'}</Value>
        </Row>
        <Row>
          <Label>.pem</Label>
          <Icon
            src={
              server.pem
                ? `/assets/icons/ic_checked_${mode}_true.svg`
                : `/assets/icons/ic_checked_${mode}_false.svg`
            }
            alt="ic_checked"
          />
        </Row>
      </Section>

      <Divider />

      {/* 어플리케이션 */}
      <Section>
        <Row>
          <Label>
            어플리케이션
            {appCnt > 1 && (
              <ArrowIcon
                src={`/assets/icons/ic_arrow_down_${mode}.svg`}
                alt="arrow"
                isExpanded={isExpanded}
                onClick={toggleExpand}
                role="button"
              />
            )}
          </Label>

          {!isExpanded && (
            <AppTag>
              {mainAppName}
              {appCnt > 1 && ' 외'}
            </AppTag>
          )}
        </Row>

        {isExpanded &&
          apps.map((app) => (
            <Row key={app.imageName}>
              <AppTag>{app.imageName}</AppTag>
            </Row>
          ))}
      </Section>

      <Divider />

      {/* 환경설정 */}
      <Section>
        <Row>
          <Label>환경설정</Label>
          <Icon
            src={
              env
                ? `/assets/icons/ic_checked_${mode}_true.svg`
                : `/assets/icons/ic_checked_${mode}_false.svg`
            }
            alt="ic_checked"
          />
        </Row>
      </Section>
    </SidebarWrapper>
  );
}

const SidebarWrapper = styled.aside`
  min-width: 18rem;
  padding: 1.5rem;
  padding-bottom: 0;

  border: 1px solid ${({ theme }) => theme.colors.BorderDefault};
  border-radius: 1.5rem;
`;

const Section = styled.section`
  padding: 1rem;
`;

const Row = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;

  margin-bottom: 1rem;
`;

const Label = styled.span`
  ${({ theme }) => theme.fonts.Head5};
  color: ${({ theme }) => theme.colors.Text};
`;

const Value = styled.span`
  ${({ theme }) => theme.fonts.Body3};
`;

const ArrowIcon = styled.img<{ isExpanded?: boolean }>`
  position: relative;
  top: 0.5rem;

  transform: rotate(${({ isExpanded }) => (isExpanded ? '0deg' : '-90deg')});
  transition: transform 0.2s ease;

  cursor: pointer;
`;

const AppTag = styled.span`
  margin-left: auto;
  ${({ theme }) => theme.fonts.Body3};
`;

const Divider = styled.hr`
  border-top: 1px solid ${({ theme }) => theme.colors.BorderDefault};
  margin: 0.5rem 0;
`;

const Icon = styled.img``;
