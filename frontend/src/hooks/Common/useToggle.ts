// 토글 버튼 커스텀 훅

import { useState } from 'react';

type UseToggleReturnType = [boolean, () => void];

const useToggle = (initialValue: boolean): UseToggleReturnType => {
  const [toggle, setToggle] = useState(initialValue);

  const handleToggle = () => {
    setToggle((prevState) => !prevState);
  };

  return [toggle, handleToggle];
};

export default useToggle;
