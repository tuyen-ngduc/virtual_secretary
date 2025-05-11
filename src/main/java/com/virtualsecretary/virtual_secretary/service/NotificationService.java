package com.virtualsecretary.virtual_secretary.service;

import com.virtualsecretary.virtual_secretary.dto.request.NotificationRequest;
import com.virtualsecretary.virtual_secretary.dto.response.NotificationResponse;
import com.virtualsecretary.virtual_secretary.entity.Member;
import com.virtualsecretary.virtual_secretary.entity.Notification;
import com.virtualsecretary.virtual_secretary.entity.User;
import com.virtualsecretary.virtual_secretary.enums.ErrorCode;
import com.virtualsecretary.virtual_secretary.exception.IndicateException;
import com.virtualsecretary.virtual_secretary.repository.MemberRepository;
import com.virtualsecretary.virtual_secretary.repository.NotificationRepository;
import com.virtualsecretary.virtual_secretary.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class NotificationService {

    NotificationRepository notificationRepository;
    UserRepository userRepository;

    public void saveNotification(NotificationRequest dto) {
        User sender = userRepository.findByEmployeeCode(dto.getSender())
                .orElseThrow(() -> new IndicateException(ErrorCode.SENDER_NOT_EXISTED));

        Notification notification = new Notification();
        notification.setNotificationId(dto.getNotificationId());
        notification.setSender(sender);
        notification.setContent(dto.getContent());
        notification.setTimestamp(dto.getTimestamp());
        notification.setRead(dto.isRead());
        notification.setReceive(dto.getReceive());

        notificationRepository.save(notification);
    }



    public List<NotificationResponse> getAllNotificationsByEmployeeCode(String employeeCode) {
        List<Notification> notifications = notificationRepository.findByReceive(employeeCode);

        return notifications.stream().map(n -> {
            NotificationResponse res = new NotificationResponse();
            res.setNotificationId(n.getNotificationId());
            res.setSender(n.getSender().getEmployeeCode());
            res.setContent(n.getContent());
            res.setTimestamp(n.getTimestamp());
            res.setRead(n.isRead());
            res.setReceive(n.getReceive());
            return res;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void markAllNotificationsAsReadByReceiver(String employeeCode) {
        List<Notification> unreadNotifications = notificationRepository.findByReceiveAndIsReadFalse(employeeCode);

        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }

        notificationRepository.saveAll(unreadNotifications); // có thể bỏ nếu dùng @Transactional
    }


}

