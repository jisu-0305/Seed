import styled from '@emotion/styled';
import { useRouter } from 'next/navigation';

import { getCardColor } from '@/utils/getColor';
import { formatDateTime } from '@/utils/getFormattedTime';

interface ProjectCardProps {
  id: number;
  emoji: string;
  title: string;
  time: string;
  build: string | null;
  https: boolean;
}

export const ProjectCard = ({
  id,
  emoji,
  title,
  time,
  build,
  https,
}: ProjectCardProps) => {
  const router = useRouter();
  const cardColor = getCardColor(emoji);

  return (
    <Card color={cardColor} onClick={() => router.push(`/projects/${id}`)}>
      <Title>
        <ProjectImage
          src={`/assets/projectcard/project_${emoji}.png`}
          alt={`project_${emoji}`}
        />
        {title}
      </Title>
      <Content>
        <BuildInfo>
          <h3>
            최근 빌드
            {time && (
              <IcStatus
                src={`/assets/icons/ic_build_${build ? 'success' : 'fail'}.svg`}
                alt={`project_${build ? 'success' : 'fail'}`}
              />
            )}
          </h3>
          <p>{time ? formatDateTime(time) : '빌드 이력 없음'}</p>
        </BuildInfo>
        <Info>
          <p>
            HTTPS
            <IcStatus
              src={`/assets/icons/ic_https_${https ? 'true' : 'false'}_light.svg`}
              alt={`https_${https ? 'true' : 'false'}`}
            />
          </p>
          <p>
            자동배포
            <IcStatus
              src="/assets/icons/ic_autoDeploy_light.svg"
              alt="auto deploy"
            />
          </p>
        </Info>
      </Content>
    </Card>
  );
};

const Card = styled.div<{ color: string }>`
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 1rem;

  padding: 2rem;
  width: 20rem;
  height: 10rem;

  background: ${({ color }) => color};
  border-radius: 1.6rem;

  cursor: pointer;
`;

const Title = styled.h3`
  display: flex;
  flex-direction: row;
  align-items: end;
  gap: 1rem;

  ${({ theme }) => theme.fonts.EnTitle2};
  color: ${({ theme }) => theme.colors.Black1};

  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
`;

const ProjectImage = styled.img`
  width: 3rem;

  padding-left: 1rem;
`;

const Content = styled.div`
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  gap: 1rem;
`;

const BuildInfo = styled.div`
  display: flex;
  flex-direction: column;
  align-items: start;
  gap: 0.5rem;

  h3 {
    display: flex;
    align-items: center;
    gap: 1rem;

    ${({ theme }) => theme.fonts.Head5};
    color: ${({ theme }) => theme.colors.Black1};
  }

  p {
    ${({ theme }) => theme.fonts.Body5};
    color: ${({ theme }) => theme.colors.Black1};
  }
`;

const IcStatus = styled.img``;

const Info = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;

  p {
    display: flex;
    align-items: center;
    gap: 1rem;

    ${({ theme }) => theme.fonts.Body4};
    color: ${({ theme }) => theme.colors.Black1};
  }
`;
