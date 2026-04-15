package pt.uc.dei.proj5.dao;

import jakarta.ejb.Stateless;
import pt.uc.dei.proj5.entity.MensagemEntity;
import pt.uc.dei.proj5.entity.UserEntity;

import java.io.Serializable;
import java.util.List;

@Stateless
public class MensagemDao extends DefaultDao<MensagemEntity> implements Serializable {

    public MensagemDao() {
        super(MensagemEntity.class);
    }

    // Vai buscar a conversa entre dois utilizadores ordenada pela data mais antiga para a mais recente
    public List<MensagemEntity> getHistorico(UserEntity u1, UserEntity u2) {
        return em.createQuery(
                        "SELECT m FROM MensagemEntity m WHERE " +
                                "(m.remetente = :u1 AND m.destinatario = :u2) OR " +
                                "(m.remetente = :u2 AND m.destinatario = :u1) " +
                                "ORDER BY m.dataEnvio ASC", MensagemEntity.class)
                .setParameter("u1", u1)
                .setParameter("u2", u2)
                .getResultList();
    }

    // Conta quantas mensagens não lidas um utilizador tem (para a badge das notificações)
    public long contarNaoLidas(UserEntity destinatario) {
        return em.createQuery(
                        "SELECT COUNT(m) FROM MensagemEntity m WHERE m.destinatario = :destinatario AND m.lida = false", Long.class)
                .setParameter("destinatario", destinatario)
                .getSingleResult();
    }

    // Conta as mensagens não lidas que vieram de UM remetente específico
    public long contarNaoLidasDe(UserEntity remetente, UserEntity destinatario) {
        return em.createQuery(
                        "SELECT COUNT(m) FROM MensagemEntity m WHERE m.remetente = :remetente AND m.destinatario = :destinatario AND m.lida = false", Long.class)
                .setParameter("remetente", remetente)
                .setParameter("destinatario", destinatario)
                .getSingleResult();
    }

    // Marca as mensagens de uma conversa específica como lidas quando o utilizador abre a janela
    public void marcarComoLidas(UserEntity remetente, UserEntity destinatario) {
        em.createQuery(
                        "UPDATE MensagemEntity m SET m.lida = true " +
                                "WHERE m.remetente = :remetente AND m.destinatario = :destinatario AND m.lida = false")
                .setParameter("remetente", remetente)
                .setParameter("destinatario", destinatario)
                .executeUpdate();
    }
}