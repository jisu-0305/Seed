import styled from '@emotion/styled';

interface AvatarListProps {
  users: { id: number; avatarUrl: string; name: string }[];
  maxVisible?: number; // 한 줄에 보여줄 최대 아바타 개수
}

export function AvatarList({ users, maxVisible = 2 }: AvatarListProps) {
  const visible = users.slice(0, maxVisible);
  const extraCount = users.length - visible.length;

  return (
    <List>
      {visible.map((u) => (
        <AvatarWrapper key={u.id}>
          <Avatar src={u.avatarUrl} alt={u.name} title={u.name} />
        </AvatarWrapper>
      ))}
      {extraCount > 0 && <MoreCount>+{extraCount}</MoreCount>}
    </List>
  );
}

const List = styled.div`
  display: flex;
  align-items: center;
  /* 겹쳐 보이도록 좌측으로 마이너스 마진 */
  & > * + * {
    margin-left: -0.75rem;
  }
`;

const AvatarWrapper = styled.div`
  width: 2.5rem;
  height: 2.5rem;
  border: 0.2rem solid #fff;
  border-radius: 50%;
  background-color: #eee;
  overflow: hidden;
`;

const Avatar = styled.img`
  width: 100%;
  height: 100%;
  object-fit: cover;
`;

const MoreCount = styled.div`
  width: 2.5rem;
  height: 2.5rem;
  border: 0.2rem solid #fff;
  border-radius: 50%;
  background-color: ${({ theme }) => theme.colors.Blue4};
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  color: ${({ theme }) => theme.colors.Black1};
`;
