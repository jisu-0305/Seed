import styled from '@emotion/styled';
import { ChangeEvent, useEffect, useState } from 'react';

import { getUserRepos } from '@/apis/gitlab';
import { useProjectInfoStore } from '@/stores/projectStore';
import { useThemeStore } from '@/stores/themeStore';

interface Repo {
  id: number;
  name: string;
  path_with_namespace: string;
  http_url_to_repo: string;
}

export default function GitlabInput() {
  const [repoList, setRepoList] = useState<Repo[]>([]);

  const { stepStatus, setGitlabStatus, setOnNextValidate } =
    useProjectInfoStore();
  const { gitlab } = stepStatus;

  const { mode } = useThemeStore();

  // repo 조회
  useEffect(() => {
    fetchUserRepos();
  }, []);

  const fetchUserRepos = async () => {
    const { data } = await getUserRepos();
    setRepoList(data);
  };

  // input 핸들러
  const handleRepoChange = (e: ChangeEvent<HTMLSelectElement>) => {
    const url = e.target.value;

    setGitlabStatus({ ...gitlab, repo: url });
  };

  // const handleInput = (e: ChangeEvent<HTMLInputElement>) => {
  //   setGitlabStatus({ ...gitlab, repo: e.target.value });
  // };

  const handleStructureSelect = (value: '모노' | '멀티') => {
    setGitlabStatus({
      ...gitlab,
      structure: value,
      directory: {
        client: '',
        server: '',
      },
    });
  };

  const handleDirectoryChange = (type: 'client' | 'server', value: string) => {
    setGitlabStatus({
      ...gitlab,
      directory: {
        ...gitlab.directory,
        [type]: value,
      },
    });
  };

  // 유효성 검사
  const isFormValid = () => {
    return (
      !!gitlab.repo &&
      !!gitlab.structure &&
      !!gitlab.directory.client &&
      !!gitlab.directory.server
    );
  };

  // next 버튼 핸들러
  useEffect(() => {
    setOnNextValidate(isFormValid);
  }, [gitlab]);

  if (mode === null) return null;

  return (
    <Container>
      <Title>GitLab 레포지토리 주소를 입력해주세요</Title>
      {/* <Input
        placeholder="https://lab.ssafy.com/s12-final/S12P31A206.git"
        value={gitlab.repo}
        onChange={handleInput}
      /> */}

      <StSelectWrapper>
        <Select onChange={handleRepoChange} value={gitlab.repo}>
          <option value="" disabled>
            레포지토리를 선택하세요
          </option>
          {repoList.map((repo) => (
            <option key={repo.id} value={repo.http_url_to_repo}>
              {repo.name}
            </option>
          ))}
        </Select>
        <ArrowIcon
          src={`/assets/icons/ic_arrow_down_${mode}.svg`}
          alt="arrow"
        />
      </StSelectWrapper>
      <Title>프로젝트 폴더 구조는 무엇인가요?</Title>
      <OptionWrapper>
        <OptionBox
          selected={gitlab.structure === '모노'}
          onClick={() => handleStructureSelect('모노')}
        >
          <Label>모노</Label>
          <IcIcon
            src={`/assets/icons/ic_monoRepo_${mode}.svg`}
            alt="mono_type"
          />
        </OptionBox>
        <OptionBox
          selected={gitlab.structure === '멀티'}
          onClick={() => handleStructureSelect('멀티')}
        >
          <Label>멀티</Label>
          <IcIcon
            src={`/assets/icons/ic_multiRepo_${mode}.svg`}
            alt="multi_type"
          />
        </OptionBox>
      </OptionWrapper>

      <Title>
        {gitlab.structure === '모노'
          ? '디렉토리명을 입력해주세요'
          : '브랜치명을 입력해주세요'}
      </Title>

      <DirectoryWrapper>
        <InputBox>
          <InputLabel>Client</InputLabel>
          <InputWrapper>
            <IcIcon
              src={
                gitlab.structure === '모노'
                  ? `/assets/icons/ic_folder_${mode}.svg`
                  : `/assets/icons/ic_branch_${mode}.svg`
              }
              alt="repo structure"
            />
            <StyledInput
              placeholder={gitlab.structure === '모노' ? 'Frontend' : 'dev/fe'}
              value={gitlab.directory?.client || ''}
              onChange={(e) => handleDirectoryChange('client', e.target.value)}
            />
          </InputWrapper>
        </InputBox>
        <InputBox>
          <InputLabel>Server</InputLabel>
          <InputWrapper>
            <IcIcon
              src={
                gitlab.structure === '모노'
                  ? `/assets/icons/ic_folder_${mode}.svg`
                  : `/assets/icons/ic_branch_${mode}.svg`
              }
              alt="repo structure"
            />
            <StyledInput
              placeholder={gitlab.structure === '모노' ? 'Backend' : 'dev/be'}
              value={gitlab.directory?.server || ''}
              onChange={(e) => handleDirectoryChange('server', e.target.value)}
            />
          </InputWrapper>
        </InputBox>
      </DirectoryWrapper>
    </Container>
  );
}

const Container = styled.div`
  width: 100%;
  padding: 4rem;
`;

// const Input = styled.input``;

const Title = styled.h2`
  width: fit-content;
  margin: 3rem 0 1.5rem;

  &:first-of-type {
    margin-top: 0;
  }

  ${({ theme }) => theme.fonts.Head4};
`;

const StSelectWrapper = styled.div`
  width: 100%;
  position: relative;
`;

const Select = styled.select`
  width: 100%;
  padding: 1rem 1.5rem;

  ${({ theme }) => theme.fonts.Body3};
  color: ${({ theme }) => theme.colors.Text};

  background-color: ${({ theme }) => theme.colors.InputBackground};
  border: 1px solid ${({ theme }) => theme.colors.InputStroke};
  border-radius: 1rem;

  appearance: none;

  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  cursor: pointer;

  option {
    ${({ theme }) => theme.fonts.Body3};
    color: ${({ theme }) => theme.colors.Text};
  }
`;

const ArrowIcon = styled.img`
  position: absolute;
  right: 5%;
  top: 55%;
  transform: translateY(-50%);

  pointer-events: none;
`;

const OptionWrapper = styled.div`
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  gap: 2rem;

  width: 100%;
  max-width: 50rem;
`;

const OptionBox = styled.div<{ selected: boolean }>`
  flex: 1;

  display: flex;
  flex-direction: row;
  justify-content: space-around;
  align-items: center;

  height: 9rem;
  padding: 1rem 1.5rem;

  border: 2px solid
    ${({ selected, theme }) =>
      selected ? theme.colors.Text : theme.colors.InputStroke};
  border-radius: 1rem;

  cursor: pointer;
  opacity: ${({ selected }) => (selected ? 1 : 0.3)};
`;

const Label = styled.div`
  ${({ theme }) => theme.fonts.Title6};
  ${({ theme }) => theme.colors.Black1};
`;

const IcIcon = styled.img``;

const DirectoryWrapper = styled.div`
  display: flex;
  gap: 1.5rem;
  margin-top: 1rem;

  flex-wrap: wrap;
`;

const InputBox = styled.div`
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 1rem;
`;

const InputLabel = styled.div`
  ${({ theme }) => theme.fonts.Title5};
`;

const InputWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 1rem;

  padding: 1.2rem 1.5rem;

  background-color: ${({ theme }) => theme.colors.InputBackground};
  border: 1px solid ${({ theme }) => theme.colors.InputStroke};
  border-radius: 1rem;
`;

const StyledInput = styled.input`
  width: 100%;
  height: 2.5rem;

  ${({ theme }) => theme.fonts.Body1};
  color: ${({ theme }) => theme.colors.Text};
`;
