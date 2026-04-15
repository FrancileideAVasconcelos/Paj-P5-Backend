package pt.uc.dei.proj5.websocket;

import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import pt.uc.dei.proj5.beans.UserBean;
import pt.uc.dei.proj5.entity.UserEntity;

@ServerEndpoint("/ws/chat/{token}")
public class ChatEndpoint {

    @Inject
    WebSocketManager sessionManager;

    @Inject
    UserBean userBean;

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        System.out.println("TENTATIVA DE LIGAÇÃO WEBSOCKET. Token recebido: " + token);
        try {
            UserEntity user = userBean.getUser(token);
            if (user != null) {
                sessionManager.addSession(user.getUsername(), session);
                System.out.println("Utilizador validado e ligado: " + user.getUsername());
            } else {
                System.out.println("FALHA! Token inválido ou expirado.");
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Token Inválido"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("token") String token) {
        try {
            UserEntity user = userBean.getUser(token);
            if (user != null) {
                sessionManager.removeSession(user.getUsername());
            }
        } catch (Exception e) {
            // Ignora falhas silenciosas ao fechar
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Erro no WebSocket: " + throwable.getMessage());
    }

    // Este método serve APENAS para receber o "Ping" do React e manter a ligação viva.
    // As mensagens de texto a sério continuam a ir por REST!
    @OnMessage
    public void onMessage(String message, Session session) {
        if ("PING".equals(message)) {
            // Não precisamos de fazer absolutamente nada!
            // Só o facto de a mensagem ter chegado já reiniciou o contador do WildFly para mais 90 segundos.
            // Podes até deixar este System.out só para veres a magia a acontecer (ou apagá-lo depois):
            System.out.println("💓 Heartbeat recebido do WebSocket!");
        }
    }

}