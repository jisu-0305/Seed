import styled from '@emotion/styled';

// eslint-disable-next-line import/extensions
import { getCardColor } from '@/utils/getColor';

interface ProjectCardProps {
  emoji: string;
  title: string;
  time: string;
  build: boolean;
  https: boolean;
}

export const ProjectCard = ({
  emoji,
  title,
  time,
  build,
  https,
}: ProjectCardProps) => {
  const cardColor = getCardColor(emoji);

  return (
    <Card color={cardColor}>
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
            <IcStatus
              src={`/assets/icons/ic_build_${build ? 'success' : 'fail'}.svg`}
              alt={`project_${build ? 'success' : 'fail'}`}
            />
          </h3>

          <p>{time}</p>
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
            <IcStatus src="/assets/icons/ic_autoDeploy.svg" alt="auto deploy" />
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
  /* min-width: 20rem; */
  height: 10rem;

  background: ${({ color }) => color};
  border-radius: 1.6rem;
`;

const Title = styled.h3`
  display: flex;
  flex-direction: row;
  align-items: end;
  gap: 1rem;

  ${({ theme }) => theme.fonts.EnTitle2};
  color: ${({ theme }) => theme.colors.Black1};
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
