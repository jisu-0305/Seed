/* eslint-disable react/no-array-index-key */
import styled from '@emotion/styled';
import { ChangeEvent, useState } from 'react';

import SmallModal from '@/components/Common/Modal/SmallModal';

interface InviteModalProps {
  isShowing: boolean;
  handleClose: () => void;
}

const dummyUsers = [
  { name: '김승엽', id: '@syt05342' },
  { name: '김예슬', id: '@0ysgong' },
  { name: '김유진', id: '@galilee155' },
  { name: '김재훈', id: '@potential1205' },
  { name: '김지수', id: '@comjisu0311' },
  { name: '김효승', id: '@julia_2000' },
];

const invited = [
  { name: '김예슬', id: '@0ysgong', status: 'accepted' },
  { name: '김지수', id: '@comjisu0311', status: 'accepted' },
  { name: '이효승', id: '@julia_2000', status: 'pending' },
  { name: '배석진', id: '@bsj1044', status: 'rejected' },
];

const TeamInviteModal = ({ isShowing, handleClose }: InviteModalProps) => {
  const [query, setQuery] = useState('');
  const [filtered, setFiltered] = useState(dummyUsers);
  const [selectedUsers, setSelectedUsers] = useState<typeof dummyUsers>([]);

  const handleSelectUser = (user: (typeof dummyUsers)[0]) => {
    if (selectedUsers.some((u) => u.id === user.id)) return;
    setSelectedUsers((prev) => [...prev, user]);
  };

  const handleRemoveSelected = (id: string) => {
    setSelectedUsers((prev) => prev.filter((user) => user.id !== id));
  };

  const handleSearch = (e: ChangeEvent<HTMLInputElement>) => {
    const keyword = e.target.value;
    setQuery(keyword);
    setFiltered(dummyUsers.filter((user) => user.name.includes(keyword)));
  };

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
                    <Profile src="/assets/icons/ic_profile.svg" alt="profile" />
                    <div>{user.name}</div>
                    <div>{user.id}</div>
                  </SearchItem>
                ))}
              </SearchList>
            </SearchWrapper>
            <MemberWrapper>
              <SubTitle>초대한 팀원</SubTitle>
              <UserList>
                {/* 새로 선택한 유저 */}
                {selectedUsers.map((user) => (
                  <UserItem key={user.id}>
                    <Profile src="/assets/icons/ic_profile.svg" alt="profile" />
                    <UserInfo>
                      <div>{user.name}</div>
                      <div>{user.id}</div>
                    </UserInfo>
                    <IcIcon
                      src="/assets/icons/ic_delete.svg"
                      alt="delete button"
                      onClick={() => handleRemoveSelected(user.id)}
                    />
                  </UserItem>
                ))}
              </UserList>

              <UserList>
                {/* 기존 초대한 사람들 (상태 표시만) */}
                {invited.map((user, idx) => (
                  <UserItem key={idx}>
                    <Profile src="/assets/icons/ic_profile.svg" alt="profile" />
                    <UserInfo>
                      <div>{user.name}</div>
                      <div>{user.id}</div>
                    </UserInfo>
                    <StatusText status={user.status}>
                      {user.status === 'accepted'
                        ? '초대 수락'
                        : user.status === 'pending'
                          ? '수락 대기'
                          : '초대 거절'}
                    </StatusText>
                  </UserItem>
                ))}
              </UserList>
            </MemberWrapper>
          </ContentWrapper>

          <DoneButton onClick={handleClose}>완료</DoneButton>
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

  border-bottom: 1px solid ${({ theme }) => theme.colors.LightGray2};

  &:last-of-type {
    height: 100%;
    border-bottom: none;
  }
`;

const UserItem = styled.div`
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;

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
    status === 'accepted'
      ? theme.colors.Green1
      : status === 'pending'
        ? theme.colors.Blue1
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
