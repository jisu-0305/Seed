import { useEffect, useRef } from 'react';

export const useVerticalDragScroll = <T extends HTMLElement>() => {
  const ref = useRef<T | null>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el) return undefined;

    let isDown = false;
    let startY = 0;
    let scrollTop = 0;

    const onMouseDown = (e: MouseEvent) => {
      isDown = true;
      startY = e.pageY - el.offsetTop;
      scrollTop = el.scrollTop;
      el.style.cursor = 'grabbing';
    };

    const onMouseLeave = () => {
      isDown = false;
      el.style.cursor = 'grab';
    };

    const onMouseUp = () => {
      isDown = false;
      el.style.cursor = 'grab';
    };

    const onMouseMove = (e: MouseEvent) => {
      if (!isDown) return;
      e.preventDefault();
      const y = e.pageY - el.offsetTop;
      const walkY = y - startY;
      el.scrollTop = scrollTop - walkY;
    };

    el.addEventListener('mousedown', onMouseDown);
    el.addEventListener('mouseleave', onMouseLeave);
    el.addEventListener('mouseup', onMouseUp);
    el.addEventListener('mousemove', onMouseMove);
    el.style.cursor = 'grab';

    return () => {
      el.removeEventListener('mousedown', onMouseDown);
      el.removeEventListener('mouseleave', onMouseLeave);
      el.removeEventListener('mouseup', onMouseUp);
      el.removeEventListener('mousemove', onMouseMove);
    };
  }, []);

  return ref;
};
