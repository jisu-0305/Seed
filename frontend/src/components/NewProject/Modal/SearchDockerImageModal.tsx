import styled from '@emotion/styled';
import { ChangeEvent, useEffect, useState } from 'react';

import { getProjectApplications } from '@/apis/gitlab';
import SmallModal from '@/components/Common/Modal/SmallModal';
import { ApplicationWithDefaults } from '@/types/project';

interface Props {
  isShowing: boolean;
  handleClose: () => void;
  onSelect: (img: ApplicationWithDefaults) => void;
}

// interface Image {
//   repo_name: string;
//   short_description: string;
// }

type Image = ApplicationWithDefaults;

const SearchDockerImageModal = ({
  isShowing,
  handleClose,
  onSelect,
}: Props) => {
  const [query, setQuery] = useState('');
  const [imageList, setImageList] = useState<Image[]>([]);
  const [hasSearched, setHasSearched] = useState(false);

  const fetchDockerImage = async () => {
    // const { data } = await getDockerImage(query);
    // setImageList(data.image);

    setHasSearched(true);
    const { data } = await getProjectApplications(query);
    console.log(data);

    setImageList(data);
  };

  useEffect(() => {
    if (!isShowing) return;
    fetchDockerImage();
  }, [isShowing]);

  const handleSearch = (e: ChangeEvent<HTMLInputElement>) => {
    if (e.target.value.length === 0) {
      setHasSearched(false);
      setImageList([]);
    }
    setQuery(e.target.value);
  };

  const closeWithReset = () => {
    setQuery('');
    setImageList([]);
    setHasSearched(false);
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
            {/* {imageList.map((img) => (
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
            ))} */}
            {hasSearched && query && imageList.length === 0 && (
              <NoResults>검색 결과가 없습니다.</NoResults>
            )}
            {imageList.map((img) => (
              <ResultItem
                key={img.imageName}
                onClick={() => {
                  onSelect(img);
                  closeWithReset();
                }}
              >
                <ImageName>
                  {img.imageName}
                  <OfficialIcon
                    src="/assets/icons/ic_official.svg"
                    alt="official"
                  />
                </ImageName>
                <ImageDesc>{img.description}</ImageDesc>
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
  color: ${({ theme }) => theme.colors.Black};

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
  color: ${({ theme }) => theme.colors.Black};
`;

const ImageDesc = styled.p`
  max-width: 29rem;
  ${({ theme }) => theme.fonts.Body6};
  color: ${({ theme }) => theme.colors.Black};

  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
`;

const OfficialIcon = styled.img``;

const NoResults = styled.div`
  ${({ theme }) => theme.fonts.Body2};
  color: ${({ theme }) => theme.colors.Black};
  padding: 2rem 0;
  text-align: center;
`;
