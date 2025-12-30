package kr.co.busanbank.call.controller;

import kr.co.busanbank.call.dto.CallTokenRequest;
import kr.co.busanbank.call.dto.CallTokenResponse;
import kr.co.busanbank.call.service.CallTokenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/call")
public class CallController {

    private final CallTokenService service;

    public CallController(CallTokenService service) {
        this.service = service;
    }

    @PostMapping("/token")
    public CallTokenResponse token(@RequestBody CallTokenRequest request) {
        return service.issue(request.getSessionId(), request.getRole());
    }
}
