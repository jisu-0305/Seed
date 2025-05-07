import 'react-day-picker/dist/style.css';

import styled from '@emotion/styled';
import { DayPicker } from 'react-day-picker';

interface CalenderProps {
  selected: Date;
  onSelect: (date: Date) => void;
}

export const Calender = ({ selected, onSelect }: CalenderProps) => {
  return (
    <CalendarWrapper>
      <DayPicker
        mode="single"
        selected={selected}
        onSelect={onSelect}
        modifiersClassNames={{
          selected: 'rdp-day_selected',
          today: 'rdp-day_today',
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

  .rdp-day_selected {
    background-color: ${({ theme }) => theme.colors.Main_Carrot};
    color: white;
    border-radius: 100%;
  }

  .rdp-day_today {
    box-shadow: 0 0 0 1px ${({ theme }) => theme.colors.CalendarInactive};
    border-radius: 100%;
  }

  /* 둘 다 적용되는 경우 덮어쓰기 */
  .rdp-day_today.rdp-day_selected {
    background-color: ${({ theme }) => theme.colors.Main_Carrot};
    box-shadow: none;
  }
`;
