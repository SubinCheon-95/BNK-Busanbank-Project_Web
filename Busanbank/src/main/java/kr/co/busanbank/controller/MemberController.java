/*
    날짜 : 2025/11/21
    이름 : 오서정
    내용 : 회원 기능 처리 컨트롤러 작성
*/
package kr.co.busanbank.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kr.co.busanbank.dto.TermDTO;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.jwt.JwtProvider;
import kr.co.busanbank.mapper.MemberMapper;
import kr.co.busanbank.security.AESUtil;
import kr.co.busanbank.service.EmailService;
import kr.co.busanbank.service.HpService;
import kr.co.busanbank.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/member")

public class MemberController {

    private final MemberService memberService;
    private final EmailService emailService;
    private final HpService hpService;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MemberMapper memberMapper;

    @GetMapping("/login")
    public String login(@RequestParam(value = "redirect_uri", required = false) String redirectUri,
                        @RequestParam(value = "error", required = false) String error,
                        Model model,
                        HttpSession session) {
        if (redirectUri != null) {
            session.setAttribute("redirect_uri", redirectUri);
        }
        /* 2025/12/01 - 회원 상태 처리(W:탈퇴중, D:탈퇴 시 로그인 제한) - 오서정 */
        if (error != null) {
            switch (error) {
                case "withdrawPending":
                    model.addAttribute("msg", "해당 계정은 현재 탈퇴 진행 중입니다. 고객센터로 문의해주세요.");
                    break;

                case "withdrawComplete":
                    model.addAttribute("msg", "아이디 또는 비밀번호가 잘못되었습니다.");
                    break;

                case "true":
                default:
                    model.addAttribute("msg", "아이디 또는 비밀번호가 잘못되었습니다.");
                    break;
            }
        }

        return "member/login";
    }

    @GetMapping("/register")
    public String register() {
        return "member/register";
    }

    /**
     * 회원가입 처리
     * 작성자: 진원, 2025-11-20 (비밀번호 정책 검증 추가)
     */
    @PostMapping("/register")
    public String register(UsersDTO usersDTO, HttpServletRequest req, Model model) throws Exception {
        log.info(usersDTO.toString());

        try {
            Random random = new Random();
            int randomInt = random.nextInt(999999999);
            usersDTO.setUserNo(randomInt);

            log.info("usersDTO = {}", usersDTO);

            memberService.save(usersDTO);

            return "redirect:/member/register/finish";
        } catch (IllegalArgumentException e) {
            // 비밀번호 정책 위반
            log.warn("회원가입 실패 - 비밀번호 정책 위반: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usersDTO", usersDTO);
            return "member/register";
        }
    }

    @GetMapping("/register/finish")
    public String registerFinish() {
        return "member/registerFinish";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        List<TermDTO> terms = memberService.findTermsAll();
        log.info("terms = {}", terms);
        model.addAttribute("terms", terms);
        return "member/signup";
    }

    @GetMapping("/find/id")
    public String userId(){
        return "member/find/id";
    }


    @PostMapping("/find/id")
    public String id(@RequestParam("authMethod") int authMethod,
                     String userName,
                     @RequestParam(value = "email", required = false) String email,
                     @RequestParam(value = "hp", required = false) String hp,
                     Model model) throws Exception {

        log.info("userName: {}, email: {}, hp: {}", userName, email, hp);
        if(authMethod == 1){
            UsersDTO findIdInfo = memberService.getUserIdInfoEmail(userName, email);
            if(findIdInfo == null){
                model.addAttribute("msg", "회원정보가 일치하지 않습니다.");
                return "member/find/id";
            } else {
                findIdInfo.setUserName(AESUtil.decrypt(findIdInfo.getUserName()));
                findIdInfo.setEmail(AESUtil.decrypt(findIdInfo.getEmail()));
                model.addAttribute("findIdInfo", findIdInfo);
                return "member/find/idResult";
            }
        }else if(authMethod == 2){
            UsersDTO finIdInfo = memberService.getUserIdInfoHp(userName, hp);
            if(finIdInfo == null){
                model.addAttribute("msg", "회원정보가 일치하지 않습니다.");
                return "member/find/id";
            }else{
                finIdInfo.setUserName(AESUtil.decrypt(finIdInfo.getUserName()));
                finIdInfo.setHp(AESUtil.decrypt(finIdInfo.getHp()));
                model.addAttribute("findIdInfo", finIdInfo);
                return "member/find/idResult";
            }
        }
        return "member/find/id";
    }

    @GetMapping("/find/pw")
    public String pw() {
        return "member/find/pw";
    }

    @PostMapping("/find/pw")
    public String pw(@RequestParam("authMethod") int authMethod,
                     String userName,
                     String userId,
                     @RequestParam(value = "email", required = false) String email,
                     @RequestParam(value = "hp", required = false) String hp,
                     Model model) throws Exception {

        log.info("userName: {}, userId: {}, email: {}, hp: {}", userName, userId, email, hp);
        if(authMethod == 1){
            UsersDTO findIdInfo = memberService.getUserPwInfoEmail(userName, userId, email);
            if(findIdInfo == null){
                model.addAttribute("msg", "회원정보가 일치하지 않습니다.");
                return "member/find/pw";
            } else {
                findIdInfo.setUserName(AESUtil.decrypt(findIdInfo.getUserName()));
                findIdInfo.setEmail(AESUtil.decrypt(findIdInfo.getEmail()));
                model.addAttribute("findIdInfo", findIdInfo);
                return "member/find/changePw";
            }
        }else if(authMethod == 2){
            UsersDTO finIdInfo = memberService.getUserPwInfoHp(userName, userId, hp);
            if(finIdInfo == null){
                model.addAttribute("msg", "회원정보가 일치하지 않습니다.");
                return "member/find/pw";
            }else{
                finIdInfo.setUserName(AESUtil.decrypt(finIdInfo.getUserName()));
                finIdInfo.setHp(AESUtil.decrypt(finIdInfo.getHp()));
                model.addAttribute("findIdInfo", finIdInfo);
                return "member/find/changePw";
            }
        }
        return "member/find/pw";
    }



    @GetMapping("/find/id/result")
    public String idResult() {
        return "member/find/idResult";
    }

    @GetMapping("/find/pw/change")
    public String changePw() {
        return "member/find/changePw";
    }

    /**
     * 비밀번호 변경 처리
     * 작성자: 진원, 2025-11-20 (비밀번호 정책 검증 추가)
     */
    @PostMapping("/find/pw/change")
    public String changePw(@RequestParam("userId") String userId,
                           @RequestParam("userPw") String userPw,
                           Model model) {
        log.info("userId: {}, userPw: {}", userId, userPw);

        try {
            memberService.modifyPw(userId, userPw);
            return "redirect:/member/find/pw/result";
        } catch (IllegalArgumentException e) {
            // 비밀번호 정책 위반
            log.warn("비밀번호 변경 실패 - 비밀번호 정책 위반: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("userId", userId);
            return "member/find/changePw";
        }
    }



    @GetMapping("/find/pw/result")
    public String pwResult() {
        return "member/find/pwResult";
    }


    // 2025/12/05 - 인증 관련 로직 수정 - 작성자: 오서정
    // API 요청 메서드
    @ResponseBody
    @GetMapping("/{type}/{value}")
    public ResponseEntity<Map<String, Integer>> getUserCount(@PathVariable("type") String type,
                                                             @PathVariable("value") String value) throws Exception {
        log.info("type = {}, value = {}", type, value);

//        String queryValue;
//        if ("userId".equals(type)) {
//            queryValue = value;
//        } else {
//            queryValue = AESUtil.encrypt(value); // 암호화
//        }

        int count = memberService.countUser(type, value);

        // Json 생성
        Map<String, Integer> map = Map.of("count", count);
        return ResponseEntity.ok(map);
    }


    // 2025/12/05 – 인증 전체 리팩터링 – 작성자: 오서정
    @ResponseBody
    @PostMapping("/email/send")
    public ResponseEntity<String> sendEmail(@RequestBody(required = false) Map<String,String> req) {

        String email = req.get("email");
        String mode  = req.get("mode"); // join / find

        int count = memberService.countUser("email", email);

        // 1) 회원가입 모드 (중복 불가)
        if("join".equals(mode)) {
            if(count > 0){
                return ResponseEntity.badRequest().body("이미 존재하는 이메일입니다.");
            }
            emailService.sendCode(email);
            return ResponseEntity.ok("인증 코드 발송 완료");
        }

        // 2) 아이디/비밀번호 찾기 모드 (존재해야 정상)
        if("find".equals(mode)) {
            if(count == 0){
                return ResponseEntity.badRequest().body("존재하지 않는 이메일입니다.");
            }
            emailService.sendCode(email);
            return ResponseEntity.ok("인증 코드 발송 완료");
        }

        // 그 외 다른 mode
        emailService.sendCode(email);
        return ResponseEntity.ok("인증 코드 발송 완료");
    }

    @ResponseBody
    @PostMapping("/hp/send")
    public ResponseEntity<String> sendHp(@RequestBody(required = false) Map<String,String> req) {

        /*2025/12/16 - 휴대폰인증 flutter 연동  - 작성자 : 오서정*/
        String hp   = req.get("hp");

        String mode = req.get("mode"); // join / find / app

        if ("app".equals(mode)) {
            hpService.sendCodeForApp(hp);
            return ResponseEntity.ok("인증 코드 발송 완료");
        }

        // mode 없을 때 (기본 인증)
        if (mode == null || mode.isBlank()) {
            hpService.sendCode(hp);
            return ResponseEntity.ok("인증 코드 발송 완료");
        }

        int count = memberService.countUser("hp", hp);

        // 회원가입
        if("join".equals(mode)) {
            if(count > 0){
                return ResponseEntity.badRequest().body("이미 존재하는 휴대폰입니다.");
            }
            hpService.sendCode(hp);
            return ResponseEntity.ok("인증 코드 발송 완료");
        }

        // 아이디/비밀번호 찾기
        if("find".equals(mode)) {
            if(count == 0){
                return ResponseEntity.badRequest().body("존재하지 않는 휴대폰입니다.");
            }
            hpService.sendCode(hp);
            return ResponseEntity.ok("인증 코드 발송 완료");
        }

        // 그 외 다른 mode
        hpService.sendCode(hp);
        return ResponseEntity.ok("인증 코드 발송 완료");
    }


    @GetMapping("/withdraw/finish")
    public String withdrawFinish() {
        return "member/withdrawFinish";
    }

    @GetMapping("/auto")
    public String auto() {
        return "member/autoLogout";
    }

    @GetMapping("/chatbot")
    public String chatbot() {
        return "member/chatbotTest";
    }


    /**
     *  상품 가입용 SMS/이메일 인증 검증 API
     */
    @PostMapping("/hp/verify")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> verifyHp(@RequestBody Map<String,String> req) {
        String code = req.get("code");
        boolean verified = hpService.verifyCode(code);

        Map<String, Boolean> result = Map.of("verified", verified);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/email/verify")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> verifyEmail(@RequestBody Map<String,String> req) {
        String code = req.get("code");
        boolean verified = emailService.verifyCode(code);

        Map<String, Boolean> result = Map.of("verified", verified);
        return ResponseEntity.ok(result);
    }

    /**
     * 약관 PDF 보기용 페이지 (인쇄 최적화)
     * 작성자: 진원, 2025-11-26
     */
    @GetMapping("/term/{termNo}")
    public String viewTermPrint(@PathVariable("termNo") int termNo, Model model) {
        log.info("회원가입 약관 PDF 보기 - termNo: {}", termNo);

        // 약관 조회
        TermDTO term = memberService.findTermById(termNo);

        if (term == null) {
            log.warn("약관을 찾을 수 없음 - termNo: {}", termNo);
            return "redirect:/member/signup";
        }

        model.addAttribute("term", term);
        return "member/termPrint";
    }

    /**
     * Flutter 로그인 API
     * POST /api/member/login
     * ✅ JWT 토큰 생성 및 반환
     * ✅ userNo 포함
     */
    // 25/12/17 - 아래 코드는 flutter post 맵핑이 안맞아서(/member/api/member/login이라) ApiMemberController(/api/member/login)으로 옮겨두었습니다.!
//    @PostMapping("/api/member/login")
//    @ResponseBody
//    public ResponseEntity<?> loginApi(@RequestBody Map<String, String> loginRequest) {
//
//        String userId = loginRequest.get("userId");
//        String userPw = loginRequest.get("userPw");
//
//        log.info("📱 [Flutter] 로그인 요청 - userId: {}", userId);
//
//        try {
//            // 1. 사용자 조회
//            UsersDTO user = memberMapper.findByUserId(userId);
//
//            if (user == null) {
//                log.warn("❌ 사용자 없음 - userId: {}", userId);
//                return ResponseEntity.status(401).body(Map.of("error", "로그인 실패"));
//            }
//
//            // 2. 비밀번호 검증
//            boolean passwordMatches = passwordEncoder.matches(userPw, user.getUserPw());
//
//            if (!passwordMatches) {
//                log.warn("❌ 비밀번호 불일치 - userId: {}", userId);
//                return ResponseEntity.status(401).body(Map.of("error", "로그인 실패"));
//            }
//
//            // 3. 회원 상태 확인
//            if ("W".equals(user.getStatus())) {
//                log.warn("❌ 탈퇴 진행중 - userId: {}", userId);
//                return ResponseEntity.status(401).body(Map.of("error", "탈퇴 진행중인 계정입니다"));
//            }
//
//            if ("S".equals(user.getStatus())) {
//                log.warn("❌ 탈퇴 완료 - userId: {}", userId);
//                return ResponseEntity.status(401).body(Map.of("error", "탈퇴 완료된 계정입니다"));
//            }
//
//            // 4. JWT 토큰 생성
//            String accessToken = jwtProvider.createToken(user, 1);  // 1일
//            String refreshToken = jwtProvider.createToken(user, 7);  // 7일
//
//            // 5. 응답 생성
//            Map<String, Object> result = new HashMap<>();
//            result.put("accessToken", accessToken);
//            result.put("refreshToken", refreshToken);
//            result.put("userNo", user.getUserNo());  // ✅ userNo 추가!
//            result.put("userId", user.getUserId());
//
//            log.info("✅ [Flutter] 로그인 성공 - userId: {}, userNo: {}", userId, user.getUserNo());
//
//            return ResponseEntity.ok(result);
//
//        } catch (Exception e) {
//            log.error("❌ [Flutter] 로그인 처리 중 오류", e);
//            return ResponseEntity.status(500).body(Map.of("error", "서버 오류"));
//        }
//    }

}

