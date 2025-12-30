package kr.co.busanbank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // 409
public class AlreadyParticipatedException extends RuntimeException {

    public AlreadyParticipatedException() {
        super("이미 오늘 이벤트에 참여했습니다.");
    }
}