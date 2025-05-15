/* eslint-disable react/no-array-index-key */
import styled from '@emotion/styled';
import { ChangeEvent, useEffect, useState } from 'react';

import {
  fetchInvitationCandidates,
  fetchProjectUsers,
  sendInvitations,
} from '@/apis/user';
import SmallModal from '@/components/Common/Modal/SmallModal';
import { ProjectMember } from '@/types/project';

interface InviteModalProps {
  projectId: string | null;
  isShowing: boolean;
  handleClose: () => void;
}

const TeamInviteModal = ({
  projectId,
  isShowing,
  handleClose,
}: InviteModalProps) => {
  const [query, setQuery] = useState('');
  const [filtered, setFiltered] = useState<ProjectMember[]>([]);
  const [selectedUsers, setSelectedUsers] = useState<ProjectMember[]>([]);
  const [projectUsers, setProjectUsers] = useState<ProjectMember[]>([]);

  const handleSelectUser = (user: ProjectMember) => {
    if (selectedUsers.some((u) => u.userId === user.userId)) return;
    setSelectedUsers((prev) => [...prev, user]);
    setQuery('');
    setFiltered([]);
  };

  const handleRemoveSelected = (userId: number) => {
    setSelectedUsers((prev) => prev.filter((user) => user.userId !== userId));
  };

  const handleSearch = async (e: ChangeEvent<HTMLInputElement>) => {
    const keyword = e.target.value;
    setQuery(keyword);
    if (!projectId) {
      setFiltered([]);
      return;
    }
    const id = Number(projectId);
    if (Number.isNaN(id)) {
      setFiltered([]);
      return;
    }

    try {
      const candidates = await fetchInvitationCandidates(id, keyword);
      setFiltered(candidates);
    } catch (err) {
      console.error('초대 가능 사용자 조회 실패', err);
      setFiltered([]);
    }
  };

  const handleDone = async () => {
    if (!projectId) return;
    const id = Number(projectId);
    if (Number.isNaN(id)) return;

    const idList = selectedUsers.map((u) => u.userId);
    try {
      await sendInvitations(id, idList);
      handleClose();
    } catch (err) {
      console.error('초대 요청 실패', err);
    }

    setSelectedUsers([]);
    setFiltered([]);
    setQuery('');
  };

  useEffect(() => {
    if (!isShowing || !projectId) return;
    const id = Number(projectId);
    if (Number.isNaN(id)) {
      console.warn(`Invalid projectId: ${projectId}`);
      return;
    }

    fetchProjectUsers(id).then(setProjectUsers);
  }, [isShowing, projectId]);

  return (
    isShowing && (
      <SmallModal
        title="팀원 초대 및 관리"
        isShowing={isShowing}
        handleClose={handleClose}
      >
        <ModalWrapper>
          <ContentWrapper>
            <SearchWrapper>
              <SearchBar>
                <SearchInput
                  type="text"
                  placeholder="사용자를 검색해주세요."
                  value={query}
                  onChange={handleSearch}
                />
                <SearchIcon
                  src="/assets/icons/ic_search_light.svg"
                  alt="search"
                />
              </SearchBar>

              <SearchList>
                {filtered.map((user, idx) => (
                  <SearchItem key={idx} onClick={() => handleSelectUser(user)}>
                    <Profile
                      src={user.profileImageUrl || '/assets/user.png'}
                      alt="profile"
                    />
                    <div>{user.userName}</div>
                    <div>@{user.userIdentifyId}</div>
                  </SearchItem>
                ))}
              </SearchList>
            </SearchWrapper>
            <MemberWrapper>
              <SubTitle>초대한 팀원</SubTitle>
              <UserList>
                {/* 새로 선택한 유저 */}
                {selectedUsers.map((user) => (
                  <UserItem key={user.userId}>
                    <Profile
                      src={user.profileImageUrl || '/assets/user.png'}
                      alt="profile"
                    />
                    <UserInfo>
                      <div>{user.userName}</div>
                      <div>@{user.userIdentifyId}</div>
                    </UserInfo>
                    <IcIcon
                      src="/assets/icons/ic_delete.svg"
                      alt="delete button"
                      onClick={() => handleRemoveSelected(user.userId)}
                    />
                  </UserItem>
                ))}
              </UserList>

              <UserList>
                {/* 기존 초대한 사람들 (상태 표시만) */}
                {projectUsers.map((user, idx) => (
                  <UserItem key={idx}>
                    <Profile
                      src={user.profileImageUrl || '/assets/user.png'}
                      alt="profile"
                    />
                    <UserInfo>
                      <div>{user.userName}</div>
                      <div>@{user.userIdentifyId}</div>
                    </UserInfo>
                    <StatusText status={user.status}>
                      {user.status === 'ACCEPTED'
                        ? '초대 수락'
                        : user.status === 'PENDING'
                          ? '수락 대기'
                          : user.status === 'OWNER'
                            ? '소유자'
                            : '알수 없음'}
                    </StatusText>
                  </UserItem>
                ))}
              </UserList>
            </MemberWrapper>
          </ContentWrapper>

          <DoneButton onClick={handleDone}>완료</DoneButton>
        </ModalWrapper>
      </SmallModal>
    )
  );
};

export default TeamInviteModal;

const ModalWrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;

  padding-right: 4rem;
`;

const ContentWrapper = styled.div`
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: space-between;

  width: 100%;
`;

const SearchWrapper = styled.div`
  width: 100%;

  display: flex;
  flex-direction: column;
  align-items: center;
`;

const SearchBar = styled.div`
  display: flex;
  align-items: center;
`;

const SearchList = styled.div`
  width: 20rem;
  max-height: 30rem;

  padding-left: 2rem;
  margin-top: 1rem;

  overflow-y: auto;
`;

const SearchItem = styled.div`
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 1rem;

  padding: 0.5rem;
  padding-left: 0;

  div {
    ${({ theme }) => theme.fonts.Title7};
    color: ${({ theme }) => theme.colors.Black1};
  }

  div:last-of-type {
    ${({ theme }) => theme.fonts.EnBody3};
    color: ${({ theme }) => theme.colors.Gray3};
  }

  cursor: pointer;
`;

const SearchInput = styled.input`
  width: 20rem;
  height: 2.5rem;
  padding: 0.5rem 1rem;

  ${({ theme }) => theme.fonts.Body3};
  color: ${({ theme }) => theme.colors.Black1};

  background-color: ${({ theme }) => theme.colors.LightGray3};
  border: 1px solid ${({ theme }) => theme.colors.InputStroke};
  border-radius: 1rem;
`;

const SearchIcon = styled.img`
  width: 2rem;
  margin-left: -3.5rem;

  cursor: pointer;
`;

const MemberWrapper = styled.div``;

const SubTitle = styled.div`
  margin-top: 0.5rem;

  ${({ theme }) => theme.fonts.Head3};
  color: ${({ theme }) => theme.colors.Black1};
`;

const UserList = styled.div`
  width: 100%;
  height: 12rem;
  margin-top: 1rem;

  overflow-y: auto;
  overflow-x: hidden;

  border-bottom: 1px solid ${({ theme }) => theme.colors.LightGray2};

  &:last-of-type {
    height: 100%;
    border-bottom: none;
  }

  & {
    scrollbar-width: thin;
    scrollbar-color: ${({ theme }) =>
      `${theme.colors.BorderDefault} transparent`};
  }
`;

const UserItem = styled.div`
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;

  width: 20rem;
  padding: 1rem;
  padding-left: 0;
`;

const Profile = styled.img`
  width: 3.5rem;
  height: 3.5rem;
  border-radius: 50%;
`;

const UserInfo = styled.div`
  display: flex;
  flex-direction: column;

  width: 10rem;
  padding-left: 1rem;

  div:first-of-type {
    ${({ theme }) => theme.fonts.Title6};
    color: ${({ theme }) => theme.colors.Black1};
  }

  div:last-of-type {
    ${({ theme }) => theme.fonts.EnBody2};
    color: ${({ theme }) => theme.colors.Gray3};
  }
`;

const StatusText = styled.div<{ status: string }>`
  ${({ theme }) => theme.fonts.Title7};
  color: ${({ theme, status }) =>
    status === 'ACCEPTED'
      ? theme.colors.Green1
      : status === 'PENDING'
        ? theme.colors.Blue1
        : status === 'OWNER'
          ? theme.colors.Black1
          : theme.colors.Red2};
`;

const DoneButton = styled.button`
  width: 15rem;
  padding: 1rem;
  margin-top: 2rem;

  ${({ theme }) => theme.fonts.Title5};
  color: ${({ theme }) => theme.colors.White};

  background-color: ${({ theme }) => theme.colors.Gray0};
  border-radius: 1rem;
`;

const IcIcon = styled.img`
  cursor: pointer;
`;
