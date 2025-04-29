// 모달창 커스텀 훅

import { useState } from 'react';

// 타입 명시할 것
export type UseModalReturnType = {
  isShowing: boolean;
  toggle: () => void;
  setShowing: (showing: boolean) => void;
};

const useModal = (): UseModalReturnType => {
  const [isShowing, setIsShowing] = useState(false);

  const toggle = () => setIsShowing((prev) => !prev);

  const setShowing = (showing: boolean) => setIsShowing(showing);

  return {
    isShowing,
    toggle,
    setShowing,
  };
};

export default useModal;
