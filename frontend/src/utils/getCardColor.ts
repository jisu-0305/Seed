import { CARD_COLOR } from '@/constants/common';

export const getCardColor = (statusName: string) => {
  const category = CARD_COLOR.find((item) => item.name === statusName);
  return category ? category.color : '#F7F9FB';
};
