/*
    날짜 : 2025/12/01
    이름 : 오서정
    내용 : 금 이벤트 서비스 작성
*/
package kr.co.busanbank.service;

import kr.co.busanbank.dto.GoldEventLogDTO;
import kr.co.busanbank.mapper.GoldEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class GoldEventService {
    private final GoldEventMapper goldEventMapper;

    private final MyService myService;
    private final UserCouponService userCouponService;

    public GoldEventLogDTO findTodayEvent(int userNo) {
        return goldEventMapper.findTodayEvent(userNo);
    }

    /** 오늘 이미 참여했는지 체크 */
    public boolean isAlreadyJoinedToday(int userNo) {
        return goldEventMapper.findTodayEvent(userNo) != null;
    }

    /** 오늘 금 시세 조회 */
    public double getTodayGoldPrice() {
        return goldEventMapper.findLatestPrice("XAU");
    }

    /** WAIT 상태 전부 조회 */
    public List<GoldEventLogDTO> findAllWait() {
        return goldEventMapper.findAllWait();
    }

    /** 이벤트 저장 */
    public void saveEvent(String userId, double todayPrice, double errorRate,
                          double min, double max) {

        int userNo = myService.findUserNo(userId); // ★ userId → userNo
        GoldEventLogDTO dto = new GoldEventLogDTO();
        dto.setUserNo(userNo);
        dto.setTodayPrice(todayPrice);
        dto.setErrorRate(errorRate);
        dto.setMinPrice(min);
        dto.setMaxPrice(max);
        dto.setResult("WAIT");

        goldEventMapper.insertEvent(dto);
    }

    /** 결과 업데이트 */
    public void updateEvent(GoldEventLogDTO dto) {
        goldEventMapper.updateEvent(dto);
    }

    /** 쿠폰 지급 */
    public void giveCoupon(int userNo, String couponCode) {


        // UserCouponService 활용 — 동일한 검증 로직, 사용한도, 중복 체크 다 포함됨
        String result = userCouponService.registerCoupon(userNo, couponCode);

        if ("SUCCESS".equals(result)) {
            log.info("이벤트 쿠폰 발급 성공 — userNo={}, couponCode={}", userNo, couponCode);
        } else {
            log.warn("이벤트 쿠폰 발급 실패 — {}, userNo={}", result, userNo);
        }
    }

    public GoldEventLogDTO findLastEvent(int userNo) {
        return goldEventMapper.findLastEvent(userNo);
    }

    public Double getGoldResultPrice(LocalDateTime resultAt) {
        if (resultAt == null) return null;

        LocalDate resultDate = resultAt.toLocalDate(); // 🔥 핵심
        return goldEventMapper.findGoldPriceByResultDate(resultDate);
    }

}
