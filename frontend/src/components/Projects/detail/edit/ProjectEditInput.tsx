import styled from '@emotion/styled';
import React, { useEffect, useState } from 'react';

import { getImageTag } from '@/apis/gitlab';
import FileInput from '@/components/Common/FileInput';
import ModalWrapper from '@/components/Common/Modal/ModalWrapper';
import CustomDropdown from '@/components/NewProject/CustomDropdown';
import SearchDockerImageModal from '@/components/NewProject/Modal/SearchDockerImageModal';
import { useModal } from '@/hooks/Common';
import { useProjectFileStore } from '@/stores/projectStore';
import { useThemeStore } from '@/stores/themeStore';
import { ApplicationWithDefaults, EnvInfo, ServerInfo } from '@/types/project';

export interface ProjectEditInputProps {
  server: ServerInfo;
  env: EnvInfo;
  apps: ApplicationWithDefaults[];

  // 변경된 값을 상위(스토어)에 반영할 콜백
  onChangeServer: (s: ServerInfo) => void;
  onChangeEnv: (e: EnvInfo) => void;
  onChangeApps: (a: ApplicationWithDefaults[]) => void;
}

export default function ProjectEditInput({
  server,
  env,
  apps,
  onChangeServer,
  onChangeEnv,
  onChangeApps,
}: ProjectEditInputProps) {
  const { mode } = useThemeStore();
  const search = useModal();

  const { setBackEnvFile, setFrontEnvFile } = useProjectFileStore();
  const { frontEnvFile, backEnvFile } = useProjectFileStore();

  // 실 파일 존재 여부와 스토어 플래그 동기화
  useEffect(() => {
    onChangeEnv({
      ...env,
      frontEnv: Boolean(frontEnvFile),
      frontEnvName: frontEnvFile?.name ?? '',
      backEnv: Boolean(backEnvFile),
      backEnvName: backEnvFile?.name ?? '',
    });
  }, [frontEnvFile, backEnvFile]);

  // IP 파트
  const [ipParts, setIpParts] = useState<string[]>(() => server.ip.split('.'));
  useEffect(() => setIpParts(server.ip.split('.')), [server.ip]);
  const handleIpChange = (i: number, v: string) => {
    const num = v.replace(/\D/g, '');
    if (num && +num > 255) return;
    const u = [...ipParts];
    u[i] = num;
    setIpParts(u);
    onChangeServer({ ...server, ip: u.join('.') });
  };

  // 앱 선택 + 태그 리스트
  const [selectedIndex, setSelectedIndex] = useState<number | null>(null);
  const [tagList, setTagList] = useState<string[]>([]);

  useEffect(() => {
    if (
      selectedIndex !== null &&
      (selectedIndex < 0 || selectedIndex >= apps.length)
    ) {
      setSelectedIndex(null);
    }
  }, [apps, selectedIndex]);

  const fetchImageTag = async (imageName: string) => {
    const { data } = await getImageTag(imageName);
    const tags = data.map((t: { name: string }) => t.name);
    setTagList(tags);
    return tags[0]!;
  };

  // 앱 추가
  const handleAddApp = async (img: ApplicationWithDefaults) => {
    const idx = apps.findIndex((a) => a.imageName === img.imageName);
    if (idx > -1) {
      setSelectedIndex(idx);
      fetchImageTag(img.imageName);
      return;
    }
    const initialTag = await fetchImageTag(img.imageName);
    const initialPort = img.defaultPorts[0] || 8080;
    const newApp: ApplicationWithDefaults = {
      ...img,
      tag: initialTag,
      port: initialPort,
      imageEnvs: img.imageEnvs,
    };
    const updated = [...apps, newApp];
    onChangeApps(updated);
    setSelectedIndex(updated.length - 1);
  };

  const handleSelectApp = (index: number) => {
    setSelectedIndex(index);
    fetchImageTag(apps[index].imageName);
  };

  // 앱 삭제
  const handleDeleteApp = (index: number) => {
    const updated = [...apps];
    updated.splice(index, 1);
    onChangeApps(updated);

    if (selectedIndex === index) {
      setSelectedIndex(null);
    }
  };

  // 태그 선택
  const handleTagChange = (value: string) => {
    if (selectedIndex == null) return;
    const u = [...apps];
    u[selectedIndex].tag = value;
    onChangeApps(u);
  };

  // 포트 선택
  const handlePortSelect = (value: string) => {
    if (selectedIndex == null) return;
    const portNum = Number(value);
    const used = new Set(
      apps.map((a, idx) => (idx === selectedIndex ? null : a.port)),
    );
    if (used.has(portNum)) {
      alert('이미 사용 중인 포트입니다.');
      return;
    }
    const u = [...apps];
    u[selectedIndex].port = portNum;
    onChangeApps(u);
  };

  // Env 파일
  const handleClientEnvChange = (file: File) => {
    onChangeEnv({ ...env, frontEnv: !!file, frontEnvName: file.name });
    setFrontEnvFile(file);
  };

  const handleServerEnvChange = async (file: File) => {
    onChangeEnv({ ...env, backEnv: !!file, backEnvName: file.name });
    setBackEnvFile(file);

    const text = await file.text();
    const envKeys = new Set(
      text
        .split('\n')
        .map((line) => line.trim())
        .filter((line) => line && !line.startsWith('#') && line.includes('='))
        .map((line) => line.split('=')[0].trim()),
    );

    const missingKeys = new Set<string>();
    apps.forEach((app) => {
      app.imageEnvs?.forEach((key) => {
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

  return (
    <Container>
      <Section>
        <Title>
          IP 주소 <SubText>포트는 22번으로 고정됩니다.</SubText>
        </Title>

        <IpInputWrapper>
          {ipParts.map((part, i) => (
            // eslint-disable-next-line react/no-array-index-key
            <React.Fragment key={i}>
              <IpBox
                maxLength={3}
                value={part}
                onChange={(e) => handleIpChange(i, e.target.value)}
              />
              {i < 3 && '.'}
            </React.Fragment>
          ))}
        </IpInputWrapper>
      </Section>

      <Section>
        <Title>등록한 어플리케이션</Title>
        <RegisteredList>
          {apps.map((app, idx) => (
            <Tag
              key={app.imageName}
              selected={idx === selectedIndex}
              onClick={() => handleSelectApp(idx)}
            >
              <AppName>{app.imageName} :</AppName>
              {app.tag}
              <CloseButton onClick={() => handleDeleteApp(idx)}>x</CloseButton>
            </Tag>
          ))}
        </RegisteredList>

        <SearchWrapper onClick={search.toggle}>
          <SearchInput placeholder="어플리케이션을 검색해주세요." readOnly />
          <SearchIcon
            src={`/assets/icons/ic_search_${mode}.svg`}
            alt="search"
          />
        </SearchWrapper>

        {selectedIndex !== null && apps[selectedIndex] && (
          <>
            <Row>
              <Label>선택한 어플리케이션</Label>
              <SelectedApp>
                {apps[selectedIndex].imageName}
                <IcIcon src="/assets/icons/ic_official.svg" alt="lock" />
              </SelectedApp>
            </Row>

            <Row>
              <SelectWrapper>
                <Label>Tag</Label>
                <CustomDropdown
                  options={tagList}
                  value={apps[selectedIndex].tag || ''}
                  onChange={handleTagChange}
                />
              </SelectWrapper>

              <SelectWrapper>
                <Label>포트번호</Label>
                <CustomDropdown
                  options={apps[selectedIndex].defaultPorts.map(String)}
                  value={String(apps[selectedIndex].port)}
                  onChange={handlePortSelect}
                />
              </SelectWrapper>
            </Row>
            {apps[selectedIndex].imageEnvs?.length > 0 && (
              <EnvDisplayRow>
                <Label>필수 환경변수</Label>
                <EnvList>
                  {apps[selectedIndex].imageEnvs.map((envKey) => (
                    <EnvItem key={envKey}>{envKey}</EnvItem>
                  ))}
                </EnvList>
              </EnvDisplayRow>
            )}
          </>
        )}
      </Section>

      <ModalWrapper isShowing={search.isShowing}>
        <SearchDockerImageModal
          isShowing={search.isShowing}
          handleClose={search.toggle}
          onSelect={(img) => {
            handleAddApp(img);
          }}
        />
      </ModalWrapper>

      <Section>
        <Row>
          <Label>Client 환경변수</Label>
          <FileInput
            id="front"
            handleFileChange={handleClientEnvChange}
            accept=".env"
            placeholder="frontend.env"
            inputType="frontEnv"
          />
        </Row>
        <Row>
          <Label>Server 환경변수</Label>
          <FileInput
            id="back"
            handleFileChange={handleServerEnvChange}
            accept=".env"
            placeholder="backend.env"
            inputType="backEnv"
          />
        </Row>
      </Section>
    </Container>
  );
}

const Container = styled.div`
  width: 100%;
  padding: 4rem;
`;

const Section = styled.section`
  margin-bottom: 3rem;

  &:not(:first-of-type) {
    padding-top: 3rem;
    border-top: 1px solid ${({ theme }) => theme.colors.BorderDefault};
  }

  &:last-of-type {
    margin-bottom: 0;
  }
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

const Title = styled.h3`
  width: fit-content;
  margin: 4rem 0 2rem;

  &:first-of-type {
    margin-top: 0;
  }

  ${({ theme }) => theme.fonts.Head4};
`;

const SubText = styled.span`
  ${({ theme }) => theme.fonts.Body6};
  color: ${({ theme }) => theme.colors.Gray3};

  margin-left: 1rem;
`;

const IpInputWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
`;

const IpBox = styled.input`
  width: 6rem;
  padding: 1rem;
  text-align: center;

  ${({ theme }) => theme.fonts.Body1};
  color: ${({ theme }) => theme.colors.Text};

  background-color: ${({ theme }) => theme.colors.InputBackground};
  border: 1px solid ${({ theme }) => theme.colors.InputStroke};
  border-radius: 1rem;
`;

const RegisteredList = styled.div`
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;

  margin-top: 1rem;
  margin-bottom: 4rem;
`;

const Tag = styled.div<{ selected?: boolean }>`
  display: flex;
  align-items: center;
  gap: 1rem;

  padding: 0.8rem 1.2rem;

  ${({ theme }) => theme.fonts.Body1};

  background-color: ${({ theme }) => theme.colors.InputBackground};
  border: 1px solid
    ${({ selected, theme }) =>
      selected ? theme.colors.Text : theme.colors.InputStroke};
  border-radius: 1rem;

  cursor: pointer;
`;

const AppName = styled.h3`
  ${({ theme }) => theme.fonts.Title5};
`;

const CloseButton = styled.span`
  ${({ theme }) => theme.fonts.Body1};

  padding-bottom: 0.2rem;

  cursor: pointer;
`;

const SearchWrapper = styled.div`
  display: flex;
  align-items: center;

  margin-bottom: 2rem;
`;

const SearchInput = styled.input`
  flex: 1;
  padding: 1rem;

  ${({ theme }) => theme.fonts.Body1};
  color: ${({ theme }) => theme.colors.Text};

  border: 1px solid ${({ theme }) => theme.colors.InputStroke};
  border-radius: 1rem;
`;

const SearchIcon = styled.img`
  margin-left: -4rem;

  cursor: pointer;
`;

const SelectedApp = styled.div`
  display: flex;
  align-items: center;
  gap: 0.5rem;

  padding: 0.7rem 0.5rem 0.7rem 1rem;

  ${({ theme }) => theme.fonts.Title5};

  border: 1px solid ${({ theme }) => theme.colors.Text};
  border-radius: 1rem;
`;

const IcIcon = styled.img`
  margin-top: 0.2rem;
`;

const SelectWrapper = styled.div`
  position: relative;
  width: 20rem;
  display: flex;
  align-items: center;
  gap: 1rem;
`;

const EnvDisplayRow = styled.div`
  display: flex;
  align-items: flex-start;
  gap: 2rem;
  padding-left: 2rem;
  margin-bottom: 2rem;
`;

const EnvList = styled.div`
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
`;

const EnvItem = styled.div`
  padding: 0.5rem 1rem;
  background-color: ${({ theme }) => theme.colors.InputBackground};
  border: 1px solid ${({ theme }) => theme.colors.InputStroke};
  border-radius: 0.8rem;

  ${({ theme }) => theme.fonts.Body3};
  color: ${({ theme }) => theme.colors.Text};
`;
