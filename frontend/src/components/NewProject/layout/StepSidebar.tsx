import styled from '@emotion/styled';

import { useProjectInfoStore } from '@/stores/projectStore';

export default function StepSidebar() {
  const { stepStatus: status } = useProjectInfoStore();

  return (
    <SidebarWrapper>
      {/* GitLab 정보 */}
      <Section>
        <Row>
          <Label>GitLab Repo</Label>
          <Value>{status.gitlab.repo || '-'}</Value>
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
                ? '/assets/icons/ic_checked_true.svg'
                : '/assets/icons/ic_checked_false.svg'
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
                ? '/assets/icons/ic_checked_true.svg'
                : '/assets/icons/ic_checked_false.svg'
            }
            alt="ic_checked"
          />
        </Row>
      </Section>

      <Divider />

      {/* 어플리케이션 */}
      <Section>
        <Row>
          <Label>어플리케이션</Label>
        </Row>
        {status.app.length > 0 ? (
          status.app.map((app) => (
            <Row key={app.name}>
              <AppTag>{app.name}</AppTag>
            </Row>
          ))
        ) : (
          <Row>
            <AppTag>-</AppTag>
          </Row>
        )}
      </Section>

      <Divider />

      {/* 환경설정 */}
      <Section>
        <Row>
          <Label>환경설정</Label>
          <Icon
            src={
              status.env
                ? '/assets/icons/ic_checked_true.svg'
                : '/assets/icons/ic_checked_false.svg'
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

  border: 1px solid ${({ theme }) => theme.colors.LightGray1};
  border-radius: 1.5rem;
`;

const Section = styled.section`
  padding: 1rem;
`;

const Row = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;

  margin-bottom: 1.5rem;
`;

const Label = styled.span`
  ${({ theme }) => theme.fonts.Head5};
  color: ${({ theme }) => theme.colors.Black1};
`;

const Value = styled.span`
  ${({ theme }) => theme.fonts.Body3};
`;

const AppTag = styled.span`
  margin-left: auto;
  ${({ theme }) => theme.fonts.Body3};
`;

const Divider = styled.hr`
  border-top: 1px solid ${({ theme }) => theme.colors.LightGray1};
  margin: 0.5rem 0;
`;

const Icon = styled.img``;
