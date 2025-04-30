import styled from '@emotion/styled';
import { useState } from 'react';

import { ActivityCard } from './ActivityCard';
import { Calender } from './Calender';
import { ProjectCard } from './ProjectCard';

export default function HomePage() {
  const [selectedDate, setSelectedDate] = useState(new Date());

  return (
    <PageWrapper>
      <WorkspaceSection>
        <SectionTitle>Workspace</SectionTitle>
        <WorkspaceGrid>
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
        </WorkspaceGrid>
      </WorkspaceSection>

      <DevelopmentSection>
        <CalendarBox>
          <SectionTitle>Development</SectionTitle>
          <Calender
            selected={selectedDate}
            onSelect={(d) => d && setSelectedDate(d)}
            tasks={null}
          />
        </CalendarBox>

        <TaskBox>
          <SectionTitle>Day {selectedDate.getDate()}</SectionTitle>
          <ActivityCard
            title="초기 배포설정"
            project="S12P31A206"
            time="10:30:24"
            status="success"
            color="#FFE8C7"
          />
          <ActivityCard
            title="Https 설정"
            project="S12P31A206"
            time="10:40:72"
            status="success"
            color="#C9F1E0"
          />
          <ActivityCard
            title="#132 MR 빌드"
            project="Project2"
            time="11:30:02"
            status="pending"
            color="#D5E3FF"
          />
          <ActivityCard
            title="#133 MR 빌드"
            project="Project2"
            time="16:17:23"
            status="fail"
            color="#FFD5D5"
          />
        </TaskBox>
      </DevelopmentSection>
    </PageWrapper>
  );
}

const PageWrapper = styled.div`
  padding: 40px;
  max-width: 1200px;
  margin: 0 auto;
`;

const SectionTitle = styled.h2`
  margin-bottom: 2rem;

  ${({ theme }) => theme.fonts.EnTitle1};
`;

const WorkspaceSection = styled.section`
  margin-bottom: 48px;
`;

const WorkspaceGrid = styled.div`
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
`;

const DevelopmentSection = styled.section`
  display: flex;
  gap: 32px;
  align-items: flex-start;
  flex-wrap: wrap;
`;

const CalendarBox = styled.div`
  flex: 1;
  min-width: 300px;
`;

const TaskBox = styled.div`
  flex: 1;
  min-width: 300px;
`;
