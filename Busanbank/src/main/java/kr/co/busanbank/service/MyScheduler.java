/*
    날짜 : 2025/12/01
    이름 : 오서정
    내용 : 마이페이지 스케줄러 작성
 */
package kr.co.busanbank.service;

import kr.co.busanbank.dto.GoldEventLogDTO;
import kr.co.busanbank.mapper.MyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MyScheduler {

    private final MyMapper myMapper;
    private final GoldEventService goldEventService;

    @Scheduled(cron = "0 0 0 * * *")
    public void autoCompleteWithdrawUsers() {
        myMapper.updateUsersToD();
    }

    //@Scheduled(cron = "0/30 * * * * *")
    @Scheduled(cron = "0 1 5 * * *")
    public void evaluate() {

        double todayPrice = goldEventService.getTodayGoldPrice();

        List<GoldEventLogDTO> waitList = goldEventService.findAllWait();

        for(GoldEventLogDTO e : waitList){

            boolean success = (todayPrice >= e.getMinPrice()
                    && todayPrice <= e.getMaxPrice());

            if(success){
                if(success){
                    e.setResult("SUCCESS");
                    goldEventService.giveCoupon(e.getUserNo(), "6");
                }
            }else{
                e.setResult("FAIL");
            }

            e.setResultAt(LocalDateTime.now());
            goldEventService.updateEvent(e);
        }
    }


}
