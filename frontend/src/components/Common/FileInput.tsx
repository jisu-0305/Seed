import styled from '@emotion/styled';
import { ChangeEvent, useState } from 'react';

import { useProjectInfoStore } from '@/stores/projectStore';
import { useThemeStore } from '@/stores/themeStore';

interface FileInputProps {
  handleFileChange: (file: File) => void;
  accept?: string;
  placeholder: string;
  id?: string;
  inputType?: 'pem' | 'frontEnv' | 'backEnv';
}

export default function FileInput({
  handleFileChange,
  accept,
  placeholder,
  id,
  inputType,
}: FileInputProps) {
  const { mode } = useThemeStore();
  const { stepStatus } = useProjectInfoStore();
  const [file, setFile] = useState<File | null>(null);

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0];
    if (selectedFile) {
      setFile(selectedFile);
      handleFileChange(selectedFile);
    }
  };

  let fileName = '';
  if (inputType === 'pem') {
    fileName = stepStatus.server.pemName;
  } else if (inputType === 'frontEnv') {
    fileName = stepStatus.env.frontEnvName;
  } else if (inputType === 'backEnv') {
    fileName = stepStatus.env.backEnvName;
  }

  if (mode === null) return null;

  return (
    <PemInputWrapper>
      <PemInput
        type="text"
        readOnly
        value={file?.name || fileName || ''}
        placeholder={placeholder || ''}
      />
      <UploadLabel htmlFor={id || 'upload'}>
        <UploadIcon src={`/assets/icons/ic_upload_${mode}.svg`} alt="upload" />
      </UploadLabel>
      <HiddenInput
        type="file"
        accept={accept}
        id={id || 'upload'}
        onChange={handleChange}
      />
    </PemInputWrapper>
  );
}

const PemInputWrapper = styled.div`
  display: flex;
  align-items: center;

  max-width: 25rem;
  padding: 1rem 2rem;
  margin-top: 0.5rem;

  background-color: ${({ theme }) => theme.colors.InputBackground};
  border: 1px solid ${({ theme }) => theme.colors.InputStroke};
  border-radius: 1rem;
`;

const PemInput = styled.input`
  width: 100%;
  flex: 1;

  ${({ theme }) => theme.fonts.Body1};
  color: ${({ theme }) => theme.colors.Text};

  cursor: default;
`;

const UploadIcon = styled.img`
  cursor: pointer;
`;

const UploadLabel = styled.label`
  display: flex;
  align-items: center;
`;

const HiddenInput = styled.input`
  display: none;
`;
