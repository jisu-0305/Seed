'use client';

import styled from '@emotion/styled';
import { useParams, useRouter } from 'next/navigation';

import { deleteProject, updateProject } from '@/apis/project';
import SmallButton from '@/components/Common/button/SmallButton';
import FunctionModal from '@/components/Common/Modal/FunctionModal';
import ModalWrapper from '@/components/Common/Modal/ModalWrapper';
import TipItem from '@/components/Common/TipItem';
import { useModal } from '@/hooks/Common';
import { useProjectInfoStore } from '@/stores/projectStore';
import { useThemeStore } from '@/stores/themeStore';
import { useUserStore } from '@/stores/userStore';
import { ProjectUpdateRequest } from '@/types/project';

import ProjectEditInput from './edit/ProjectEditInput';
import StepSidebar from './edit/StepSidebar';

export default function ProjectEdit() {
  const router = useRouter();
  const { mode } = useThemeStore();
  const params = useParams();
  const rawId = params?.id;
  const projectId = rawId ? Number(rawId) : null;

  const { stepStatus, setServerStatus, setAppStatus, setEnvStatus } =
    useProjectInfoStore();
  const user = useUserStore((s) => s.user);
  const ownerId = useProjectInfoStore((s) => s.stepStatus.ownerId);
  const deleteModal = useModal();

  const handleDelete = async () => {
    if (projectId === null) {
      console.error('projectId가 없습니다');
      return;
    }

    try {
      await deleteProject(projectId);
      console.log('✔️ 프로젝트 삭제 성공');
    } catch (err) {
      console.error('❌ 프로젝트 삭제 실패', err);
    } finally {
      router.replace('/dashboard');
    }
  };

  const handleComplete = async () => {
    if (projectId == null) return;

    const payload: ProjectUpdateRequest = {
      serverIP: stepStatus.server.ip,
      applications: stepStatus.app.map((app) => ({
        imageName: app.imageName,
        tag: app.tag,
        port: app.port,
      })),
    };

    try {
      await updateProject(projectId, payload);
      console.log('✔️ 프로젝트 수정 성공');
      router.back();
    } catch (err) {
      console.error('❌ 프로젝트 수정 실패', err);
    }
  };

  const onCompleteAndGoDetail = async () => {
    await handleComplete();
    router.push(`/projects/${projectId}`);
  };

  return (
    <Wrapper>
      <TitleHeader>
        <BackIcon
          src={`/assets/icons/ic_back_${mode}.svg`}
          alt="뒤로가기"
          onClick={() => router.back()}
        />
        <Heading>프로젝트 정보 수정</Heading>
        <TipItem text="최종 수정 완료 후 재빌드를 해주세요!" important />
      </TitleHeader>
      <Content>
        <Main>
          <ProjectEditInput
            server={stepStatus.server}
            env={stepStatus.env}
            apps={stepStatus.app}
            onChangeServer={setServerStatus}
            onChangeEnv={setEnvStatus}
            onChangeApps={setAppStatus}
          />
        </Main>
        <SideBarWrapper>
          <StepSidebar
            gitlab={stepStatus.gitlab}
            server={stepStatus.server}
            apps={stepStatus.app}
            env={stepStatus.env}
          />

          <StButtonWrapper>
            <SmallButton
              variant="cancel"
              onClick={() => {
                router.back();
              }}
            >
              취소
            </SmallButton>
            <SmallButton variant="next" onClick={onCompleteAndGoDetail}>
              완료
            </SmallButton>
          </StButtonWrapper>

          <StDeleteWrapper>
            <DeleteButton
              onClick={() => {
                if (user?.userId === ownerId) deleteModal.toggle();
              }}
            >
              프로젝트 삭제하기
            </DeleteButton>
            <StCautionWrapper>
              <IcIcon src="/assets/icons/ic_caution.svg" alt="caution" />
              {user?.userId === ownerId
                ? '프로젝트 삭제는 되돌릴 수 없습니다.'
                : '프로젝트는 소유자만 삭제 가능합니다.'}
            </StCautionWrapper>
          </StDeleteWrapper>
        </SideBarWrapper>
      </Content>

      {/* DeleteModal */}
      <ModalWrapper isShowing={deleteModal.isShowing}>
        <FunctionModal
          title="정말 삭제하시겠습니까?"
          isShowing={deleteModal.isShowing}
          handleClose={deleteModal.toggle}
        >
          <ModalContent>
            <ButtonRow>
              <SmallButton variant="cancel" onClick={deleteModal.toggle}>
                취소
              </SmallButton>
              <SmallButton
                variant="delete"
                onClick={() => {
                  deleteModal.toggle();
                  handleDelete();
                }}
              >
                삭제
              </SmallButton>
            </ButtonRow>
          </ModalContent>
        </FunctionModal>
      </ModalWrapper>
    </Wrapper>
  );
}

const Wrapper = styled.div`
  max-width: 100rem;

  display: flex;
  flex-direction: column;

  margin: 0 auto;
  padding: 2rem;
  padding-top: 5rem;
`;

const TitleHeader = styled.div`
  display: flex;
  justify-content: flex-start;
  align-items: center;
  width: 100%;
  max-height: 70rem;
  max-width: 110rem;
  margin-bottom: 3rem;
  gap: 2rem;
`;

const BackIcon = styled.img`
  width: 1.5rem;
  cursor: pointer;
`;

const Heading = styled.h2`
  ${({ theme }) => theme.fonts.EnTitle1};
`;

const Content = styled.div`
  display: flex;
  flex: 1;
  gap: 2rem;
`;

const Main = styled.main`
  flex: 6;
  display: flex;
  align-items: center;

  min-height: 50rem;

  /* padding: 0 3rem 2rem; */

  border: 1px solid ${({ theme }) => theme.colors.BorderDefault};
  border-radius: 1.5rem;
`;

const SideBarWrapper = styled.aside`
  flex: 4;
`;

const StButtonWrapper = styled.aside`
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  gap: 2rem;

  padding-top: 3rem;
`;

const StDeleteWrapper = styled.div`
  position: relative;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
`;

const DeleteButton = styled.button`
  margin-top: 5rem;
  padding: 1rem 5rem;
  ${({ theme }) => theme.fonts.Title5};
  color: ${({ theme }) => theme.colors.White};
  background-color: ${({ theme }) => theme.colors.RedBtn};
  border: none;
  border-radius: 1rem;
  cursor: pointer;

  &:hover {
    background-color: ${({ theme }) => theme.colors.RedBtnHover};
  }

  &:hover + div {
    visibility: visible;
    opacity: 1;
  }
`;

const IcIcon = styled.img`
  width: 3rem;

  margin-right: 0.5rem;
`;

const StCautionWrapper = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;

  width: 30rem;
  padding: 1rem;
  margin-top: 2rem;

  color: ${({ theme }) => theme.colors.Black1};
  ${({ theme }) => theme.fonts.Head5};

  background-color: ${({ theme }) => theme.colors.Red4};
  border-radius: 1rem;

  visibility: hidden;
`;

const ModalContent = styled.div`
  padding: 2rem;
  text-align: center;
  ${({ theme }) => theme.fonts.Body4};
`;

const ButtonRow = styled.div`
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin-top: 1.5rem;
`;
