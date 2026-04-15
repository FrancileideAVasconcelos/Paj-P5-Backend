package pt.uc.dei.proj5.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class NotificationWebSocketManager {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public void addSession(String username, Session session) {
        sessions.put(username, session);
        System.out.println("🔔 WS Notificações Ligado: " + username);
    }

    public void removeSession(String username) {
        sessions.remove(username);
        System.out.println("🔕 WS Notificações Desligado: " + username);
    }

    public void sendNotification(String username, String messageJson) {
        Session session = sessions.get(username);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(messageJson);
            } catch (IOException e) {
                System.err.println("Erro ao enviar notificação para " + username + ": " + e.getMessage());
            }
        }
    }
}