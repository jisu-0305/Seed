import 'swiper/css';

import styled from '@emotion/styled';
import { useState } from 'react';
import { Swiper, SwiperSlide } from 'swiper/react';

import { useVerticalDragScroll } from '@/hooks/Common/useVerticalDragScroll';

import { ActivityCard } from './ActivityCard';
import { Calender } from './Calender';
import { ProjectCard } from './ProjectCard';

const activityDates = [
  new Date(2025, 4, 4),
  new Date(2025, 4, 6),
  new Date(2025, 4, 12),
  new Date(2025, 4, 14),
  new Date(2025, 4, 24),
];

const createdDates = [new Date(2025, 4, 6), new Date(2025, 4, 22)];

export default function HomePage() {
  const verticalDragRef = useVerticalDragScroll<HTMLDivElement>();

  const [selectedDate, setSelectedDate] = useState(new Date());

  return (
    <PageWrapper>
      <WorkspaceSection>
        <SectionTitle>Workspace</SectionTitle>
        <Swiper
          spaceBetween={28}
          slidesPerView={4}
          grabCursor
          style={{ paddingBottom: '1rem' }}
        >
          <SwiperSlide>
            <ProjectCard
              emoji="default"
              title="S12P31A206"
              time="03.20 10:02:75"
              build
              https
            />
          </SwiperSlide>
          <SwiperSlide>
            <ProjectCard
              emoji="fail"
              title="Project 2"
              time="03.20 10:02:75"
              build={false}
              https={false}
            />
          </SwiperSlide>
          <SwiperSlide>
            <ProjectCard
              emoji="success"
              title="Project 3"
              time="10일 전"
              build
              https
            />
          </SwiperSlide>
          <SwiperSlide>
            <ProjectCard
              emoji="success"
              title="Project 4"
              time="10일 전"
              build
              https
            />
          </SwiperSlide>
          <SwiperSlide>
            <ProjectCard
              emoji="success"
              title="Project 4"
              time="10일 전"
              build
              https
            />
          </SwiperSlide>
        </Swiper>
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
  width: 100%;
  overflow: hidden;
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

  height: 30rem;

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

  padding-bottom: 2rem;
`;
