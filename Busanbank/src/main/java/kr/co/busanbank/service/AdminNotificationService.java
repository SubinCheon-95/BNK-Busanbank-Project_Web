package kr.co.busanbank.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import kr.co.busanbank.dto.NotificationDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.mapper.AdminNotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {
    private final AdminNotificationMapper adminNotificationMapper;
    private final FirebaseMessaging firebaseMessaging;

    public PageResponseDTO selectAll(PageRequestDTO pageRequestDTO) {
        List<NotificationDTO> dtoList = adminNotificationMapper.findAll(pageRequestDTO);
        int total = adminNotificationMapper.selectCount(pageRequestDTO);

        return PageResponseDTO.<NotificationDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public PageResponseDTO searchAll(PageRequestDTO pageRequestDTO) {
        List<NotificationDTO> dtoList = adminNotificationMapper.searchAll(pageRequestDTO);
        int total = adminNotificationMapper.searchCountTotal(pageRequestDTO);

        return PageResponseDTO.<NotificationDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public void insertPush(NotificationDTO notificationDTO) {

        Message message = Message.builder()
                .setTopic("all")
                .setNotification(
                        Notification.builder()
                                .setTitle(notificationDTO.getTitle())
                                .setBody(notificationDTO.getContent())
                                .build()
                )
                .putData("type", "ADMIN_NOTIFICATION")
                .putData("title", notificationDTO.getTitle())
                .putData("content", notificationDTO.getContent())
                .build();

        firebaseMessaging.sendAsync(message)
                .addListener(() -> {
                    System.out.println("FCM 전송 요청 성공");
                }, Runnable::run);

        adminNotificationMapper.insertPush(notificationDTO);
    }

    public void singleDelete(int id) {adminNotificationMapper.singleDelete(id);}
    public void delete(List<Long> idList) {adminNotificationMapper.delete(idList);}
}