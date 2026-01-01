package kr.co.busanbank.call.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "chat.call.assign")
public class CallAssignProperties {

    /**
     * 배정 워커 on/off
     */
    private boolean enabled = true;

    /**
     * 어떤 inquiryType 큐들을 돌릴지
     * - 비워두면 default 하나만 처리합니다.
     * - 예: ["예금","카드","대출"]
     */
    private List<String> queueTypes = new ArrayList<>();

    /**
     * 한 번 스케줄링에서 최대 몇 건 처리할지
     */
    private int maxPerTick = 20;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public List<String> getQueueTypes() { return queueTypes; }
    public void setQueueTypes(List<String> queueTypes) { this.queueTypes = queueTypes; }

    public int getMaxPerTick() { return maxPerTick; }
    public void setMaxPerTick(int maxPerTick) { this.maxPerTick = maxPerTick; }
}
