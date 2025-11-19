package kr.co.busanbank.config;

import kr.co.busanbank.service.SiteSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Map;

/**
 * 작성자: 진원
 * 수정일: 2025-11-19 (사이트 설정 데이터 전역 제공 추가)
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final AppInfo appInfo;
    private final SiteSettingService siteSettingService;

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        model.addAttribute("appInfo", appInfo);

        // 사이트 설정 데이터를 모든 페이지에서 사용 가능하도록 추가 (작성자: 진원, 2025-11-19)
        try {
            Map<String, String> siteSettings = siteSettingService.getSettingsAsMap();
            model.addAttribute("siteSettings", siteSettings);
        } catch (Exception e) {
            // 에러 발생 시 빈 Map 추가 (페이지 렌더링은 계속 진행)
            model.addAttribute("siteSettings", Map.of());
        }
    }
}
