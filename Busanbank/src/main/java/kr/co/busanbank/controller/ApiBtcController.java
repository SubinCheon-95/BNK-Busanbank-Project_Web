package kr.co.busanbank.controller;

import kr.co.busanbank.dto.UserCouponDTO;
import kr.co.busanbank.service.BtcService;
import kr.co.busanbank.service.UserCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiBtcController {
    private final BtcService btcService;
    private final UserCouponService userCouponService;

    @PostMapping("/btcEvent")
    @ResponseBody
    public String apiBtcEvent(@RequestBody Map<String, String> data) { //비트코인 이벤트 - 작성자: 윤종인 2025.12.23
        log.info("테스트 = {}", data.toString());

        String result = data.get("result");

        int userNo = Integer.parseInt(data.get("userNo"));

        log.info("JWT 사용자 userNo = {}", userNo);
        log.info("Flutter에서 받은 결과 = {}", result);

        if ("success".equals(result)) {
            List<UserCouponDTO> coupons = btcService.couponSearch(userNo);

            for (UserCouponDTO coupon : coupons) {
                if (coupon.getCouponId() == 7 && coupon.getUserId() == null) {
                    userCouponService.registerCoupon(userNo, coupon.getCouponCode());
                    btcService.markUserParticipated(userNo, 7);
                    return "success";
                }
            }
        } else {
            btcService.markUserParticipated(userNo, 7);
        }

        return "fail";
    }
}
