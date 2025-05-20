import styled from '@emotion/styled';
import { useEffect } from 'react';

import {
  useProjectFileStore,
  useProjectInfoStore,
} from '@/stores/projectStore';

import FileInput from '../Common/FileInput';
import TipItem from '../Common/TipItem';

export default function EnvInput() {
  const { stepStatus, setEnvStatus, setOnNextValidate } = useProjectInfoStore();
  const { env } = stepStatus;

  const { setBackEnvFile, setFrontEnvFile } = useProjectFileStore();
  const { frontEnvFile, backEnvFile } = useProjectFileStore();

  const parseEnvFile = async (file: File): Promise<Set<string>> => {
    const text = await file.text();
    const lines = text.split('\n');
    const keys = lines
      .map((line) => line.trim())
      .filter((line) => line && !line.startsWith('#') && line.includes('='))
      .map((line) => line.split('=')[0].trim());

    return new Set(keys);
  };

  // 실 파일 존재 여부와 스토어 플래그 동기화
  useEffect(() => {
    setEnvStatus({
      ...env,
      frontEnv: Boolean(frontEnvFile),
      frontEnvName: frontEnvFile?.name ?? '',
      backEnv: Boolean(backEnvFile),
      backEnvName: backEnvFile?.name ?? '',
    });
  }, [frontEnvFile, backEnvFile]);

  // const handleNodeVersionChange = (e: ChangeEvent<HTMLInputElement>) => {
  //   setEnvStatus({ ...env, node: e.target.value });
  // };
  const handleNodeVersionChange = (version: string) => {
    setEnvStatus({ ...env, node: version });
  };

  const handleJdkChange = (version: string) => {
    setEnvStatus({ ...env, jdk: version });
  };

  const handleBuildToolChange = (tool: 'Gradle' | 'Maven') => {
    setEnvStatus({ ...env, buildTool: tool });
  };

  const handleClientEnvChange = (file: File) => {
    setEnvStatus({ ...env, frontEnv: !!file, frontEnvName: file.name });
    setFrontEnvFile(file);
  };

  const handleServerEnvChange = async (file: File) => {
    setEnvStatus({ ...env, backEnv: !!file, backEnvName: file.name });
    setBackEnvFile(file);

    const envKeys = await parseEnvFile(file);
    const { app } = useProjectInfoStore.getState().stepStatus;

    const missingKeys = new Set<string>();
    app.forEach((a) => {
      a.imageEnvs?.forEach((key) => {
        if (!envKeys.has(key)) {
          missingKeys.add(key);
        }
      });
    });

    if (missingKeys.size === 0) {
      alert('✅ 모든 환경변수가 정상적으로 포함되어 있습니다.');
    } else {
      alert(`❌ 누락된 환경변수:\n• ${Array.from(missingKeys).join('\n• ')}`);
    }
  };

  // const handleFrontFrameworkChange = (e: ChangeEvent<HTMLInputElement>) => {
  //   setEnvStatus({ ...env, frontendFramework: e.target.value });
  // };
  const handleFrontFrameworkChange = (framework: string) => {
    setEnvStatus({ ...env, frontendFramework: framework });
  };

  // 유효성 검사
  const isFormValid = () => {
    return (
      !!env.frontEnv &&
      !!env.node &&
      !!env.node &&
      !!env.backEnv &&
      !!env.buildTool
    );
  };

  // next 버튼 핸들러
  useEffect(() => {
    setOnNextValidate(isFormValid);
  }, [env]);

  return (
    <Container>
      <Section>
        <Title>Client</Title>
        <Row>
          <Label>Framework</Label>
          {/* <Input
            type="text"
            placeholder="react"
            value={env.frontendFramework || ''}
            onChange={handleFrontFrameworkChange}
          /> */}
          <RadioGroup>
            <label>
              <input
                type="radio"
                checked={env.frontendFramework === 'React'}
                onChange={() => handleFrontFrameworkChange('React')}
              />{' '}
              React
            </label>
            <label>
              <input
                type="radio"
                checked={env.frontendFramework === 'Vue.js'}
                onChange={() => handleFrontFrameworkChange('Vue.js')}
              />{' '}
              Vue.js
            </label>
            <label>
              <input
                type="radio"
                checked={env.frontendFramework === 'Next.js'}
                onChange={() => handleFrontFrameworkChange('Next.js')}
              />{' '}
              Next.js
            </label>
          </RadioGroup>
        </Row>
        <Row>
          <Label>Node.js version</Label>
          {/* <Input
            type="text"
            placeholder="22"
            value={env.node || ''}
            onChange={handleNodeVersionChange}
          /> */}
          <RadioGroup>
            <label>
              <input
                type="radio"
                checked={env.node === '22'}
                onChange={() => handleNodeVersionChange('22')}
              />{' '}
              22
            </label>
          </RadioGroup>
          버전 추가 예정
        </Row>
        <Row>
          <Label>환경변수</Label>
          <FileInput
            id="front"
            handleFileChange={handleClientEnvChange}
            accept=".env"
            placeholder="frontend.env"
            inputType="frontEnv"
          />
        </Row>
      </Section>

      <Section>
        <Title>Server</Title>
        <Row>
          <Label>JDK version</Label>
          <RadioGroup>
            <label>
              <input
                type="radio"
                checked={env.jdk === '17'}
                onChange={() => handleJdkChange('17')}
              />{' '}
              17
            </label>
            <label>
              <input
                type="radio"
                checked={env.jdk === '21'}
                onChange={() => handleJdkChange('21')}
              />{' '}
              21
            </label>
          </RadioGroup>
        </Row>
        <Row>
          <Label>환경변수</Label>
          <FileInput
            id="back"
            handleFileChange={handleServerEnvChange}
            accept=".env"
            placeholder="backend.env"
            inputType="backEnv"
          />
        </Row>
        <Row>
          <Label>빌드 도구</Label>
          <RadioGroup>
            <label>
              <input
                type="radio"
                checked={env.buildTool === 'Gradle'}
                onChange={() => {
                  handleBuildToolChange('Gradle');
                }}
              />{' '}
              Gradle
            </label>
            <label>
              <input
                type="radio"
                checked={env.buildTool === 'Maven'}
                onChange={() => {
                  handleBuildToolChange('Maven');
                }}
              />{' '}
              Maven
            </label>
          </RadioGroup>
        </Row>
      </Section>

      <TipList>
        <TipItem text="현재 사용하고 있는 key 정보를 담은 .env 파일을 업로드해주세요" />
        <TipItem text="버전 정보를 꼭 확인해주세요" important />
      </TipList>
    </Container>
  );
}

const Container = styled.div`
  width: 100%;
  padding: 3rem 4rem;
`;

const Section = styled.section`
  margin-bottom: 4rem;
`;

const Title = styled.h3`
  ${({ theme }) => theme.fonts.Head2};
  margin-bottom: 1rem;
`;

const Row = styled.div`
  display: flex;
  align-items: center;
  gap: 2rem;

  padding-left: 2rem;
  margin-bottom: 2rem;
`;

const Label = styled.label`
  min-width: fit-content;

  ${({ theme }) => theme.fonts.Title5};
`;

// const Input = styled.input`
//   flex: 1;
//   max-width: 10rem;
//   padding: 1rem 1.5rem;

//   ${({ theme }) => theme.fonts.Body1};
//   color: ${({ theme }) => theme.colors.Text};

//   background-color: ${({ theme }) => theme.colors.InputBackground};
//   border: 1px solid ${({ theme }) => theme.colors.InputStroke};
//   border-radius: 1rem;
// `;

const RadioGroup = styled.div`
  display: flex;
  gap: 1rem;

  /* input[type='radio'] {
    accent-color: ${({ theme }) => theme.colors.RadioSelected};
  } */

  input[type='radio'] {
    appearance: none;

    width: 2rem;
    height: 2rem;

    border: 2px solid ${({ theme }) => theme.colors.Text};
    border-radius: 50%;

    position: relative;

    cursor: pointer;

    &:checked {
      &::after {
        content: '';

        width: 1.2rem;
        height: 1.2rem;

        background-color: ${({ theme }) => theme.colors.Text};
        border-radius: 50%;

        position: absolute;
        top: 0.2rem;
        left: 0.2rem;
      }
    }
  }

  label {
    ${({ theme }) => theme.fonts.Body1};
    display: flex;
    align-items: center;
    gap: 1rem;
  }
`;

const TipList = styled.ul`
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
`;
