export interface NotificationItem {
  id: number;
  notificationTitle: string;
  notificationContent: string;
  createdAt: string;
  notificationType: string;
  read: boolean;
}

export interface NotificationResponse {
  success: boolean;
  message: string;
  data: NotificationItem[];
}
