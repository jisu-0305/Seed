import 'swiper/css';

import styled from '@emotion/styled';
import { useMemo, useState } from 'react';
import { Swiper, SwiperSlide } from 'swiper/react';

import { useProjectCards, useProjectExecutions } from '@/apis/project';
import { useVerticalDragScroll } from '@/hooks/Common/useVerticalDragScroll';
import { Execution } from '@/types/execution';

import { LoadingSpinner } from '../Common/LoadingSpinner';
import { ActivityCard } from './ActivityCard';
import { Calender } from './Calender';
import { ProjectCard } from './ProjectCard';

export default function HomePage() {
  const verticalDragRef = useVerticalDragScroll<HTMLDivElement>();
  const [selectedDate, setSelectedDate] = useState(new Date());

  const { data: projectCards = [], isLoading: loadingProjects } =
    useProjectCards();
  const { data: executionsByDate = [], isLoading: loadingExec } =
    useProjectExecutions();

  // useEffect(() => {
  //   console.log('📦 projectCards:', projectCards);
  // }, [projectCards]);

  // useEffect(() => {
  //   console.log('🔄 executionsByDate:', executionsByDate);
  // }, [executionsByDate]);

  // 1) 캘린더에 찍을 날짜 배열
  const activityDates: Date[] = useMemo(
    () =>
      executionsByDate.map((grp) => {
        const [year, month, day] = grp.date.split('-').map(Number);
        // monthIndex 는 0부터 시작
        return new Date(year, month - 1, day);
      }),
    [executionsByDate],
  );
  const createdDates = useMemo(
    () =>
      projectCards
        .map((pc) => (pc.createdAt ? new Date(pc.createdAt) : null))
        .filter((d): d is Date => d !== null),
    [projectCards],
  );

  // 2) 실행 기록을 Map<YYYY-MM-DD, Execution[]>
  const execMap = useMemo(() => {
    const m = new Map<string, Execution[]>();
    executionsByDate.forEach((grp) => {
      m.set(grp.date, grp.executionList);
    });
    return m;
  }, [executionsByDate]);

  // 3) 선택된 날짜 키
  const selectedKey = `${selectedDate.getFullYear()}-${String(
    selectedDate.getMonth() + 1,
  ).padStart(2, '0')}-${String(selectedDate.getDate()).padStart(2, '0')}`;

  // 4) 오늘 생성 & 실행 리스트
  const todayCreated = projectCards.filter((pc) =>
    pc.createdAt?.startsWith(selectedKey),
  );
  const todayExecList = execMap.get(selectedKey) ?? [];

  // 5) 실행 타입 → ActivityCard type 매핑
  const mapType = (execType: string, status: string) => {
    if (execType === 'BUILD') return status === 'SUCCESS' ? 'success' : 'fail';
    if (execType === 'DEPLOY') return 'deploy';
    if (execType === 'HTTPS') return 'https';
    return 'success';
  };

  return (
    <PageWrapper>
      <WorkspaceSection>
        <SectionTitle>Workspace</SectionTitle>
        {loadingProjects ? (
          <p>로딩 중...</p>
        ) : projectCards?.length === 0 ? (
          <EmptyMessage>아직 프로젝트가 없습니다</EmptyMessage>
        ) : (
          <Swiper
            spaceBetween={28}
            slidesPerView={4}
            grabCursor
            breakpoints={{
              0: {
                slidesPerView: 1,
              },
              480: {
                slidesPerView: 3,
              },
              768: {
                slidesPerView: 3,
              },
              1024: {
                slidesPerView: 4,
              },
            }}
            style={{ paddingBottom: '1rem' }}
          >
            {projectCards?.map((project, idx) => {
              const emoji =
                idx === 0
                  ? 'default'
                  : project.buildStatus === 'SUCCESS'
                    ? 'success'
                    : 'fail';

              return (
                <SwiperSlide key={project.id}>
                  <ProjectCard
                    id={project.id}
                    emoji={emoji}
                    title={project.projectName}
                    time={project.lastBuildAt}
                    build={project.buildStatus}
                    deploy={project.autoDeploymentEnabled}
                    https={project.httpsEnabled}
                  />
                </SwiperSlide>
              );
            })}
          </Swiper>
        )}
      </WorkspaceSection>

      <SectionTitle>Development</SectionTitle>
      <DevelopmentSection>
        <CalendarBox>
          <Calender
            selected={selectedDate}
            onSelect={(d) => d && setSelectedDate(d)}
            activityDates={activityDates}
            createdDates={createdDates}
          />
        </CalendarBox>

        <TaskBox>
          <Title>Day {selectedDate.getDate()}</Title>
          <ActivityWrapper ref={verticalDragRef}>
            {loadingExec || loadingProjects ? (
              <LoadingSpinner />
            ) : todayCreated.length === 0 && todayExecList.length === 0 ? (
              <EmptyMessage>생성/실행된 기록이 없습니다</EmptyMessage>
            ) : (
              <>
                {todayCreated.map((pc) => (
                  <ActivityCard
                    key={`create-${pc.id}`}
                    title="프로젝트 생성"
                    project={pc.projectName}
                    time={pc.createdAt!.slice(0, 10)}
                    type="deploy"
                  />
                ))}

                {todayExecList.map((exec) => (
                  <ActivityCard
                    key={exec.id}
                    title={exec.projectExecutionTitle}
                    project={exec.projectName}
                    time={exec.createdAt}
                    type={mapType(exec.executionType, exec.executionStatus)}
                  />
                ))}
              </>
            )}
          </ActivityWrapper>
        </TaskBox>
      </DevelopmentSection>
    </PageWrapper>
  );
}

const PageWrapper = styled.div`
  padding: 4rem;
  margin: 0 auto;
  max-width: 100rem;
`;

const SectionTitle = styled.h2`
  margin-bottom: 2rem;

  ${({ theme }) => theme.fonts.EnTitle1};
`;

const WorkspaceSection = styled.section`
  margin-bottom: 4.8rem;
  width: 100%;
  min-height: 20rem;
  overflow: hidden;

  .swiper-slide {
    display: flex;
    justify-content: flex-start;
  }
`;

const DevelopmentSection = styled.section`
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  gap: 5rem;

  flex-wrap: wrap;

  padding: 2rem 3rem;

  border: 1px solid ${({ theme }) => theme.colors.BorderDefault};
  border-radius: 2rem;
`;

const CalendarBox = styled.div`
  min-width: 30rem;

  padding-top: 1rem;
`;

const Title = styled.h2`
  ${({ theme }) => theme.fonts.EnTitle2};
`;

const ActivityWrapper = styled.div`
  display: flex;
  flex-direction: column;
  gap: 1rem;
  overflow-y: auto;

  height: 31rem;

  cursor: grab;
  -webkit-overflow-scrolling: touch;

  &::-webkit-scrollbar {
    display: none;
  }
`;

const TaskBox = styled.div`
  min-width: 45rem;
  overflow: hidden;

  display: flex;
  flex-direction: column;
  gap: 1.5rem;

  padding-bottom: 2rem;
`;

const EmptyMessage = styled.div`
  padding: 1rem;
  text-align: center;
  ${({ theme }) => theme.fonts.Body1};
  color: ${({ theme }) => theme.colors.Text};
`;
