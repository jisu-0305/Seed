import styled from '@emotion/styled';
import React, { useEffect, useRef, useState } from 'react';

import { useThemeStore } from '@/stores/themeStore';

interface CustomDropdownProps {
  options: string[];
  value?: string;
  onChange?: (selected: string) => void;
  width?: string;
  dropdownScrollRef?: React.Ref<HTMLUListElement>;
  loaderRef?: React.Ref<HTMLLIElement>;
}

export default function CustomDropdown({
  options,
  value,
  onChange,
  width = '15rem',
  dropdownScrollRef,
  loaderRef,
}: CustomDropdownProps) {
  const { mode } = useThemeStore();
  const [open, setOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const handleClickOutside = (e: MouseEvent) => {
    if (
      dropdownRef.current &&
      !dropdownRef.current.contains(e.target as Node)
    ) {
      setOpen(false);
    }
  };

  useEffect(() => {
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSelect = (val: string) => {
    onChange?.(val);
    setOpen(false);
  };

  return (
    <Wrapper ref={dropdownRef}>
      <DropdownButton onClick={() => setOpen(!open)} $width={width}>
        {value}
        <ArrowIcon
          src={`/assets/icons/ic_arrow_down_${mode}.svg`}
          alt="arrow"
        />
      </DropdownButton>
      {open && (
        <DropdownList $width={width} ref={dropdownScrollRef}>
          {options.map((opt, idx) => (
            <DropdownItem
              // eslint-disable-next-line react/no-array-index-key
              key={`${opt}-${idx}`}
              onClick={() => handleSelect(opt)}
            >
              {opt}
            </DropdownItem>
          ))}
          {loaderRef && <DropdownItem ref={loaderRef} />}
        </DropdownList>
      )}
    </Wrapper>
  );
}

const Wrapper = styled.div`
  position: relative;
  width: 100%;
`;

const DropdownButton = styled.button<{ $width: string }>`
  width: ${({ $width }) => $width};
  padding: 1rem;
  padding-right: 4rem;

  ${({ theme }) => theme.fonts.Body1};
  color: ${({ theme }) => theme.colors.Text};
  text-align: start;

  background-color: ${({ theme }) => theme.colors.InputBackground};
  border: 1px solid ${({ theme }) => theme.colors.InputStroke};
  border-radius: 1rem;

  appearance: none;

  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  cursor: pointer;
`;

const ArrowIcon = styled.img`
  position: absolute;
  right: 1rem;
  top: 55%;
  transform: translateY(-50%);

  pointer-events: none;
`;

const DropdownList = styled.ul<{ $width: string }>`
  position: absolute;

  width: ${({ $width }) => $width};
  max-height: calc(2.4rem * 7);
  margin-top: 0.5rem;
  overflow-y: auto;

  border: 1px solid ${({ theme }) => theme.colors.Gray3};
  border-radius: 1rem;

  background: ${({ theme }) => theme.colors.Background};
  z-index: 1000;
`;

const DropdownItem = styled.li`
  padding: 0.8rem 1rem;

  color: ${({ theme }) => theme.colors.Text};
  ${({ theme }) => theme.fonts.Body3};

  cursor: pointer;

  &:hover {
    background-color: ${({ theme }) => theme.colors.InputBackground};
  }
`;
