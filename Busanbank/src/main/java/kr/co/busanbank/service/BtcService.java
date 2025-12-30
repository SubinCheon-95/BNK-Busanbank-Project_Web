package kr.co.busanbank.service;

import kr.co.busanbank.dto.BtcPredictDTO;
import kr.co.busanbank.dto.PriceHistoryDTO;
import kr.co.busanbank.dto.UserCouponDTO;
import kr.co.busanbank.exception.AlreadyParticipatedException;
import kr.co.busanbank.mapper.BtcMapper;
import kr.co.busanbank.mapper.PriceHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BtcService {
    private final BtcMapper btcMapper;
    private final PriceHistoryMapper priceHistoryMapper;
    private final UserCouponService userCouponService;
    private final AdminNotificationService adminNotificationService;

    public List<UserCouponDTO> couponSearch(int userId) {return btcMapper.findById(userId);}

    public void markUserParticipated(int userNo, int couponId) {
        btcMapper.markUserParticipated(userNo, couponId);
    }

    @Transactional
    public void insertPredict(int userNo, String prediction) {
        try {
            btcMapper.insertPredict(userNo, prediction);
        } catch (DuplicateKeyException e) {
            throw new AlreadyParticipatedException();
        }
    }

    //@Scheduled(cron = "0 10 0 * * *", zone = "Asia/Seoul") //서버용
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul") //테스트용
    @Transactional
    public void resultCheck() {
        log.info("BTC resultCheck 스케줄러 실행");

        List<PriceHistoryDTO> list = priceHistoryMapper.getBtcYesterdayAndToday("BTC");
        log.info("어제, 오늘 값 = {}", list);

        long yesterday = Math.round(list.get(0).getPrice());
        log.info("어제 = {}", yesterday);
        long today = Math.round(list.get(1).getPrice());
        log.info("오늘 = {}", today);

        String actual = today > yesterday ? "UP" : "DOWN";
        log.info("BTC 결과: {}", actual);

        List<BtcPredictDTO> predicts = btcMapper.findByYesterdayPredict();

        for (BtcPredictDTO predict : predicts) {
            int userNo = predict.getUserNo();
            boolean success = predict.getPredict().equals(actual);
            log.info("예측 성공 여부 = {}", success);

            btcMapper.updatePredictResult(predict.getPredictId(),
                    actual,
                    success ? "Y" : "N"
            );

            if (success) {
                List<UserCouponDTO> coupons = couponSearch(userNo);

                for (UserCouponDTO coupon : coupons) {
                    if (coupon.getCouponId() == 7 && coupon.getUserId() == null) {
                        userCouponService.registerCoupon(userNo, coupon.getCouponCode());
                        markUserParticipated(userNo, 7);
                        adminNotificationService.insertBtcPush(userNo, success, yesterday, today);
                        btcMapper.updateCouponId(userNo, 7);
                    }
                }
            } else {
                adminNotificationService.insertBtcPush(userNo, success, yesterday, today);
            }
        }
    }

    public void updateEvent(int couponId) {btcMapper.updateEvent(couponId);}
}
