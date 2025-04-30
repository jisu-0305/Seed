import styled from '@emotion/styled';
import { useState } from 'react';

import { useDragScroll } from '@/hooks/Common/useDragScroll';
import { useVerticalDragScroll } from '@/hooks/Common/useVerticalDragScroll';

import { ActivityCard } from './ActivityCard';
import { Calender } from './Calender';
import { ProjectCard } from './ProjectCard';

export default function HomePage() {
  const dragRef = useDragScroll<HTMLDivElement>();
  const verticalDragRef = useVerticalDragScroll<HTMLDivElement>();

  const [selectedDate, setSelectedDate] = useState(new Date());

  return (
    <PageWrapper>
      <WorkspaceSection>
        <SectionTitle>Workspace</SectionTitle>
        <WorkspaceGrid ref={dragRef}>
          <ProjectCard
            emoji="default"
            title="S12P31A206"
            time="03.20 10:02:75"
            build
            https
          />
          <ProjectCard
            emoji="fail"
            title="Project 2"
            time="03.20 10:02:75"
            build={false}
            https={false}
          />
          <ProjectCard
            emoji="success"
            title="Project 3"
            time="10일 전"
            build
            https
          />
          <ProjectCard
            emoji="success"
            title="Project 4"
            time="10일 전"
            build
            https
          />
          <ProjectCard
            emoji="success"
            title="Project 4"
            time="10일 전"
            build
            https
          />
        </WorkspaceGrid>
      </WorkspaceSection>

      <SectionTitle>Development</SectionTitle>
      <DevelopmentSection>
        <CalendarBox>
          <Calender
            selected={selectedDate}
            onSelect={(d) => d && setSelectedDate(d)}
          />
        </CalendarBox>

        <TaskBox>
          <Title>Day {selectedDate.getDate()}</Title>
          <ActivityWrapper ref={verticalDragRef}>
            <ActivityCard
              title="초기 배포설정"
              project="S12P31A206"
              time="10:30:24"
              type="deploy"
            />
            <ActivityCard
              title="Https 설정"
              project="S12P31A206"
              time="10:40:72"
              type="https"
            />
            <ActivityCard
              title="#132 MR 빌드"
              project="Project2"
              time="11:30:02"
              type="success"
            />
            <ActivityCard
              title="#133 MR 빌드"
              project="Project2"
              time="16:17:23"
              type="fail"
            />
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
`;

const WorkspaceGrid = styled.div`
  display: flex;
  gap: 1rem;

  overflow-x: auto;
  cursor: grab;
  -webkit-overflow-scrolling: touch;

  &::-webkit-scrollbar {
    display: none;
  }
`;

const DevelopmentSection = styled.section`
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  gap: 5rem;

  padding: 2rem 3rem;

  border: 1px solid ${({ theme }) => theme.colors.LightGray1};
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

  height: 30rem;

  overflow-y: auto;
  cursor: grab;
  -webkit-overflow-scrolling: touch;

  &::-webkit-scrollbar {
    display: none;
  }
`;

const TaskBox = styled.div`
  min-width: 30rem;

  display: flex;
  flex-direction: column;
  gap: 1.5rem;
`;
