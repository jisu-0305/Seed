// src/components/LoginModal.tsx

'use client';

import styled from '@emotion/styled';
import React, { useEffect, useState } from 'react';

import { fetchMe } from '@/apis/user';
import { MeResponse } from '@/types/user';

interface Props {
  onClose: () => void;
}

const LoginModal: React.FC<Props> = ({ onClose }) => {
  const [me, setMe] = useState<MeResponse['data'] | null>(null);

  useEffect(() => {
    fetchMe()
      .then((data) => {
        setMe(data.data);
      })
      .catch((err) => {
        console.error('유저 정보 조회 실패', err);
      });
  }, []);

  return (
    <>
      <Backdrop onClick={onClose} />
      <Modal>
        <CloseBtn onClick={onClose}>&times;</CloseBtn>

        {me ? (
          <>
            <ProfileSection>
              <Avatar
                src={me.avatarUrl || '/assets/images/avatar_placeholder.png'}
                alt="avatar"
              />
              <UserName>{me.name}</UserName>
              <EditIcon>✏️</EditIcon>
            </ProfileSection>

            <List>
              <Item>
                <GitlabIcon src="/assets/icons/ic_gitlab.svg" alt="GitLab" />@
                {me.username}
              </Item>
              <Item
                onClick={() => {
                  /* 로그아웃 */
                }}
              >
                <Icon as="span">⤴︎</Icon>
                로그아웃
              </Item>
            </List>
          </>
        ) : (
          <p>유저 정보를 불러올 수 없습니다.</p>
        )}
      </Modal>
    </>
  );
};

export default LoginModal;

// styles (이전과 동일)
const Backdrop = styled.div`
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 10;
`;
const Modal = styled.div`
  position: absolute;
  top: 4rem;
  right: 2rem;
  width: 18rem;
  background: #fff;
  border-radius: 0.5rem;
  padding: 1rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  z-index: 11;
`;
const CloseBtn = styled.button`
  position: absolute;
  top: 0.5rem;
  right: 0.75rem;
  background: transparent;
  border: none;
  font-size: 1.25rem;
  cursor: pointer;
`;
const ProfileSection = styled.div`
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
`;
const Avatar = styled.img`
  width: 3rem;
  height: 3rem;
  border-radius: 50%;
  background: #eef2f7;
  margin-right: 0.75rem;
`;
const UserName = styled.span`
  flex: 1;
  font-weight: 600;
  font-size: 1.125rem;
`;
const EditIcon = styled.span`
  cursor: pointer;
`;

const List = styled.ul`
  list-style: none;
  padding: 0;
  margin: 0;
`;
const Item = styled.li`
  display: flex;
  align-items: center;
  padding: 0.75rem 0;
  cursor: pointer;
  border-top: 1px solid #f0f0f0;
  &:first-of-type {
    border-top: none;
  }
`;
const Icon = styled.img`
  width: 1.25rem;
  height: 1.25rem;
  margin-right: 0.5rem;
`;
const GitlabIcon = styled.img`
  width: 3rem;
  margin-right: 1rem;
`;
