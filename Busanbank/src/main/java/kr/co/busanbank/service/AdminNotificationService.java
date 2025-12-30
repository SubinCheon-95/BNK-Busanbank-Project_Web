package kr.co.busanbank.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import kr.co.busanbank.dto.NotificationDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.mapper.AdminNotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
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

    public void sendPush(NotificationDTO notificationDTO) {

        Message message = Message.builder()
                .setTopic("all")
                .putData("type", "ADMIN_NOTIFICATION")
                .putData("title", notificationDTO.getTitle())
                .putData("content", notificationDTO.getContent())

                .putData("route", notificationDTO.getRoute() != null ? notificationDTO.getRoute() : "")  //추가사항
                .build();

        firebaseMessaging.sendAsync(message)
                .addListener(() -> {
                    log.info("FCM 전송 요청 성공");
                }, Runnable::run);
    }

    public void sendBtcPush(int userNo, boolean success, long yesterday, long today) {
        log.info("예측 결과 후 푸시 알림");

        Message message = Message.builder()
                .setTopic("user_" + userNo)
                .putData("type", "ADMIN_NOTIFICATION")
                .putData("title", "어제 예측한 결과가 나왔어요")
                .putData("content", "지금 결과를 확인해 보세요")

                .putData("route", success ? "/success" : "/fail")
                .putData("yesterday", String.valueOf(yesterday))
                .putData("today", String.valueOf(today))
                .build();


        firebaseMessaging.sendAsync(message)
                .addListener(() -> {
                    log.info("FCM 전송 요청 성공");
                }, Runnable::run);
    }

    public void insertBtcPush(int userNo, boolean success, long yesterday, long today) {
        sendBtcPush(userNo, success, yesterday, today);
    }

    public void insertPush(NotificationDTO notificationDTO) {
        sendPush(notificationDTO);
        adminNotificationMapper.insertPush(notificationDTO);
    }

    public void insertAuto(NotificationDTO dto) {
        adminNotificationMapper.insertPush(dto);
    }

    @Scheduled(cron = "0 * * * * ?") // 1분 간격
    public void sendAutoPush() {

        List<NotificationDTO> list = adminNotificationMapper.findAutoList();

        for (NotificationDTO dto : list) {
            log.info("check id={}, cron='{}'", dto.getId(), dto.getCronExpr());
            if (isTimeToSend(dto.getCronExpr())) {
                sendPush(dto);
                adminNotificationMapper.markSent(dto.getId());
                log.info("SENT id={}", dto.getId());
            }
        }
    }

    public boolean isTimeToSend(String cronExpr) {

        if (cronExpr == null || cronExpr.isBlank()) return false;

        try {
            CronExpression cron = CronExpression.parse(cronExpr);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime next = cron.next(now.minusSeconds(1));

            return next != null && next.isBefore(now.plusSeconds(1));
        } catch (Exception e) {
            log.error("잘못된 cron 표현식: {}", cronExpr);
            return false;
        }
    }

    public void singleDelete(int id) {adminNotificationMapper.singleDelete(id);}
    public void delete(List<Long> idList) {adminNotificationMapper.delete(idList);}
}