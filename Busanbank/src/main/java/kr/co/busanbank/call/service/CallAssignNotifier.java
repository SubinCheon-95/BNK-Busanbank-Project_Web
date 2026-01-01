package kr.co.busanbank.call.service;

public interface CallAssignNotifier {
    void notifyAssigned(String sessionId, String consultantId, String agoraChannel);
}
