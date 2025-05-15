import styled from '@emotion/styled';
import { useParams, useRouter } from 'next/navigation';
import React, { useEffect, useState } from 'react';

import { getAiReportDetail, getAiReports } from '@/apis/ai';
import { LoadingSpinner } from '@/components/Common/LoadingSpinner';
import { useThemeStore } from '@/stores/themeStore';
import type { AiReport, AiReportDetail } from '@/types/aiReport';
import { formatDateLong } from '@/utils/getFormattedTime';

const getIconName = (status: AiReport['status']) => {
  switch (status) {
    case 'SUCCESS':
      return 'success';
    case 'FAIL':
      return 'fail';
    default:
      return 'rejected';
  }
};

export default function AiReport() {
  const router = useRouter();
  const { mode } = useThemeStore();
  const params = useParams();
  const rawId = params?.id;
  const projectId = Array.isArray(rawId) ? rawId[0] : rawId;

  const [reports, setReports] = useState<AiReport[]>([]);
  const [listLoading, setListLoading] = useState(false);

  useEffect(() => {
    if (!projectId) return;

    setListLoading(true);
    getAiReports(projectId)
      .then((res) => {
        setReports(res);
      })

      .catch(console.error)
      .finally(() => setListLoading(false));
  }, [projectId]);

  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [detail, setDetail] = useState<AiReportDetail | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);

  // 최초 목록 로드 후 첫 아이템 자동 선택
  useEffect(() => {
    if (reports.length > 0 && selectedId === null) {
      setSelectedId(reports[0].id);
    }
  }, [reports, selectedId]);

  // selectedId 변경 시 상세 호출
  useEffect(() => {
    if (selectedId === null) return;

    setDetailLoading(true);
    getAiReportDetail(selectedId)
      .then((res) => setDetail(res))
      .catch(console.error)
      .finally(() => setDetailLoading(false));
  }, [selectedId]);

  if (listLoading) return <LoadingSpinner />;

  return (
    <Container>
      <TitleHeader>
        <BackIcon
          src={`/assets/icons/ic_back_${mode}.svg`}
          alt="뒤로가기"
          onClick={() => router.back()}
        />
        <TitleHeading>AI 보고서</TitleHeading>
      </TitleHeader>
      <Wrapper>
        <LeftPanel>
          {reports.map((r) => (
            <ReportItem
              key={r.id}
              active={r.id === selectedId}
              mode={mode ?? 'light'}
              onClick={() => setSelectedId(r.id)}
            >
              <ItemHeader>
                <Date>{formatDateLong(r.date)}</Date>
                <Id>#{r.id}</Id>
              </ItemHeader>
              <Meta>
                <Icon
                  src={`/assets/icons/ic_ai_report_${getIconName(r.status)}_${mode}.svg`}
                  alt={r.status}
                />
                <Title>{r.title}</Title>
              </Meta>
              <Status status={r.status}>{r.status}</Status>
            </ReportItem>
          ))}
        </LeftPanel>

        <RightPanel>
          {detailLoading || !detail ? (
            <LoadingSpinner />
          ) : (
            <>
              <Header>
                <Heading>{detail.title}</Heading>
              </Header>

              <Section>
                <SectionTitle>요약</SectionTitle>
                <SummaryBox>{detail.summary}</SummaryBox>
              </Section>

              <Section>
                <SectionRow>
                  <SectionTitle>적용된 파일</SectionTitle>
                  <ConfirmButton
                    onClick={() => {
                      const url =
                        detail.status === 'SUCCESS'
                          ? detail.mergeRequestUrl
                          : detail.commitUrl;
                      window.open(url, '_blank');
                    }}
                  >
                    확인하기
                  </ConfirmButton>
                </SectionRow>
                <FileList>
                  {detail.files.map((f) => (
                    <FileItem key={f}>{f}</FileItem>
                  ))}
                </FileList>
              </Section>

              <Section>
                <SectionTitle>추가 설명</SectionTitle>
                <DetailBox>{detail.detail}</DetailBox>
              </Section>
            </>
          )}
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
  padding: 5rem 2rem;
  border-radius: 1.5rem;
`;

const TitleHeader = styled.div`
  display: flex;
  justify-content: flex-start;
  align-items: center;
  width: 100%;
  max-height: 70rem;
  max-width: 110rem;
  margin-bottom: 3rem;
  padding-left: 5rem;
  gap: 2rem;
`;

const TitleHeading = styled.h2`
  ${({ theme }) => theme.fonts.EnTitle1};
`;

const BackIcon = styled.img`
  width: 1.5rem;
  cursor: pointer;
`;

const Wrapper = styled.div`
  display: flex;
  max-height: 65rem;
  max-width: 110rem;
  overflow: hidden;
`;

const LeftPanel = styled.div`
  max-width: 30rem;
  min-width: 22rem;
  background: ${({ theme }) => theme.colors.White};
  border: 1px solid ${({ theme }) => theme.colors.BorderDefault};
  border-radius: 1.5rem;
  overflow-y: auto;
  scrollbar-gutter: stable;

  & {
    scrollbar-width: none; /* Firefox */
    -ms-overflow-style: none; /* IE 10+ */
  }
`;

const ReportItem = styled.div<{ active: boolean; mode: string }>`
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 1.5rem 2.5rem;
  gap: 0.6rem;
  border-bottom: 1px solid ${({ theme }) => theme.colors.BorderDefault};
  background: ${({ active, mode, theme }) => {
    if (!active) {
      return theme.colors.Background;
    }
    return mode === 'light' ? theme.colors.LightGray2 : theme.colors.Gray0;
  }};
  cursor: pointer;

  &:hover {
    background: ${({ theme }) => theme.colors.BuildHover};
  }
`;

const ItemHeader = styled.div`
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  width: 100%;
`;

const Date = styled.div`
  ${({ theme }) => theme.fonts.Body4};
  color: ${({ theme }) => theme.colors.Gray3};
  margin-bottom: 0.25rem;
`;

const Id = styled.div`
  ${({ theme }) => theme.fonts.Body4};
  color: ${({ theme }) => theme.colors.Gray3};
  margin-bottom: 0.25rem;
`;

const Meta = styled.div`
  display: flex;
  align-items: center;
  gap: 2rem;
`;

const Icon = styled.img`
  width: 3rem;
  height: 3rem;
`;

const Title = styled.div`
  ${({ theme }) => theme.fonts.Body2};
`;

const Status = styled.div<{ status: AiReport['status'] }>`
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-end;
  margin-top: 0.5rem;
  width: 100%;
  gap: 0.7rem;

  ${({ theme }) => theme.fonts.Body4};
  color: ${({ status, theme }) =>
    status === 'SUCCESS'
      ? theme.colors.Green2
      : status === 'FAIL'
        ? theme.colors.Purple3
        : theme.colors.Gray3};

  &::before {
    content: '●';
    font-size: 0.8rem;
  }
`;

const RightPanel = styled.div`
  flex: 1;
  padding: 5rem;
  max-width: 70rem;
  min-width: 50rem;
  border: 1px solid ${({ theme }) => theme.colors.BorderDefault};
  border-radius: 1.5rem;
  overflow-y: auto;

  & {
    scrollbar-width: thin;
    scrollbar-color: ${({ theme }) =>
      `${theme.colors.BorderDefault} transparent`};
  }
`;

const Header = styled.div`
  margin-bottom: 3rem;
  display: flex;
  justify-content: center;
`;

const Heading = styled.h2`
  width: 55rem;
  text-align: center;
  ${({ theme }) => theme.fonts.EnTitle1};
`;

const Section = styled.div`
  margin-bottom: 5rem;
`;

const SectionTitle = styled.h3`
  ${({ theme }) => theme.fonts.Title3};
`;

const SectionRow = styled.div`
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 2rem;
  margin-bottom: 2rem;
`;

const SummaryBox = styled.div`
  padding: 4rem;
  background: ${({ theme }) => theme.colors.BuildHover};
  border-radius: 1.5rem;
  ${({ theme }) => theme.fonts.Body1};
  line-height: 1.6;
  margin-top: 2rem;
`;

const FileList = styled.ul`
  list-style: disc inside;
  margin: 0 0 1rem;
  padding: 0;
`;

const FileItem = styled.li`
  margin-bottom: 1rem;
  ${({ theme }) => theme.fonts.Body2};
`;

const ConfirmButton = styled.button`
  display: inline-flex;
  align-items: center;
  justify-content: center;

  width: 8rem;
  height: 3rem;
  border: none;
  border-radius: 1.5rem;
  background: ${({ theme }) => theme.colors.MenuBg};
  color: ${({ theme }) => theme.colors.MenuText};
  ${({ theme }) => theme.fonts.Body4};
`;

const DetailBox = styled.div`
  padding: 4rem;
  background: ${({ theme }) => theme.colors.BuildHover};
  border-radius: 1.5rem;
  ${({ theme }) => theme.fonts.Body1};
  white-space: pre-line;
  margin-top: 2rem;
`;
