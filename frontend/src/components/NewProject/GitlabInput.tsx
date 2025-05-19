import styled from '@emotion/styled';
import { useEffect, useRef, useState } from 'react';

import { getUserReposCursor } from '@/apis/gitlab';
import { useProjectInfoStore } from '@/stores/projectStore';
import { useThemeStore } from '@/stores/themeStore';

import CustomDropdown from './CustomDropdown';

interface Repo {
  id: number;
  name: string;
  path_with_namespace: string;
  http_url_to_repo: string;
  default_branch: string;
}

export default function GitlabInput() {
  const [repoList, setRepoList] = useState<Repo[]>([]);
  const [lastRepoId, setLastRepoId] = useState<number | null>(null);
  const [hasMore, setHasMore] = useState(true);
  const loader = useRef<HTMLLIElement | null>(null); // pagination
  const scrollContainerRef = useRef<HTMLUListElement>(null);

  const { stepStatus, setGitlabStatus, setOnNextValidate, resetProjectStatus } =
    useProjectInfoStore();
  const { gitlab } = stepStatus;

  const { mode } = useThemeStore();

  // 페이지가 마운트될 때 한 번만 초기화
  useEffect(() => {
    resetProjectStatus();
  }, [resetProjectStatus]);

  // repo 조회
  useEffect(() => {
    fetchUserRepos();
  }, []);

  const fetchUserRepos = async () => {
    const userStr = sessionStorage.getItem('user');
    if (!userStr) {
      console.error('세션스토리지에 “user” 키가 없습니다.');
      return;
    }

    let parsed: { state: { user: { userId: number } } };
    try {
      parsed = JSON.parse(userStr);
    } catch (e) {
      console.error('“user” JSON 파싱 실패', e);
      return;
    }

    const userId = parsed.state?.user?.userId;
    if (typeof userId !== 'number') {
      console.error('parsed.state.user.userId가 유효하지 않습니다.', parsed);
      return;
    }

    // getUserRepos(userId)
    //   .then((data) => {
    //     setRepoList(data);
    //   })
    //   .catch((err) => {
    //     console.error('레포 조회 실패', err);
    //   });
    try {
      const newRepos = await getUserReposCursor(
        userId,
        lastRepoId || undefined,
      );

      if (newRepos.length === 0) {
        setHasMore(false);
        return;
      }

      setRepoList((prev) => {
        const existingIds = new Set(prev.map((r) => r.id));
        const deduped = newRepos.filter((r: Repo) => !existingIds.has(r.id));
        return [...prev, ...deduped];
      });

      const last = newRepos[newRepos.length - 1];
      setLastRepoId(last.id);
    } catch (err) {
      console.error('레포 조회 실패', err);
    }
  };

  // input 핸들러

  const handleRepoSelect = (repoName: string) => {
    const selected = repoList.find((repo) => repo.name === repoName);
    if (!selected) return;

    setGitlabStatus({
      ...gitlab,
      id: selected.id,
      repo: selected.http_url_to_repo,
      defaultBranch: selected.default_branch,
    });
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

  useEffect(() => {
    const target = loader.current;
    const root = scrollContainerRef.current;

    if (!target || !hasMore) return undefined;

    const observer = new IntersectionObserver(
      (entries) => {
        const entry = entries[0];
        console.log('observer triggered:', entry.isIntersecting, entry);
        if (entry.isIntersecting) {
          fetchUserRepos();
        }
      },
      {
        root,
        threshold: 0.3,
      },
    );

    observer.observe(target);

    return () => {
      if (target) observer.unobserve(target);
    };
  }, [hasMore, repoList]);

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
        <CustomDropdown
          options={repoList.map((repo) => repo.name)}
          value={
            repoList.find((r) => r.http_url_to_repo === gitlab.repo)?.name ||
            '레포지토리를 선택해주세요.'
          }
          onChange={handleRepoSelect}
          width="100%"
          dropdownScrollRef={scrollContainerRef}
          loaderRef={loader}
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
              placeholder={gitlab.structure === '모노' ? 'frontend' : 'dev/fe'}
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
              placeholder={gitlab.structure === '모노' ? 'backend' : 'dev/be'}
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
