import { CARD_COLOR } from '@/constants/common';
import { ACTIVITY_COLOR } from '@/constants/dashboard';

export const getCardColor = (statusName: string) => {
  const category = CARD_COLOR.find((item) => item.name === statusName);
  return category ? category.color : '#F7F9FB';
};

export const getActivityColor = (type: string) => {
  const category = ACTIVITY_COLOR.find((item) => item.name === type);
  return category ? category.color : '#FFA100';
};
