import styled from '@emotion/styled';
import { useState } from 'react';

import { useProjectInfoStore } from '@/stores/projectStore';
import { useThemeStore } from '@/stores/themeStore';

export default function StepSidebar() {
  const { stepStatus: status } = useProjectInfoStore();
  const [isExpanded, setIsExpanded] = useState(false);
  const { mode } = useThemeStore();

  const appCnt = status.app.length;
  const mainAppName = status.app[0]?.imageName || '-';

  const toggleExpand = () => {
    setIsExpanded((prev) => !prev);
  };

  if (mode === null) return null;

  return (
    <SidebarWrapper>
      {/* GitLab 정보 */}
      <Section>
        <Row>
          <Label>GitLab Repo</Label>
          <Value>
            {status.gitlab.repo
              ?.split('/')
              .pop()
              ?.replace(/\.git$/, '') || '-'}
          </Value>
        </Row>
        <Row>
          <Label>폴더 구조</Label>
          <Value>{status.gitlab.structure}</Value>
        </Row>
        <Row>
          <Label>디렉토리</Label>
          <Icon
            src={
              status.gitlab.directory.client && status.gitlab.directory.server
                ? `/assets/icons/ic_checked_${mode}_true.svg`
                : `/assets/icons/ic_checked_${mode}_false.svg`
            }
            alt="ic_checked"
          />
        </Row>
      </Section>

      <Divider />

      {/* 서버 정보 */}
      <Section>
        <Row>
          <Label>IP</Label>
          <Value>{status.server.ip || '-'}</Value>
        </Row>
        <Row>
          <Label>.pem</Label>
          <Icon
            src={
              status.server.pem
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
          status.app.map((app) => (
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
              status.env
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
  gap: 1rem;

  margin-bottom: 1rem;
`;

const Label = styled.span`
  min-width: fit-content;

  ${({ theme }) => theme.fonts.Head5};
  color: ${({ theme }) => theme.colors.Text};
`;

const Value = styled.span`
  ${({ theme }) => theme.fonts.Body3};

  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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
