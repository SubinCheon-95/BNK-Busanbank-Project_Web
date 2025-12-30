package kr.co.busanbank.call.dto;

public class CallTokenRequest {

    private String sessionId;
    private String role; // CUSTOMER | AGENT

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
