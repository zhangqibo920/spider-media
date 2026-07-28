package com.spider.media.hotmonitor.websocket;

import com.spider.media.framework.security.JwtToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class HotTopicWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(HotTopicWebSocketHandler.class);
    private final JwtToken jwtToken;

    public HotTopicWebSocketHandler(JwtToken jwtToken) {
        this.jwtToken = jwtToken;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : "";
        String token = null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2 && "token".equals(kv[0])) {
                    token = kv[1];
                    break;
                }
            }
        }
        if (token == null || !jwtToken.validateToken(token)) {
            try { session.close(CloseStatus.POLICY_VIOLATION); } catch (Exception ignored) {}
            return;
        }
        Long userId = jwtToken.getUserIdFromToken(token);
        session.getAttributes().put("userId", userId);
        WebSocketSessionManager.addSession(userId, session);
        log.debug("WebSocket连接建立, userId={}", userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        WebSocketSessionManager.removeSession(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    }
}
