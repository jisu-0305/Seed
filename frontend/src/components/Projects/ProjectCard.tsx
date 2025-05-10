import styled from '@emotion/styled';
import { useRouter } from 'next/navigation';

import { ProjectMember } from '@/types/project';
import { getCardColor } from '@/utils/getColor';

import { AvatarList } from './AvatarList';

interface ProjectCardProps {
  id: number;
  emoji: 'default' | 'success' | 'fail';
  title: string;
  time: string;
  https: boolean;
  status: string;
  users: ProjectMember[];
}

export const ProjectCard = ({
  id,
  emoji,
  title,
  time,
  https,
  status,
  users,
}: ProjectCardProps) => {
  const router = useRouter();
  const cardColor = getCardColor(emoji);

  let buildIconSrc = '';
  if (status === 'SUCCESS') {
    buildIconSrc = '/assets/icons/ic_build_success.svg';
  } else if (status === 'FAILURE') {
    buildIconSrc = '/assets/icons/ic_build_fail.svg';
  }

  const handleClick = () => {
    router.push(`/projects/${id}`);
  };

  return (
    <Card color={cardColor} onClick={handleClick}>
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
          <Icon src="/assets/icons/ic_autoDeploy_light.svg" alt="auto-deploy" />
        </Item>
        <Item>
          HTTPS{' '}
          <Icon
            src={`/assets/icons/ic_https_${https ? 'true' : 'false'}_light.svg`}
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
  cursor: pointer;
`;

const Left = styled.div`
  display: flex;
  align-items: center;
  gap: 2rem;
`;

const Right = styled.div`
  display: flex;
  align-items: center;
  gap: 2rem;
`;

const Title = styled.h3`
  ${({ theme }) => theme.fonts.EnTitle2};
  color: ${({ theme }) => theme.colors.Black};
`;

const ProjectImage = styled.img`
  width: 4rem;
  height: 4rem;
  object-fit: contain;
`;

const Icon = styled.img`
  width: 2rem;
  height: 2rem;
`;

const Item = styled.div`
  display: flex;
  align-items: center;
  gap: 0.5rem;
  ${({ theme }) => theme.fonts.Body4};
  color: ${({ theme }) => theme.colors.Black};
`;

const Time = styled.div`
  ${({ theme }) => theme.fonts.Body5};
  color: ${({ theme }) => theme.colors.Black};
`;
