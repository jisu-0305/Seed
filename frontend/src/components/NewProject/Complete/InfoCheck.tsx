import styled from '@emotion/styled';

import { useProjectInfoStore } from '@/stores/projectStore';
import { useThemeStore } from '@/stores/themeStore';

export default function InfoCheck() {
  const { stepStatus: status } = useProjectInfoStore();
  const { mode } = useThemeStore();

  return (
    <StWrapper>
      <Title>5. 최종 확인</Title>

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
              status.gitlab.directory
                ? `/assets/icons/ic_checked_${mode}_true.svg`
                : `/assets/icons/ic_checked_${mode}_false.svg`
            }
            alt="checked"
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
            alt="checked"
          />
        </Row>
      </Section>

      <Divider />

      {/* 어플리케이션 */}
      <Section>
        <Row>
          <Label>어플리케이션</Label>
        </Row>
        <AppList>
          {status.app.length > 0 ? (
            status.app.map((app) => (
              <Row key={app.name}>
                <AppInfo>
                  <strong>{app.name}</strong> :{' '}
                  {app.tag === 'latest' ? 'LTS' : app.tag}
                </AppInfo>
                <PortInfo>: {app.port}</PortInfo>
              </Row>
            ))
          ) : (
            <Row>
              <Value>-</Value>
            </Row>
          )}
        </AppList>
      </Section>

      <Divider />

      {/* 환경 설정 */}
      <Section>
        <Row>
          <Label>환경설정</Label>
        </Row>
        <AppList>
          <Row>
            <AppInfo>.env</AppInfo>
            <Icon
              src={
                status.env.env
                  ? `/assets/icons/ic_checked_${mode}_true.svg`
                  : `/assets/icons/ic_checked_${mode}_false.svg`
              }
              alt="checked"
            />
          </Row>
          <Row>
            <AppInfo>Node</AppInfo>
            <Value>{status.env.node || '-'}</Value>
          </Row>
          <Row>
            <AppInfo>JDK</AppInfo>
            <Value>{status.env.jdk || '-'}</Value>
          </Row>
          <Row>
            <AppInfo>빌드</AppInfo>
            <Value>{status.env.buildTool || '-'}</Value>
          </Row>
        </AppList>
      </Section>
    </StWrapper>
  );
}

const StWrapper = styled.aside`
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  min-height: 55rem;

  padding: 4rem;
  padding-bottom: 2rem;

  border: 1px solid ${({ theme }) => theme.colors.BorderDefault};
  border-radius: 1.5rem;
`;

const Title = styled.h3`
  ${({ theme }) => theme.fonts.Head3};
  margin-bottom: 2rem;
`;

const Section = styled.section`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  padding: 1rem 0;
`;

const Row = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;

  width: 100%;
  max-width: 35rem;
  margin-bottom: 1rem;
`;

const Label = styled.span`
  ${({ theme }) => theme.fonts.Head5};
  color: ${({ theme }) => theme.colors.Text};
`;

const Value = styled.span`
  ${({ theme }) => theme.fonts.Body3};
`;

const Divider = styled.hr`
  /* max-width: 40rem; */

  border-top: 1px solid ${({ theme }) => theme.colors.BorderDefault};
  margin: 1rem 0;
`;

const Icon = styled.img`
  width: 1.5rem;
  height: 1.5rem;
`;

const AppList = styled.div`
  display: flex;
  flex-direction: column;

  width: 20rem;
  padding-left: 15rem;
`;

const AppInfo = styled.div`
  ${({ theme }) => theme.fonts.Body3};

  strong {
    ${({ theme }) => theme.fonts.Title6};
  }
`;

const PortInfo = styled.div`
  ${({ theme }) => theme.fonts.Body3};
`;
