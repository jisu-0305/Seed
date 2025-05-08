import 'react-day-picker/dist/style.css';

import styled from '@emotion/styled';
import { DayPicker } from 'react-day-picker';

interface CalenderProps {
  selected: Date;
  onSelect: (date: Date) => void;
  activityDates: Date[];
  createdDates: Date[];
}

export const Calender = ({
  selected,
  onSelect,
  activityDates,
  createdDates,
}: CalenderProps) => {
  return (
    <CalendarWrapper>
      <DayPicker
        mode="single"
        selected={selected}
        onSelect={onSelect}
        modifiers={{
          activity: activityDates,
          created: createdDates,
        }}
        modifiersClassNames={{
          selected: 'rdp-day_selected',
          today: 'rdp-day_today',
          activity: 'rdp-day_activity',
          created: 'rdp-day_created',
        }}
        required
      />
    </CalendarWrapper>
  );
};

const CalendarWrapper = styled.div`
  .rdp-month_grid {
    margin-top: 1rem;
  }

  .rdp-button_previous .rdp-chevron polygon {
    fill: ${({ theme }) => theme.colors.Gray3};
  }

  .rdp-button_next .rdp-chevron polygon {
    fill: ${({ theme }) => theme.colors.CalenderDefault};
  }

  .rdp-caption_label {
    padding-left: 1.5rem;
    ${({ theme }) => theme.fonts.EnTitle2};
  }

  .rdp-weekday {
    ${({ theme }) => theme.fonts.EnBody1};
    color: ${({ theme }) => theme.colors.Gray3};
  }

  .rdp-month_grid {
    border-collapse: separate !important;
    border-spacing: 0.7rem;
  }

  .rdp-day {
    padding: 0.5rem;

    width: 3rem;
    height: 3rem;
    border-radius: 50%;

    ${({ theme }) => theme.fonts.EnBody1};
    color: ${({ theme }) => theme.colors.CalenderDefault};
  }

  //선택한 날짜
  .rdp-day_selected {
    background-color: ${({ theme }) => theme.colors.Main_Carrot};
    color: ${({ theme }) => theme.colors.White};
    border-radius: 100%;
  }

  //오늘 날짜
  .rdp-day_today {
    box-shadow: 0 0 0 1px ${({ theme }) => theme.colors.CalendarInactive};
    border-radius: 100%;
  }

  // 오늘 날짜 선택한 경우
  .rdp-day_today.rdp-day_selected {
    background-color: ${({ theme }) => theme.colors.Main_Carrot};
    box-shadow: none;
  }

  //활동 점
  .rdp-day_activity {
    position: relative;
    overflow: visible;
  }

  .rdp-day_activity::after {
    content: '';

    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);

    width: 0.5rem;
    height: 0.5rem;

    background-color: ${({ theme }) => theme.colors.Gray3};
    border-radius: 50%;
  }

  // 선택한 날에 활동이 있는 경우
  .rdp-day_selected.rdp-day_activity::after {
    display: none;
  }

  //생성일
  .rdp-day_created {
    position: relative;
    background-image: url('/assets/seed.png');
    background-repeat: no-repeat;
    background-position: center;
    background-size: 4rem 4rem;
    color: transparent;
  }

  //선택한 날이 생성일인 경우
  .rdp-day_selected.rdp-day_created {
    background-color: ${({ theme }) => theme.colors.Main_Carrot};
    box-shadow: none;

    background-image: none;
    color: ${({ theme }) => theme.colors.White};
  }
`;
