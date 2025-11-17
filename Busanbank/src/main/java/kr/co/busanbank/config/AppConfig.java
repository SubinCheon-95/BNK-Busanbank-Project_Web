package kr.co.busanbank.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${spring.application.name}")
    private String name;

    @Value("${spring.application.version}")
    private String version;

    @Bean
    public AppInfo appInfo() {
        System.out.println("### AppInfo Bean 생성됨 ::: " + name + " / " + version);

        AppInfo info = new AppInfo();
        info.setAppName(name);
        info.setAppVersion(version);
        return info;
    }

}
