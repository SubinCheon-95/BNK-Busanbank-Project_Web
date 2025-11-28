package kr.co.busanbank.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 경로 끝에 슬래시가 없으면 추가
        String path = uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";

        log.info("파일 업로드 경로 설정: {}", path);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + path);
    }
}