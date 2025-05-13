import styled from '@emotion/styled';
import { ChangeEvent, useState } from 'react';

import { getDockerImage } from '@/apis/gitlab';
import SmallModal from '@/components/Common/Modal/SmallModal';

interface Props {
  isShowing: boolean;
  handleClose: () => void;
  onSelect: (name: string) => void;
}

interface Image {
  repo_name: string;
  short_description: string;
}

const SearchDockerImageModal = ({
  isShowing,
  handleClose,
  onSelect,
}: Props) => {
  const [query, setQuery] = useState('');
  const [imageList, setImageList] = useState<Image[]>([]);

  const fetchDockerImage = async () => {
    const { data } = await getDockerImage(query);
    setImageList(data.image);
  };

  const handleSearch = (e: ChangeEvent<HTMLInputElement>) => {
    setQuery(e.target.value);
  };

  const closeWithReset = () => {
    setQuery('');
    setImageList([]);
    handleClose();
  };

  return (
    isShowing && (
      <SmallModal
        title="Docker Image 찾기"
        isShowing={isShowing}
        handleClose={closeWithReset}
      >
        <StModalWrapper>
          <SearchWrapper>
            <SearchInput
              type="text"
              placeholder="어플리케이션을 검색해주세요."
              value={query}
              onChange={handleSearch}
              onKeyDown={(e) => {
                if (e.key === 'Enter') fetchDockerImage();
              }}
            />
            <SearchIcon
              onClick={fetchDockerImage}
              src="/assets/icons/ic_search_light.svg"
              alt="search"
            />
          </SearchWrapper>

          <ResultList>
            {imageList.map((img) => (
              <ResultItem
                key={img.repo_name}
                onClick={() => {
                  onSelect(img.repo_name);
                  closeWithReset();
                }}
              >
                <ImageName>
                  {img.repo_name}
                  <OfficialIcon
                    src="/assets/icons/ic_official.svg"
                    alt="official"
                  />
                </ImageName>
                <ImageDesc>{img.short_description}</ImageDesc>
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
