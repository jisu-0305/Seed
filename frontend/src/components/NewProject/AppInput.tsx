import styled from '@emotion/styled';
import { useState } from 'react';

import { useProjectInfoStore } from '@/stores/projectStore';
import { useThemeStore } from '@/stores/themeStore';

import TipItem from './TipItem';

const dummyApps = [
  {
    name: 'redis',
    tag: 'latest',
    port: 8081,
  },
  {
    name: 'mysql',
    tag: 'stable',
    port: 3306,
  },
  {
    name: 'elastic search',
    tag: 'latest',
    port: 9200,
  },
];

export default function AppInput() {
  const { mode } = useThemeStore();

  const { setAppStatus } = useProjectInfoStore();
  const apps = dummyApps;
  //   const { app } = stepStatus;

  const [selectedIndex, setSelectedIndex] = useState<number | null>(null);
  const [tag, setTag] = useState('latest');
  const [port, setPort] = useState(8080);

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

  if (mode === null) return null;

  return (
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
            {app.tag === 'latest' ? 'LTS' : app.tag}
            <CloseButton onClick={() => handleDeleteApp(idx)}>x</CloseButton>
          </Tag>
        ))}
      </RegisteredList>

      <SearchWrapper>
        <SearchInput placeholder="어플리케이션을 검색해주세요." readOnly />
        <SearchIcon src={`/assets/icons/ic_search_${mode}.svg`} alt="search" />
      </SearchWrapper>

      {selectedIndex !== null && (
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
              <Select value={tag} onChange={handleTagChange}>
                <option value="latest">latest</option>
                <option value="stable">stable</option>
              </Select>
              <ArrowIcon
                src={`/assets/icons/ic_arrow_down_${mode}.svg`}
                alt="arrow"
              />
            </SelectWrapper>

            <div>
              <Label>포트번호</Label>
              <PortInput
                type="number"
                value={port}
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
        />
      </TipList>
    </Wrapper>
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

const Row = styled.div`
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;

  margin-bottom: 1.5rem;
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
  position: relative;
  width: 20rem;
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

const TipList = styled.ul`
  display: flex;
  flex-direction: column;
  gap: 1rem;

  margin-top: 4rem;
`;
