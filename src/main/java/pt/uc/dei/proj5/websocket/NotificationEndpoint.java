package pt.uc.dei.proj5.websocket;

import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import pt.uc.dei.proj5.beans.UserBean;
import pt.uc.dei.proj5.entity.UserEntity;

@ServerEndpoint("/ws/notifications/{token}")
public class NotificationEndpoint {

    @Inject
    NotificationWebSocketManager notifManager;

    @Inject
    UserBean userBean;

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        try {
            UserEntity user = userBean.getUser(token);
            if (user != null) {
                notifManager.addSession(user.getUsername(), session);
            } else {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Token Inválido"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if ("PING".equals(message)) {
            // Heartbeat para manter a ligação de notificações viva!
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("token") String token) {
        try {
            UserEntity user = userBean.getUser(token);
            if (user != null) {
                notifManager.removeSession(user.getUsername());
            }
        } catch (Exception e) { }
    }
}