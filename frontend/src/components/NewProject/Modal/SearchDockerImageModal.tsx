import styled from '@emotion/styled';
import { ChangeEvent, useState } from 'react';

import SmallModal from '@/components/Common/Modal/SmallModal';

interface Props {
  isShowing: boolean;
  handleClose: () => void;
  onSelect: (app: { name: string }) => void;
}

const dummyImages = [
  {
    name: 'redis',
    desc: 'Redis is the world’s fastest data platform for caching, vector search, and NoSQL databases.',
  },
  {
    name: 'redis-temp',
    desc: 'redis-stack-server installs a Redis server with additional database capabilities',
  },
  {
    name: 'redis/redis-stack',
    desc: 'redis-stack installs a Redis server with additional database capabilities and the RedisInsight.',
  },
];

const SearchDockerImageModal = ({
  isShowing,
  handleClose,
  onSelect,
}: Props) => {
  const [query, setQuery] = useState('');

  const filteredImages = dummyImages.filter((img) => img.name.includes(query));

  const handleSearch = (e: ChangeEvent<HTMLInputElement>) => {
    setQuery(e.target.value);
  };

  return (
    isShowing && (
      <SmallModal
        title="Docker Image 찾기"
        isShowing={isShowing}
        handleClose={handleClose}
      >
        <StModalWrapper>
          <SearchWrapper>
            <SearchInput
              type="text"
              placeholder="어플리케이션을 검색해주세요."
              value={query}
              onChange={handleSearch}
            />
            <SearchIcon src="/assets/icons/ic_search_light.svg" alt="search" />
          </SearchWrapper>

          <ResultList>
            {filteredImages.map((img) => (
              <ResultItem
                key={img.name}
                onClick={() => {
                  onSelect(img);
                  handleClose();
                }}
              >
                <ImageName>
                  {img.name}
                  <OfficialIcon
                    src="/assets/icons/ic_official.svg"
                    alt="official"
                  />
                </ImageName>
                <ImageDesc>{img.desc}</ImageDesc>
              </ResultItem>
            ))}
          </ResultList>
        </StModalWrapper>
      </SmallModal>
    )
  );
};

export default SearchDockerImageModal;

const StModalWrapper = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  padding: 0 3rem;
`;
const SearchWrapper = styled.div`
  display: flex;
  align-items: center;

  width: 100%;
  margin-bottom: 2rem;
`;

const SearchInput = styled.input`
  flex: 1;
  padding: 1rem;

  ${({ theme }) => theme.fonts.Body1};
  color: ${({ theme }) => theme.colors.Text};

  background-color: ${({ theme }) => theme.colors.LightGray3};
  border: 1px solid ${({ theme }) => theme.colors.InputStroke};
  border-radius: 1rem;
`;

const SearchIcon = styled.img`
  margin-left: -4rem;

  cursor: pointer;
`;

const ResultList = styled.div`
  display: flex;
  flex-direction: column;
  gap: 1rem;

  width: 95%;
  max-height: 16rem;
  margin-bottom: 2rem;

  overflow-y: auto;
`;

const ResultItem = styled.div`
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;

  padding: 1rem 2rem;
  padding-right: 1rem;

  border: 1px solid ${({ theme }) => theme.colors.Black};
  border-radius: 0.8rem;

  cursor: pointer;
`;

const ImageName = styled.div`
  display: flex;
  align-items: center;
  gap: 0.5rem;

  ${({ theme }) => theme.fonts.Title5};
`;

const ImageDesc = styled.p`
  max-width: 29rem;
  ${({ theme }) => theme.fonts.Body6};

  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
`;

const OfficialIcon = styled.img``;
