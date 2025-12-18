package kr.co.busanbank.service.chat;

import kr.co.busanbank.dto.chat.ChatMessageHistoryItem;
import kr.co.busanbank.dto.chat.ChatSessionHistoryItem;
import kr.co.busanbank.mapper.ChatHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final ChatHistoryMapper mapper;

    public Map<String, Object> getSessions(int userNo, String cursor, int size) {
        List<ChatSessionHistoryItem> items =
                mapper.selectSessionHistory(userNo, cursor, size);

        if (items == null) items = Collections.emptyList();

        String nextCursor = null;
        if (!items.isEmpty()) {
            nextCursor = String.valueOf(items.get(items.size() - 1).getSessionId());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("items", items);
        data.put("nextCursor", nextCursor);

        Map<String, Object> res = new HashMap<>();
        res.put("status", "SUCCESS");
        res.put("data", data);
        return res;
    }

    public Map<String, Object> getMessages(int userNo, int sessionId, String cursor, int size) {
        List<ChatMessageHistoryItem> items =
                mapper.selectMessageHistory(userNo, sessionId, cursor, size);

        if (items == null) items = Collections.emptyList();

        String nextCursor = null;
        if (!items.isEmpty()) {
            nextCursor = String.valueOf(items.get(items.size() - 1).getMessageId());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("items", items);
        data.put("nextCursor", nextCursor);

        Map<String, Object> res = new HashMap<>();
        res.put("status", "SUCCESS");
        res.put("data", data);
        return res;
    }
}
