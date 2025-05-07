/* eslint-disable no-nested-ternary */
// src/components/AiReportPanel.tsx
import styled from '@emotion/styled';
import React, { useState } from 'react';

import { dummyReports } from '@/assets/dummy/aiReports';
import type { AiReport } from '@/types/aiReport';

export default function AiReport() {
  const [selectedId, setSelectedId] = useState<string>(dummyReports[0].id);
  const selected = dummyReports.find((r) => r.id === selectedId)!;

  return (
    <Container>
      <Wrapper>
        <LeftPanel>
          {dummyReports.map((r) => (
            <ReportItem
              key={r.id}
              active={r.id === selectedId}
              onClick={() => setSelectedId(r.id)}
            >
              <Date>{r.date}</Date>
              <Meta>
                <Icon>
                  <img src="/assets/icons/ic_ai_report.svg" alt="AI" />
                </Icon>
                <Title>
                  #{r.id} {r.title}
                </Title>
              </Meta>
              <Status status={r.status}>{r.status}</Status>
            </ReportItem>
          ))}
        </LeftPanel>

        <RightPanel>
          <Header>
            <Heading>{selected.title}</Heading>
          </Header>

          <Section>
            <SectionTitle>요약</SectionTitle>
            <SummaryBox>{selected.summary}</SummaryBox>
          </Section>

          <Section>
            <SectionTitle>적용된 파일</SectionTitle>
            <FileList>
              {selected.files.map((f) => (
                <FileItem key={f}>{f}</FileItem>
              ))}
            </FileList>
            <ConfirmButton>확인하기</ConfirmButton>
          </Section>

          <Section>
            <SectionTitle>추가 설명</SectionTitle>
            <DetailBox>{selected.detail}</DetailBox>
          </Section>
        </RightPanel>
      </Wrapper>
    </Container>
  );
}

const Container = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  width: 100%;
  padding-top: 5rem;
  padding-bottom: 5rem;
  background-color: ${({ theme }) => theme.colors.White};
  border-radius: 1.5rem;
`;

const Wrapper = styled.div`
  display: flex;
  max-height: 65rem;
  max-width: 100rem;
  overflow: hidden;
`;

const LeftPanel = styled.div`
  width: 220px;
  background: ${({ theme }) => theme.colors.White};
  border: 1px solid ${({ theme }) => theme.colors.BorderDefault};
  overflow-y: auto;

  /* WebKit 기반 브라우저 (Chrome, Safari) */
  &::-webkit-scrollbar {
    width: 8px;
    background: transparent;
  }

  /* 스크롤바 버튼(위/아래) 숨기기 */
  &::-webkit-scrollbar-button {
    display: none;
  }

  /* 스크롤바 thumb */
  &::-webkit-scrollbar-thumb {
    background-color: transparent;
    border-radius: 4px;
  }

  /* hover 시 thumb 보이기 */
  &:hover::-webkit-scrollbar-thumb {
    background-color: ${({ theme }) => theme.colors.Gray3};
  }

  /* Firefox: 기본에는 숨기기 */
  scrollbar-width: none;

  /* hover 시만 가늘게 표시 */
  &:hover {
    scrollbar-width: thin;
    scrollbar-color: ${({ theme }) => `${theme.colors.Gray3} transparent`};
  }
`;

const ReportItem = styled.div<{ active: boolean }>`
  padding: 1rem 1.5rem;
  border-bottom: 1px solid ${({ theme }) => theme.colors.BorderDefault};
  background: ${({ active, theme }) =>
    active ? theme.colors.LightGray2 : 'transparent'};
  cursor: pointer;

  &:hover {
    background: ${({ theme }) => theme.colors.BuildHover};
  }
`;

const Date = styled.div`
  ${({ theme }) => theme.fonts.Body4};
  color: ${({ theme }) => theme.colors.Gray3};
  margin-bottom: 0.25rem;
`;

const Meta = styled.div`
  display: flex;
  align-items: center;
  gap: 0.5rem;
`;

const Icon = styled.div`
  width: 1.5rem;
  height: 1.5rem;
`;

const Title = styled.div`
  ${({ theme }) => theme.fonts.Body2};
`;

const Status = styled.div<{ status: AiReport['status'] }>`
  ${({ theme }) => theme.fonts.Body4};
  color: ${({ status, theme }) =>
    status === 'In Progress'
      ? theme.colors.Main_Carrot
      : status === 'Merged'
        ? theme.colors.CalendarGreen
        : theme.colors.Gray3};
  margin-top: 0.5rem;
`;

const RightPanel = styled.div`
  flex: 1;
  padding: 1.5rem;
  overflow-y: auto;
`;

const Header = styled.div`
  margin-bottom: 3rem;
  display: flex;
`;

const Heading = styled.h2`
  ${({ theme }) => theme.fonts.EnTitle1};
`;

const Section = styled.div`
  margin-bottom: 4rem;
`;

const SectionTitle = styled.h3`
  ${({ theme }) => theme.fonts.Title3};
  margin-bottom: 0.75rem;
`;

const SummaryBox = styled.div`
  padding: 4rem;
  background: ${({ theme }) => theme.colors.BuildHover};
  border-radius: 1.5rem;
  ${({ theme }) => theme.fonts.Body1};
  line-height: 1.6;
`;

const FileList = styled.ul`
  list-style: disc inside;
  margin: 0 0 1rem;
  padding: 0;
  ${({ theme }) => theme.fonts.Body1};
`;

const FileItem = styled.li`
  margin-bottom: 0.5rem;
`;

const ConfirmButton = styled.button`
  padding: 0.6rem 1.5rem;
  border: none;
  border-radius: 1.5rem;
  background: ${({ theme }) => theme.colors.Black};
  color: ${({ theme }) => theme.colors.White};
  ${({ theme }) => theme.fonts.Body1};
`;

const DetailBox = styled.div`
  padding: 4rem;
  background: ${({ theme }) => theme.colors.BuildHover};
  border-radius: 1.5rem;
  ${({ theme }) => theme.fonts.Body1};
  white-space: pre-line;
`;
