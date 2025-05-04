import { PROJECT_STEP } from '@/constants/project';

// 현재 단계의 URL 알아내는 함수
export const getUrlFromId = (id: number): string => {
  const currentStep = PROJECT_STEP.find((step) => step.id === id);
  return currentStep ? currentStep.url : 'gitlab';
};

// 현재 path로 단계 알아내는 함수
export const getIdFromUrl = (url: string): number => {
  const currentStep = PROJECT_STEP.find((step) => step.url === url);
  return currentStep ? currentStep.id : 1;
};

// 현재 단계로 페이지 헤더 알아내는 함수
export const getLabelfromId = (id: number): string => {
  const currentStep = PROJECT_STEP.find((step) => step.id === id);
  return currentStep ? currentStep.label : '기본 정보';
};
