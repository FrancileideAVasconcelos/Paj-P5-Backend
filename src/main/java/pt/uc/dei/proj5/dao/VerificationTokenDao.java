package pt.uc.dei.proj5.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import pt.uc.dei.proj5.entity.VerificationToken;
import java.io.Serializable;

@Stateless
public class VerificationTokenDao extends DefaultDao<VerificationToken> implements Serializable {

    public VerificationTokenDao() {
        super(VerificationToken.class);
    }

    public VerificationToken findByToken(String token) {
        try {
            return em.createQuery("SELECT t FROM VerificationToken t WHERE t.token = :token", VerificationToken .class)
                    .setParameter("token", token)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
