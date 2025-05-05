// 파일명 get으로 시작

// HH:MM:SS 형식으로 시간 변환
export const formatHHMMSS = (isoString: string): string => {
  const date = new Date(isoString);
  const hours = date.getHours();
  const minutes = date.getMinutes();
  const seconds = date.getSeconds();

  const formattedHours = hours.toString().padStart(2, '0');
  const formattedMinutes = minutes.toString().padStart(2, '0');
  const formattedSeconds = seconds.toString().padStart(2, '0');

  return `${formattedHours}:${formattedMinutes}:${formattedSeconds}`;
};

/**
 * ISO 문자열을 "MM.DD HH:MM:SS" 포맷으로 반환합니다.
 * ex) "2025-05-01T14:32:10Z" → "05.01 14:32:10"
 */
export const formatDateTime = (isoString: string): string => {
  const date = new Date(isoString);
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date.getDate().toString().padStart(2, '0');

  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  const seconds = date.getSeconds().toString().padStart(2, '0');

  return `${month}.${day} ${hours}:${minutes}:${seconds}`;
};
