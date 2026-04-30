package pt.uc.dei.proj5.beans;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import pt.uc.dei.proj5.dao.MensagemDao;
import pt.uc.dei.proj5.dao.UserDao;
import pt.uc.dei.proj5.dto.MensagemDto;
import pt.uc.dei.proj5.dto.UserDto;
import pt.uc.dei.proj5.entity.MensagemEntity;
import pt.uc.dei.proj5.entity.UserEntity;
import pt.uc.dei.proj5.websocket.NotificationWebSocketManager;
import pt.uc.dei.proj5.websocket.WebSocketManager;

import java.util.ArrayList;
import java.util.List;

@Stateless
public class ChatBean {

    @Inject
    MensagemDao mensagemDao;

    @Inject
    UserDao userDao;

    @Inject
    WebSocketManager wsManager;

    @Inject
    NotificationWebSocketManager notifManager;


    // 5. Vai buscar os contactos e ordena-os pela mensagem mais recente
    public List<UserDto> getContactosOrdenados(UserEntity eu) {
        List<UserEntity> ativos = userDao.getActiveUsersExcluindo(eu.getUsername());
        List<pt.uc.dei.proj5.dto.UserDto> dtos = new ArrayList<>();

        // Mapa para guardar a data de cada um temporariamente
        java.util.Map<String, java.time.LocalDateTime> ultimasDatas = new java.util.HashMap<>();

        for (UserEntity u : ativos) {
            pt.uc.dei.proj5.dto.UserDto dto = new pt.uc.dei.proj5.dto.UserDto();
            dto.setUsername(u.getUsername());
            dto.setPrimeiroNome(u.getPrimeiroNome());
            dto.setUltimoNome(u.getUltimoNome());
            dto.setFotoUrl(u.getFotoUrl());
            // Mantém a lógica das notificações não lidas!
            dto.setUnreadCount(mensagemDao.contarNaoLidasDe(u, eu));

            // Descobre a data da última mensagem
            java.time.LocalDateTime ultima = mensagemDao.getUltimaInteracao(eu, u);
            if (ultima == null) {
                // Se nunca falaram, coloca uma data "zero" (1970) para irem para o fundo da lista
                ultima = java.time.LocalDateTime.of(1970, 1, 1, 0, 0);
            }
            ultimasDatas.put(u.getUsername(), ultima);

            dtos.add(dto);
        }

        // Ordena a lista: Data mais recente (maior) para a mais antiga (menor)
        dtos.sort((d1, d2) -> ultimasDatas.get(d2.getUsername()).compareTo(ultimasDatas.get(d1.getUsername())));

        return dtos;
    }

    // 1. Grava na BD via REST e avisa via WebSocket
    public MensagemDto enviarMensagem(String remetenteUsername, String destinatarioUsername, String conteudo) throws Exception {
        UserEntity remetente = userDao.checkUsername(remetenteUsername);
        UserEntity destinatario = userDao.checkUsername(destinatarioUsername);

        if (remetente == null || destinatario == null) {
            throw new Exception("Utilizador não encontrado.");
        }

        // Cria a entidade e guarda na Base de Dados
        MensagemEntity msg = new MensagemEntity();
        msg.setRemetente(remetente);
        msg.setDestinatario(destinatario);
        msg.setConteudo(conteudo);
        msg.setLida(false);

        mensagemDao.persist(msg);

        // Prepara o DTO para devolver e para enviar pelo WebSocket
        MensagemDto dto = new MensagemDto(
                remetente.getUsername(),
                destinatario.getUsername(),
                conteudo,
                msg.getDataEnvio(),
                false
        );

        // MAGIA DO TEMPO REAL: Enviar notificação para o destinatário!
        try (Jsonb jsonb = JsonbBuilder.create()) {
            // Envia a mensagem propriamente dita
            String jsonWs = "{\"type\": \"NEW_MESSAGE\", \"payload\": " + jsonb.toJson(dto) + "}";
            wsManager.sendMessageToUser(destinatarioUsername, jsonWs);

            // Envia o novo contador de mensagens não lidas VIA WS DE NOTIFICAÇÕES
            long unreadCount = mensagemDao.contarNaoLidas(destinatario);
            String unreadJson = "{\"type\": \"UNREAD_COUNT\", \"count\": " + unreadCount + "}";
            notifManager.sendNotification(destinatarioUsername, unreadJson); // <-- Alterado aqui!
        }

        return dto;
    }

    // 2. Vai buscar o histórico de uma conversa entre duas pessoas
    public List<MensagemDto> getHistorico(String username1, String username2) {
        UserEntity u1 = userDao.checkUsername(username1);
        UserEntity u2 = userDao.checkUsername(username2);

        List<MensagemEntity> entidades = mensagemDao.getHistorico(u1, u2);
        List<MensagemDto> dtos = new ArrayList<>();

        for(MensagemEntity m : entidades) {
            dtos.add(new MensagemDto(
                    m.getRemetente().getUsername(),
                    m.getDestinatario().getUsername(),
                    m.getConteudo(),
                    m.getDataEnvio(),
                    m.isLida()
            ));
        }
        return dtos;
    }

    // 3. Marca mensagens como lidas quando o utilizador abre o chat
    public void marcarComoLidas(String leitorUsername, String remetenteUsername) {
        UserEntity leitor = userDao.checkUsername(leitorUsername);
        UserEntity remetente = userDao.checkUsername(remetenteUsername);

        mensagemDao.marcarComoLidas(remetente, leitor);

        // Atualiza o balão de notificações do leitor VIA WS DE NOTIFICAÇÕES
        long unreadCount = mensagemDao.contarNaoLidas(leitor);
        String unreadJson = "{\"type\": \"UNREAD_COUNT\", \"count\": " + unreadCount + "}";
        notifManager.sendNotification(leitorUsername, unreadJson); // <-- Alterado aqui!
    }

    // 4. Vai buscar apenas o número de notificações globais não lidas (usado no login/refresh)
    public long getUnreadCount(String username) {
        UserEntity user = userDao.checkUsername(username);
        if (user != null) {
            return mensagemDao.contarNaoLidas(user);
        }
        return 0;
    }
}