package pt.uc.dei.proj5.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class WebSocketManager {

    // Este mapa guarda a correspondência entre o Username e a Sessão aberta no browser
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    // Quando o utilizador entra no site, guardamos a sessão dele
    public void addSession(String username, Session session) {
        sessions.put(username, session);
        System.out.println("✅ WebSocket Ligado: " + username);
    }

    // Quando o utilizador fecha a aba ou faz logout
    public void removeSession(String username) {
        sessions.remove(username);
        System.out.println("❌ WebSocket Desligado: " + username);
    }

    // O método mágico que "empurra" mensagens para o ecrã de alguém específico
    public void sendMessageToUser(String username, String messageJson) {
        Session session = sessions.get(username);

        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(messageJson);
            } catch (IOException e) {
                System.err.println("Erro ao enviar WebSocket para " + username + ": " + e.getMessage());
            }
        }
    }
}