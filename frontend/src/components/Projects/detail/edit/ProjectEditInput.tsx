import styled from '@emotion/styled';
import React, { useState } from 'react';

import FileInput from '@/components/Common/FileInput';
import { useProjectInfoStore } from '@/stores/projectStore';
import { useThemeStore } from '@/stores/themeStore';

export default function ProjectEditInput() {
  const { mode } = useThemeStore();

  const {
    stepStatus: { server, env, app: apps },
    setServerStatus,
    setEnvStatus,
    setAppStatus,
  } = useProjectInfoStore();

  const [selectedIndex, setSelectedIndex] = useState<number | null>(null);
  const [tag, setTag] = useState('latest');
  const [port, setPort] = useState(8080);

  const handleIpChange = (index: number, value: string) => {
    const numeric = value.replace(/\D/g, '');

    if (numeric !== '' && parseInt(numeric, 10) > 255) return;

    const ipParts = server.ip ? server.ip.split('.') : ['', '', '', ''];
    ipParts[index] = numeric;

    setServerStatus({
      ip: ipParts.map((p) => p || '').join('.'),
      pem: server.pem,
      pemName: server.pemName,
    });
  };

  const handleSelectApp = (index: number) => {
    setSelectedIndex(index);
    setTag(apps[index].tag);
    setPort(apps[index].port);
  };

  const handleDeleteApp = (index: number) => {
    const updated = [...apps];
    updated.splice(index, 1);
    setAppStatus(updated);
    setSelectedIndex(null);
  };

  const handleTagChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setTag(e.target.value);

    if (selectedIndex !== null) {
      const updated = [...apps];
      updated[selectedIndex].tag = e.target.value;
      setAppStatus(updated);
    }
  };

  const handlePortChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const portNum = Number(e.target.value);
    setPort(portNum);

    if (selectedIndex !== null) {
      const updated = [...apps];
      updated[selectedIndex].port = portNum;
      setAppStatus(updated);
    }
  };

  const handleClientEnvChange = (file: File) => {
    setEnvStatus({ ...env, frontEnv: !!file });
  };

  const handleServerEnvChange = (file: File) => {
    setEnvStatus({ ...env, frontEnv: !!file });
  };

  const ipParts = server.ip ? server.ip.split('.') : ['', '', '', ''];

  return (
    <Container>
      <Section>
        <Title>
          IP 주소 <SubText>포트는 22번으로 고정됩니다.</SubText>
        </Title>

        <IpInputWrapper>
          {ipParts.map((val, i) => (
            // eslint-disable-next-line react/no-array-index-key
            <React.Fragment key={`${val}-${i}`}>
              <IpBox
                maxLength={3}
                value={val}
                onChange={(e) => handleIpChange(i, e.target.value)}
              />
              {i !== ipParts.length - 1 && '.'}
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

        <SearchWrapper>
          <SearchInput placeholder="어플리케이션을 검색해주세요." readOnly />
          <SearchIcon
            src={`/assets/icons/ic_search_${mode}.svg`}
            alt="search"
          />
        </SearchWrapper>

        {selectedIndex !== null && (
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
                <Select value={tag} onChange={handleTagChange}>
                  <option value="latest">latest</option>
                  <option value="stable">stable</option>
                </Select>
                <ArrowIcon
                  src={`/assets/icons/ic_arrow_down_${mode}.svg`}
                  alt="arrow"
                />
              </SelectWrapper>

              <SelectWrapper>
                <Label>포트번호</Label>
                <PortInput
                  type="number"
                  value={port}
                  onChange={handlePortChange}
                />
              </SelectWrapper>
            </Row>
          </>
        )}
      </Section>

      <Section>
        <Row>
          <Label>Client 환경변수</Label>
          <FileInput
            handleFileChange={handleClientEnvChange}
            accept=".env"
            placeholder="frontend.env"
          />
        </Row>
        <Row>
          <Label>Server 환경변수</Label>
          <FileInput
            handleFileChange={handleServerEnvChange}
            accept=".env"
            placeholder="backend.env"
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

const Select = styled.select`
  width: 15rem;
  padding: 1rem;
  padding-right: 4rem;

  ${({ theme }) => theme.fonts.Body1};
  color: ${({ theme }) => theme.colors.Text};
  text-align: center;

  background-color: ${({ theme }) => theme.colors.InputBackground};
  border: 1px solid ${({ theme }) => theme.colors.InputStroke};
  border-radius: 1rem;

  appearance: none;

  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  cursor: pointer;
`;

const ArrowIcon = styled.img`
  position: absolute;
  right: 3rem;
  top: 55%;
  transform: translateY(-50%);

  pointer-events: none;
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
