package kr.co.busanbank.controller;

import jakarta.validation.Valid;
import kr.co.busanbank.dto.EmailCounselCreateRequest;
import kr.co.busanbank.dto.EmailCounselDTO;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.security.MyUserDetails;
import kr.co.busanbank.service.CsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cs/email")
@RequiredArgsConstructor
public class EmailCounselApiController {

    private final CsService csService;

    // ✅ 1) 폼 자동 채움(이름/휴대폰/이메일) + (옵션) 카테고리 리스트
    @GetMapping("/form")
    public Map<String, Object> form(Authentication authentication) throws Exception {
        UsersDTO loginUser = resolveLoginUser(authentication);

        return Map.of(
                "status", "SUCCESS",
                "data", Map.of(
                        "userName", loginUser.getUserName(),
                        "hp", loginUser.getHp(),
                        "email", loginUser.getEmail(),
                        "categories", csService.getCsCategories() // 필요없으면 제거 가능
                )
        );
    }

    // ✅ 2) 이메일 상담 등록(카테고리/제목/내용)
    @PostMapping
    public Map<String, Object> submit(
            Authentication authentication,
            @Valid @RequestBody EmailCounselCreateRequest req
    ) throws Exception {
        UsersDTO loginUser = resolveLoginUser(authentication);

        EmailCounselDTO dto = new EmailCounselDTO();
        dto.setUserId(loginUser.getUserNo());
        dto.setGroupCode("CS_TYPE");
        dto.setCsCategory(req.getCsCategory());
        dto.setTitle(req.getTitle());
        dto.setContent(req.getContent());
        dto.setStatus("REGISTERED");

        // ✅ 사용자가 수정한 값 저장
        dto.setContactEmail(req.getContactEmail());

        csService.registerEmailCounsel(dto);
        return Map.of("status", "SUCCESS");
    }

    // ✅ 3) 내 이메일 상담 목록
    @GetMapping("/my")
    public Map<String, Object> myList(Authentication authentication) throws Exception {
        UsersDTO loginUser = resolveLoginUser(authentication);
        List<EmailCounselDTO> list = csService.getMyEmailCounselList(loginUser.getUserNo());

        return Map.of("status", "SUCCESS", "data", list);
    }

    // ✅ 4) 상세
    @GetMapping("/{ecounselId}")
    public Map<String, Object> detail(
            Authentication authentication,
            @PathVariable int ecounselId
    ) throws Exception {
        UsersDTO loginUser = resolveLoginUser(authentication);
        EmailCounselDTO dto = csService.getEmailCounsel(ecounselId, loginUser.getUserNo());

        if (dto == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "NOT_FOUND");
        return Map.of("status", "SUCCESS", "data", dto);
    }

    // -------------------------
    // 로그인 사용자 해석 (컨트롤러에서만 해결)
    // -------------------------
    private UsersDTO resolveLoginUser(Authentication authentication) throws Exception {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "LOGIN_REQUIRED");
        }

        Object p = authentication.getPrincipal();
        String userId;

        // 1) MyUserDetails
        if (p instanceof MyUserDetails mud && mud.getUsersDTO() != null) {
            userId = mud.getUsersDTO().getUserId(); // ✅ String userId
        }
        // 2) UsersDTO
        else if (p instanceof UsersDTO u) {
            userId = u.getUserId(); // ✅ String userId
        }
        // 3) String(username) 등
        else {
            userId = p.toString();
        }

        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "LOGIN_REQUIRED");
        }

        UsersDTO loginUser = csService.getUserById(userId); // ✅ 복호화 포함
        if (loginUser == null || loginUser.getUserNo() == 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND");
        }
        return loginUser;
    }
}