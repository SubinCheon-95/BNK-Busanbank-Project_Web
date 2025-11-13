package kr.co.busanbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling //스케줄러 -> 하루 지나면 DB 상태 업데이트
@SpringBootApplication
public class BusanbankApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusanbankApplication.class, args);
    }

}
