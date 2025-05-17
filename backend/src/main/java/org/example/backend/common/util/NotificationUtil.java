package org.example.backend.common.util;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.domain.fcm.dto.NotificationMessage;
import org.example.backend.domain.fcm.entity.FcmToken;
import org.example.backend.domain.fcm.repository.FcmTokenRepository;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationUtil {

    private final FcmTokenRepository fcmTokenRepository;
    private final FirebaseMessaging firebaseMessaging;

    public void sendToUsers(List<Long> userIdList, NotificationMessage message) {
        List<FcmToken> tokenEntities = fcmTokenRepository.findByUserIdIn(userIdList);
        Map<String, Long> tokenToUserMap = tokenEntities.stream()
                .collect(Collectors.toMap(FcmToken::getToken, FcmToken::getUserId, (a, b) -> a));

        for (Map.Entry<String, Long> entry : tokenToUserMap.entrySet()) {
            String token = entry.getKey();

            Message fcmMessage = Message.builder()
                    .putData("type",  message.getNotificationType().name())
                    .putData("title", message.getNotificationTitle())
                    .putData("body", message.getNotificationContent())
                    .setToken(token)
                    .build();

            try {
                firebaseMessaging.send(fcmMessage);
            } catch (FirebaseMessagingException e) {
                throw new BusinessException(ErrorCode.FCM_SEND_FAILED);
            }
        }
    }

}