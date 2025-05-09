'use client';

import styled from '@emotion/styled';
import React, { useEffect, useState } from 'react';

import {
  acceptInvitation,
  fetchNotifications,
  markNotificationRead,
} from '@/apis/user';
import { useThemeStore } from '@/stores/themeStore';
import { useUserStore } from '@/stores/userStore';
import { NotificationItem } from '@/types/notification';

const NotificationModal: React.FC<{ onClose: () => void }> = ({ onClose }) => {
  const user = useUserStore((s) => s.user);
  const { mode } = useThemeStore();

  const [notifications, setNotifications] = useState<NotificationItem[] | null>(
    null,
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 1) 유저 정보 로딩 완료되면 알림 API 호출
  useEffect(() => {
    if (!user) return;
    setLoading(true);
    fetchNotifications()
      .then((data) => setNotifications(data))
      .catch(() => setError('알림을 불러올 수 없습니다.'))
      .finally(() => setLoading(false));
  }, [user]);

  if (mode === null) return null;

  const handleAccept = async (notifId: number) => {
    try {
      await acceptInvitation(notifId);
      handleRead(notifId);
    } catch (err) {
      console.error('초대 수락 실패', err);
    }
  };

  const handleRead = async (notifId: number) => {
    try {
      await markNotificationRead(notifId);
      setNotifications((prev) =>
        prev ? prev.filter((n) => n.id !== notifId) : prev,
      );
    } catch (err) {
      console.error('알림 읽음 처리 실패', err);
    }
  };

  return (
    <>
      <Backdrop onClick={onClose} />
      <Modal>
        {user && (
          <>
            {loading && <p>알림 로딩 중…</p>}
            {error && <p>{error}</p>}

            {!loading && notifications?.length === 0 && (
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
                    <Icon
                      src="/assets/icons/ic_speaker.svg"
                      alt="notification"
                    />
                    <Text>
                      <Title>{n.notificationTitle}</Title>
                      <Message>{n.notificationContent}</Message>
                    </Text>
                  </Info>
                  {n.notificationType === 'INVITATION_CREATED_TYPE' ? (
                    <AcceptButton onClick={() => handleAccept(n.id)}>
                      수락하기
                    </AcceptButton>
                  ) : (
                    <AcceptButton onClick={() => handleRead(n.id)}>
                      읽음처리
                    </AcceptButton>
                  )}
                </Item>
              ))}
            </List>
          </>
        )}
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
`;

const Title = styled.div`
  ${({ theme }) => theme.fonts.Body3};
`;

const Message = styled.div`
  ${({ theme }) => theme.fonts.Body5};
  color: ${({ theme }) => theme.colors.DetailText};
`;

const AcceptButton = styled.button`
  width: 10rem;
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
