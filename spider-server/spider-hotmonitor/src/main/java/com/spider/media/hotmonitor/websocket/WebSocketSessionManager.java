package com.spider.media.hotmonitor.websocket;

import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class WebSocketSessionManager {

    private static final ConcurrentHashMap<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    public static void addSession(Long userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    public static void removeSession(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            Set<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }
    }

    public static void sendToUser(Long userId, String message) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        synchronized (session) {
                            session.sendMessage(new org.springframework.web.socket.TextMessage(message));
                        }
                    } catch (IOException e) {
                        // ignore disconnected session
                    }
                }
            }
        }
    }

    public static void broadcast(String message) {
        for (Set<WebSocketSession> sessions : userSessions.values()) {
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        synchronized (session) {
                            session.sendMessage(new org.springframework.web.socket.TextMessage(message));
                        }
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
    }
}
