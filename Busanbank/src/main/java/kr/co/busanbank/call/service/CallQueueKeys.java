package kr.co.busanbank.call.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "chat.call")
public class CallQueueKeys {

    /**
     * chat.call.waitingZsetPrefix
     * 예) chat:call:waiting:
     */
    private String waitingZsetPrefix = "chat:call:waiting:";

    /**
     * chat.call.assignedWatchZset
     */
    private String assignedWatchZset = "chat:call:assigned:watch";

    /**
     * chat.call.sessionPrefix
     * 예) chat:session:
     */
    private String sessionPrefix = "chat:session:";

    public String callQueue(String inquiryType) {
        String type = (inquiryType == null || inquiryType.isBlank()) ? "default" : inquiryType.trim();
        return waitingZsetPrefix + type;
    }

    public String assignedWatchZset() {
        return assignedWatchZset;
    }

    public String sessionKey(String sessionId) {
        return sessionPrefix + sessionId;
    }

    // getters/setters (ConfigurationProperties 바인딩용)
    public String getWaitingZsetPrefix() { return waitingZsetPrefix; }
    public void setWaitingZsetPrefix(String waitingZsetPrefix) { this.waitingZsetPrefix = waitingZsetPrefix; }

    public String getAssignedWatchZset() { return assignedWatchZset; }
    public void setAssignedWatchZset(String assignedWatchZset) { this.assignedWatchZset = assignedWatchZset; }

    public String getSessionPrefix() { return sessionPrefix; }
    public void setSessionPrefix(String sessionPrefix) { this.sessionPrefix = sessionPrefix; }
}
