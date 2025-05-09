import styled from '@emotion/styled';
import { useState } from 'react';

import { getImageTag } from '@/apis/gitlab';
import { useModal } from '@/hooks/Common';
import { useProjectInfoStore } from '@/stores/projectStore';
import { useThemeStore } from '@/stores/themeStore';

import ModalWrapper from '../Common/Modal/ModalWrapper';
import TipItem from '../Common/TipItem';
import CustomDropdown from './CustomDropdown';
import InformInboundBriefModal from './Modal/InformInboundBriefModal';
import SearchDockerImageModal from './Modal/SearchDockerImageModal';

interface TagResponse {
  name: string;
  repository: number;
  v2: boolean;
  digest: string;
}

export default function AppInput() {
  const { mode } = useThemeStore();

  const { stepStatus, setAppStatus } = useProjectInfoStore();
  const { app: apps } = stepStatus;

  const inboundTip = useModal();
  const search = useModal();

  const [selectedIndex, setSelectedIndex] = useState<number | null>(null);
  const [tagList, setTagList] = useState<string[]>([]);
  const [port, setPort] = useState(8080);

  const handleAddApp = async (name: string) => {
    const existingIndex = apps.findIndex((a) => a.name === name);

    if (existingIndex !== -1) {
      const existingApp = apps[existingIndex];
      setSelectedIndex(existingIndex);
      setPort(existingApp.port);
      return;
    }

    const usedPorts = new Set(apps.map((a) => a.port));
    let assignedPort = 8080;
    while (usedPorts.has(assignedPort)) {
      assignedPort += 1;
    }

    const tagName = await fetchImageTag(name);

    const newApp = {
      name,
      tag: tagName,
      port: assignedPort,
    };

    const updatedApps = [...apps, newApp];
    setAppStatus(updatedApps);
    setSelectedIndex(updatedApps.length - 1);
    setPort(newApp.port);
  };

  // 이미지 선택시
  const fetchImageTag = async (tag: string) => {
    const { data } = await getImageTag(tag);
    setTagList(data.map((item: TagResponse) => item.name));

    return data[0].name;
  };

  const handleSelectApp = (index: number) => {
    setSelectedIndex(index);
    fetchImageTag(apps[index].name);
    setPort(apps[index].port);
  };

  // 이미지 삭제
  const handleDeleteApp = (index: number) => {
    const updated = [...apps];
    updated.splice(index, 1);
    setAppStatus(updated);

    if (selectedIndex === index) {
      setSelectedIndex(null);
    }
  };

  // 태그 선택시
  const handleTagChange = (value: string) => {
    if (selectedIndex !== null) {
      const updated = [...apps];
      updated[selectedIndex].tag = value;
      setAppStatus(updated);
    }
  };

  const handlePortChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const portNum = Number(e.target.value);
    const usedPorts = new Set(
      apps.map((a, i) => (i === selectedIndex ? null : a.port)),
    );

    if (usedPorts.has(portNum)) {
      alert('이미 사용 중인 포트입니다. 다른 포트를 입력해주세요.');
      return;
    }

    setPort(portNum);

    if (selectedIndex !== null) {
      const updated = [...apps];
      updated[selectedIndex].port = portNum;
      setAppStatus(updated);
    }
  };

  if (mode === null) return null;

  return (
    <>
      <Wrapper>
        <Title>등록한 어플리케이션</Title>
        <RegisteredList>
          {apps.map((app, idx) => (
            <Tag
              key={app.name}
              selected={idx === selectedIndex}
              onClick={() => handleSelectApp(idx)}
            >
              <AppName>{app.name} :</AppName>
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
                {apps[selectedIndex].name}
                <IcIcon src="/assets/icons/ic_official.svg" alt="lock" />
              </SelectedApp>
            </Row>

            <Row>
              <SelectWrapper>
                <Label>Tag</Label>
                <CustomDropdown
                  options={tagList}
                  value={apps[selectedIndex].tag}
                  onChange={handleTagChange}
                />
              </SelectWrapper>

              <div>
                <Label>포트번호</Label>
                <PortInput
                  type="number"
                  value={port}
                  min={1000}
                  max={9999}
                  onChange={handlePortChange}
                />
              </div>
            </Row>
          </>
        )}

        <TipList>
          <TipItem text="포트번호는 반드시 중복되지 않도록 설정해주세요" />
          <TipItem
            text="EC2에서 어플리케이션의 포트를 열어주세요"
            important
            help
            openModal={inboundTip.toggle}
          />
        </TipList>
      </Wrapper>
      <ModalWrapper isShowing={inboundTip.isShowing || search.isShowing}>
        <InformInboundBriefModal
          isShowing={inboundTip.isShowing}
          handleClose={inboundTip.toggle}
        />
        <SearchDockerImageModal
          isShowing={search.isShowing}
          handleClose={search.toggle}
          onSelect={handleAddApp}
        />
      </ModalWrapper>
    </>
  );
}

const Wrapper = styled.div`
  width: 100%;
  padding: 4rem;
`;

const Title = styled.h3`
  ${({ theme }) => theme.fonts.Head4};
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

  cursor: pointer;
`;

const SearchInput = styled.input`
  flex: 1;
  padding: 1rem;

  ${({ theme }) => theme.fonts.Body1};
  color: ${({ theme }) => theme.colors.Text};

  border: 1px solid ${({ theme }) => theme.colors.InputStroke};
  border-radius: 1rem;

  cursor: pointer;
`;

const SearchIcon = styled.img`
  margin-left: -4rem;
`;

const Row = styled.div`
  display: flex;
  align-items: center;
  /* gap: 3rem; */
  flex-wrap: wrap;

  margin-bottom: 1.5rem;

  &:last-of-type {
    gap: 2rem;
  }
`;

const Label = styled.label`
  min-width: fit-content;
  margin-right: 1.2rem;
  ${({ theme }) => theme.fonts.Head4}
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
  display: flex;
  flex-direction: row;
  align-items: center;

  position: relative;
  width: 20rem;
`;

const PortInput = styled.input`
  width: 8rem;
  padding: 1rem;

  ${({ theme }) => theme.fonts.Body1};
  color: ${({ theme }) => theme.colors.Text};
  text-align: center;

  background-color: ${({ theme }) => theme.colors.InputBackground};
  border: 1px solid ${({ theme }) => theme.colors.InputStroke};
  border-radius: 1rem;
`;

const TipList = styled.ul`
  display: flex;
  flex-direction: column;
  gap: 1rem;

  margin-top: 4rem;
`;
