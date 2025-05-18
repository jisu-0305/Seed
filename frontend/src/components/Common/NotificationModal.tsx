'use client';

import styled from '@emotion/styled';
import React from 'react';

import { useNotifications } from '@/hooks/Common/useNotifications';
import { useThemeStore } from '@/stores/themeStore';

const NotificationModal: React.FC<{
  onClose: () => void;
}> = ({ onClose }) => {
  const { mode } = useThemeStore();

  const { notifications, isLoading, error, markRead, accept } =
    useNotifications();

  if (mode === null) return null;

  return (
    <>
      <Backdrop onClick={onClose} />
      <Modal>
        {isLoading && <p>로딩 중…</p>}
        {error && <p>불러오기 실패</p>}

        {!isLoading && notifications?.length === 0 && (
          <Header>
            <Heading>
              <Icon src="/assets/icons/ic_speaker.svg" alt="notification" />
              알림
            </Heading>
            <Message>새 알림이 없습니다.</Message>
          </Header>
        )}

        <List>
          {notifications?.map((n) => (
            <Item key={n.id}>
              <Info>
                <Icon src="/assets/icons/ic_speaker.svg" alt="notification" />
                <Text>
                  <Title>{n.notificationTitle}</Title>
                  <Message>{n.notificationContent}</Message>
                </Text>
              </Info>
              <AcceptButton
                onClick={() => {
                  if (n.notificationType === 'INVITATION_CREATED_TYPE') {
                    accept.mutate({
                      notificationId: n.id,
                      invitationId: n.invitationId,
                    });
                  } else {
                    markRead.mutate(n.id);
                  }
                }}
              >
                {n.notificationType === 'INVITATION_CREATED_TYPE'
                  ? '수락하기'
                  : '읽음처리'}
              </AcceptButton>
            </Item>
          ))}
        </List>
      </Modal>
    </>
  );
};

export default NotificationModal;

const Backdrop = styled.div`
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0);
  z-index: 10;
`;

const Modal = styled.div`
  position: absolute;
  top: 7.1rem;
  right: 0.5rem;
  width: 26rem;
  max-height: 70vh;
  overflow-y: auto;
  background: ${({ theme }) => theme.colors.ModalBg};
  border-radius: 1rem;
  padding: 1.5rem 2rem;
  box-shadow: 1px 2px 4px rgba(0, 0, 0, 0.1);
  z-index: 11;
`;

const Header = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  width: 100%;
  gap: 1rem;
  margin-bottom: 1rem;
`;

const Heading = styled.div`
  display: flex;
  gap: 1rem;
  ${({ theme }) => theme.fonts.Title5};
`;

const List = styled.ul`
  list-style: none;
  margin: 0;
  padding: 0;
`;

const Item = styled.li`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
  gap: 2rem;
  border-bottom: 1px solid ${({ theme }) => theme.colors.BorderDefault};

  &:last-child {
    border-bottom: none;
  }
`;

const Info = styled.div`
  display: flex;
  align-items: flex-start;
  gap: 1rem;
`;

const Text = styled.div`
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  width: 14rem;
`;

const Title = styled.div`
  ${({ theme }) => theme.fonts.Body3};
`;

const Message = styled.div`
  ${({ theme }) => theme.fonts.Body5};
  color: ${({ theme }) => theme.colors.DetailText};
`;

const AcceptButton = styled.button`
  width: 11rem;
  padding: 0.5rem 1rem;
  ${({ theme }) => theme.fonts.Body5};
  background: ${({ theme }) => theme.colors.MenuBg};
  color: ${({ theme }) => theme.colors.MenuText};
  border: none;
  border-radius: 5rem;
  cursor: pointer;
`;

const Icon = styled.img`
  width: 1.75rem;
`;
