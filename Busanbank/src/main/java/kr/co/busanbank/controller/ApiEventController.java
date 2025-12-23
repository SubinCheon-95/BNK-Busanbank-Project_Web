/*
   날짜: 2025/12/23
   내용: 금열매 이벤트 플러터 연동
   작성자: 오서정
*/
package kr.co.busanbank.controller;

import kr.co.busanbank.dto.GoldEventLogDTO;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.service.GoldEventService;
import kr.co.busanbank.service.MyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
public class ApiEventController {

    private final MyService myService;
    private final GoldEventService goldEventService;

    @PostMapping("/gold")
    public Map<String, Object> goldPick(Authentication auth) {
        UsersDTO user = (UsersDTO) auth.getPrincipal();
        String userId = user.getUserId();
        int userNo = myService.findUserNo(userId);

        log.info("goldevent userId = {}, userNo = {}", userId, userNo);

        GoldEventLogDTO today = goldEventService.findTodayEvent(userNo);
        if (today != null) {
            return Map.of("already", true);
        }

        double errorRate = getRandomRange();
        double todayPrice = goldEventService.getTodayGoldPrice();

        double errorAmount = todayPrice * (errorRate / 100);
        double min = todayPrice - errorAmount;
        double max = todayPrice + errorAmount;

        goldEventService.saveEvent(userId, todayPrice, errorRate, min, max);

        return Map.of(
                "already", false,
                "errorRate", errorRate,
                "errorAmount", errorAmount,
                "min", min,
                "max", max,
                "todayPrice", todayPrice
        );
    }

    @GetMapping("/status")
    public Map<String, Object> getEventStatus(Authentication auth) {

        UsersDTO user = (UsersDTO) auth.getPrincipal();
        String userId = user.getUserId();
        int userNo = myService.findUserNo(userId);

        GoldEventLogDTO today = goldEventService.findTodayEvent(userNo);
        GoldEventLogDTO last = goldEventService.findLastEvent(userNo);

        log.info("gold event today={},last={}", today, last);

        // ------------------------------
        // CASE 1: 오늘 기록 있음 (결과 대기/확정)
        // ------------------------------
        if (today != null) {
            double errorAmount = today.getTodayPrice() * (today.getErrorRate() / 100);

            return Map.of(
                    "todayStatus", today.getResult(),   // WAIT / FAIL / SUCCESS
                    "pastStatus", (last != null ? last.getResult() : "NONE"),
                    "errorRate", today.getErrorRate(),
                    "minPrice", today.getMinPrice(),
                    "maxPrice", today.getMaxPrice(),
                    "todayPrice", today.getTodayPrice(),
                    "errorAmount", errorAmount
            );
        }

        // ------------------------------
        // CASE 2: 오늘 기록 없음 + 과거 기록 있음
        // ------------------------------
        if (last != null) {

            if ("WAIT".equals(last.getResult())) {
                return Map.of(
                        "todayStatus", "NONE",
                        "pastStatus", "WAIT",
                        "todayPrice", goldEventService.getTodayGoldPrice()
                );
            }

            // 🔒 과거 SUCCESS → 오늘 재도전 불가
            if ("SUCCESS".equals(last.getResult())) {
                return Map.of(
                        "todayStatus", "NONE",
                        "pastStatus", "SUCCESS",
                        "minPrice", last.getMinPrice(),
                        "maxPrice", last.getMaxPrice(),
                        "todayPrice", goldEventService.getTodayGoldPrice()
                );
            }

            // 🔁 과거 FAIL → 오늘 재도전 가능
            if ("FAIL".equals(last.getResult())) {
                return Map.of(
                        "todayStatus", "NONE",
                        "pastStatus", "FAIL",
                        "minPrice", last.getMinPrice(),
                        "maxPrice", last.getMaxPrice(),
                        "todayPrice", goldEventService.getTodayGoldPrice()
                );
            }
        }

        // ------------------------------
        // CASE 3: 신규 사용자
        // ------------------------------
        return Map.of(
                "todayStatus", "NONE",
                "pastStatus", "NONE",
                "todayPrice", goldEventService.getTodayGoldPrice()
        );
    }
    private double getRandomRange() {
        double random = Math.random();
        if (random < 0.4) return 1.0;
        else if (random < 0.8) return 0.5;
        else return 0.3;
    }
}
