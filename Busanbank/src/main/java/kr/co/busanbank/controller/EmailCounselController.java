package kr.co.busanbank.controller;

import jakarta.validation.Valid;
import kr.co.busanbank.dto.CodeDetailDTO;
import kr.co.busanbank.dto.EmailCounselDTO;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.helper.CategoryPageHelper;
import kr.co.busanbank.security.MyUserDetails;
import kr.co.busanbank.service.CsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class EmailCounselController {

    private final CsService csService;
    private final CategoryPageHelper categoryPageHelper;

    @GetMapping("/cs/customerSupport/login/onlineCounsel")
    public String onlineCounsel(@AuthenticationPrincipal MyUserDetails principal,
                                Model model) throws Exception {
        // 1) 로그인 체크
        if (principal == null) {
            return "redirect:/member/login"; // 실제 로그인 URL에 맞게 수정
        }

        // 2) 로그인 유저
        String userId = principal.getUsername(); // = usersDTO.getUserId()
        UsersDTO loginUser = csService.getUserById(userId);

        // 3) 이메일을 아이디/도메인으로 나누기
        String email = loginUser.getEmail() != null ? loginUser.getEmail() : "";
        String emailId = "";
        String emailDomain = "";

        if (!email.isEmpty() && email.contains("@")) {
            String[] parts = email.split("@", 2);
            emailId = parts[0];
            emailDomain = parts[1];
        }

        // 4) CS_TYPE 카테고리 코드 목록 가져오기
        List<CodeDetailDTO> cateList = csService.getCsCategories();

        // 5) 이메일 상담 폼 기본값
        EmailCounselDTO form = new EmailCounselDTO();
        form.setUserId(loginUser.getUserNo());  // int면 그대로
        form.setGroupCode("CS_TYPE");
        form.setStatus("REGISTERED");

        // 7) 화면에 넘길 데이터 넣기
        model.addAttribute("form", form);
        model.addAttribute("cateList", cateList);
        model.addAttribute("loginUser", loginUser); // 이름/이메일/휴대폰 사용
        model.addAttribute("emailId", emailId);
        model.addAttribute("emailDomain", emailDomain);

        categoryPageHelper.setupPage(34, model);

        return "cs/customerSupport/login/onlineCounsel";
    }

    @PostMapping("/cs/customerSupport/login/onlineCounsel")
    public String register(@Valid @ModelAttribute("form") EmailCounselDTO form,
                           @AuthenticationPrincipal MyUserDetails principal,
                           Model model) throws Exception {

        if (principal == null) {
            return "redirect:/member/login";
        }

        String userId = principal.getUsername();
        UsersDTO loginUser = csService.getUserById(userId);
        form.setUserId(loginUser.getUserNo());
        form.setGroupCode("CS_TYPE");

        csService.registerEmailCounsel(form);

        categoryPageHelper.setupPage(34, model);

        return "redirect:/cs/customerSupport/login/emailList";

    }

    // ================== 목록 ==================

    @GetMapping("/cs/customerSupport/login/emailList")
    public String emailList(@AuthenticationPrincipal MyUserDetails principal,
                            Model model) throws Exception {

        String userId = principal.getUsername();
        UsersDTO loginUser = csService.getUserById(userId);

        List<EmailCounselDTO> list =
                csService.getMyEmailCounselList(loginUser.getUserNo());

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("list", list);

        categoryPageHelper.setupPage(34, model); // 동일 카테고리 사용

        return "cs/customerSupport/login/emailList";
    }

    // ================== 상세 ==================

    @GetMapping("/cs/customerSupport/login/emailView")
    public String emailView(@RequestParam("id") int ecounselId,
                            @AuthenticationPrincipal MyUserDetails principal,
                            Model model) throws Exception {

        String userId = principal.getUsername();
        UsersDTO loginUser = csService.getUserById(userId);

        EmailCounselDTO counsel =
                csService.getEmailCounsel(ecounselId, loginUser.getUserNo());

        if (counsel == null) {
            // 남의 글이거나 없으면 목록으로
            return "redirect:/cs/customerSupport/login/emailList";
        }

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("counsel", counsel);

        categoryPageHelper.setupPage(34, model);

        return "cs/customerSupport/login/emailView";
    }
}
