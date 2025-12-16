package kr.co.busanbank.controller;

import kr.co.busanbank.dto.TermDTO;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.jwt.JwtProvider;
import kr.co.busanbank.security.MyUserDetails;
import kr.co.busanbank.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/member")
public class ApiMemberController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UsersDTO dto) {

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        dto.getUserId(),
                        dto.getUserPw()
                );

        Authentication authentication =
                authenticationManager.authenticate(authToken);

        MyUserDetails userDetails =
                (MyUserDetails) authentication.getPrincipal();

        UsersDTO user = userDetails.getUsersDTO();

        String accessToken =
                jwtProvider.createToken(user, 1);
        String refreshToken =
                jwtProvider.createToken(user, 7);

        Map<String, String> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/terms")
    public ResponseEntity<List<TermDTO>> getTerms() {
        return ResponseEntity.ok(memberService.findTermsAll());
    }

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> apiRegister(@RequestBody UsersDTO dto) throws Exception {
        memberService.save(dto);
        return ResponseEntity.ok().build();
    }
}
