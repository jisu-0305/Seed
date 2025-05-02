import styled from '@emotion/styled';

import { User } from '@/types/user';
import { getCardColor } from '@/utils/getColor';

import { AvatarList } from './AvatarList';

interface ProjectCardProps {
  emoji: 'default' | 'success' | 'fail';
  title: string;
  time: string;
  https: boolean;
  status: string;
  users: User[];
}

export const ProjectCard = ({
  emoji,
  title,
  time,
  https,
  status,
  users,
}: ProjectCardProps) => {
  const cardColor = getCardColor(emoji);

  let buildIconSrc = '';
  if (status === 'SUCCESS') {
    buildIconSrc = '/assets/icons/ic_build_success.svg';
  } else if (status === 'FAILURE') {
    buildIconSrc = '/assets/icons/ic_build_fail.svg';
  }

  return (
    <Card color={cardColor}>
      <Left>
        <ProjectImage
          src={`/assets/projectcard/project_${emoji}.png`}
          alt={emoji}
        />
        <Title>{title}</Title>
        <AvatarList users={users} maxVisible={2} />
      </Left>

      <Right>
        <Item>
          자동배포{' '}
          <Icon src="/assets/icons/ic_autoDeploy.svg" alt="auto-deploy" />
        </Item>
        <Item>
          HTTPS{' '}
          <Icon
            src={`/assets/icons/ic_https_${https ? 'true' : 'false'}.svg`}
            alt="https"
          />
        </Item>
        <Item>
          최근 빌드{' '}
          {buildIconSrc && <Icon src={buildIconSrc} alt="build-status" />}
        </Item>
        <Time>{time}</Time>
      </Right>
    </Card>
  );
};

const Card = styled.div<{ color: string }>`
  display: flex;
  justify-content: space-between;
  align-items: center;

  padding: 3rem;
  background: ${({ color }) => color};
  border-radius: 1.6rem;
`;

const Left = styled.div`
  display: flex;
  align-items: center;
  gap: 1rem;
`;

const Right = styled.div`
  display: flex;
  align-items: center;
  gap: 2rem;
`;

const Title = styled.h3`
  ${({ theme }) => theme.fonts.EnTitle2};
`;

const ProjectImage = styled.img`
  width: 3rem;
  height: 3rem;
  object-fit: contain;
`;

const Icon = styled.img`
  width: 1.2rem;
  height: 1.2rem;
`;

const Item = styled.div`
  display: flex;
  align-items: center;
  gap: 0.5rem;
  ${({ theme }) => theme.fonts.Body4};
`;

const Time = styled.div`
  ${({ theme }) => theme.fonts.Body5};
`;
