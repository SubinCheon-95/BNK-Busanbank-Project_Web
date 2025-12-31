package kr.co.busanbank.service;

import kr.co.busanbank.mapper.ProductPushMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductPushService { //가입 완료 푸시 알림 - 작성자: 윤종인 2025.12.31
    private final AdminNotificationService adminNotificationService;
    private final ProductPushMapper  productPushMapper;

    public String findByUserName(int userNo) {
        return productPushMapper.findByUserName(userNo);
    }

    public void insertProductPush(int userNo, String userName, String productName) {
        adminNotificationService.insertProductPush(userNo, userName, productName);
    }
}
