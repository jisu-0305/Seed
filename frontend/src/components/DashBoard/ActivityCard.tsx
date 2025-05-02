import styled from '@emotion/styled';

// eslint-disable-next-line import/extensions
import { getActivityColor } from '@/utils/getColor';
// eslint-disable-next-line import/order
import { getRgbaFromhex } from '@/utils/getRgbaFromHex';

interface ActivityCardProps {
  title: string;
  project: string;
  time: string;
  type: string;
}

export const ActivityCard = ({
  title,
  project,
  time,
  type,
}: ActivityCardProps) => {
  const mainColor = getActivityColor(type);

  return (
    <CardWrapper bg={mainColor}>
      <Title color={mainColor}>
        <Icon src={`/assets/activitycard/ic_${type}.svg`} alt={`ic_${type}`} />
        {title}
      </Title>
      <Details color={mainColor}>
        <p>{project}</p>
        <p>•</p>
        <Icon
          src={`/assets/activitycard/ic_clock_${type}.svg`}
          alt={`ic_clock_${type}`}
        />
        <p>{time}</p>
      </Details>
      <Icon
        src={`/assets/activitycard/ic_check_${type}.svg`}
        alt={`ic_check_${type}`}
      />
    </CardWrapper>
  );
};

const CardWrapper = styled.div<{ bg: string }>`
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;

  min-height: 5rem;
  width: 40rem;
  padding: 1rem 2.5rem;

  background-color: ${({ bg }) => getRgbaFromhex(bg, 0.1)};
  border-radius: 2.4rem;
`;

const Icon = styled.img``;

const Title = styled.div<{ color: string }>`
  display: flex;
  align-items: center;
  gap: 1.5rem;

  ${({ theme }) => theme.fonts.Title5};
  color: ${({ color }) => color};
`;

const Details = styled.div<{ color: string }>`
  display: flex;
  flex-direction: row;
  justify-content: flex-end;
  gap: 0.5rem;

  color: ${({ color }) => color};
  ${({ theme }) => theme.fonts.EnTitle3};
  font-size: 1.8rem;
`;
