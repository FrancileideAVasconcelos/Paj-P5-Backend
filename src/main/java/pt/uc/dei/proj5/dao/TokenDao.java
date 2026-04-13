package pt.uc.dei.proj5.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import pt.uc.dei.proj5.entity.TokenEntity;
import pt.uc.dei.proj5.entity.UserEntity;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;

@Stateless
public class TokenDao extends DefaultDao<TokenEntity> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public TokenDao() {
        super(TokenEntity.class);
    }


    public String encriptar(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao encriptar token", e);
        }
    }

    public void guardarTokenDB(String tokenLimpo, UserEntity u) {

        String tokenEncriptado = encriptar(tokenLimpo);

        // Guarda o token na tabela 'token' associado a este utilizador
        TokenEntity tokenEntity = new TokenEntity();
        tokenEntity.setToken(tokenEncriptado); // A Primary Key é o token encriptado
        tokenEntity.setUserId(u);
        tokenEntity.setDataSessao(LocalDateTime.now());
        tokenEntity.setExpireTime(LocalDateTime.now().plusHours(1)); // Expira numa hora

        persist(tokenEntity);

    }

    public UserEntity getUserByToken(String token) {
        if (token == null) return null;

        String tokenEncriptado = encriptar(token);

        try {
            // Faz um JOIN com a tabela de tokens para encontrar o dono do token válido
            return em.createQuery(
                            "SELECT u FROM UserEntity u JOIN u.tokens t WHERE t.token = :token AND t.expireTime > CURRENT_TIMESTAMP AND u.isAtivo",
                            UserEntity.class)
                    .setParameter("token", tokenEncriptado)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void setExpired(String token) {
        String tokenEncriptado = encriptar(token);

        em.createQuery("UPDATE TokenEntity t SET t.expireTime = CURRENT_TIMESTAMP WHERE t.token = :token")
                .setParameter("token", tokenEncriptado)
                .executeUpdate();
    }

}
