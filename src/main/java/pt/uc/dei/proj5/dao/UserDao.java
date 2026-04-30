package pt.uc.dei.proj5.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import pt.uc.dei.proj5.dto.UserDto;
import pt.uc.dei.proj5.entity.LeadEntity;
import pt.uc.dei.proj5.entity.UserEntity;
import java.io.Serializable;
import java.util.List;

@Stateless
public class UserDao extends DefaultDao<UserEntity> implements Serializable {

    public UserDao() {
        super(UserEntity.class);
    }

    // Procura um utilizador pelo seu username
    public UserEntity getLogin(String username, String password) {
        try {
            return em.createQuery("SELECT u FROM UserEntity u WHERE u.username = :username AND u.password = :password AND u.isAtivo", UserEntity.class)
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null; // Retorna null se não encontrar ninguém (útil para o login ou registo)
        }
    }


    public UserEntity checkUsername(String username) {
        try {
            return em.createQuery("SELECT u FROM UserEntity u WHERE u.username = :username", UserEntity.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<UserEntity> findAllUsers() {
        return em.createQuery("SELECT u FROM UserEntity u", UserEntity.class).getResultList();
    }

    // Vai buscar os utilizadores, permitindo pesquisa por username ou email e já ordenando
    public List<UserEntity> findFilteredUsers(String search) {
        String jpql = "SELECT u FROM UserEntity u";

        // Se houver um termo de pesquisa, adicionamos a cláusula WHERE
        if (search != null && !search.trim().isEmpty()) {
            jpql += " WHERE LOWER(u.username) LIKE :search OR LOWER(u.email) LIKE :search";
        }

        // O enunciado pede ordenação no backend, por isso ordenamos pelos nomes
        jpql += " ORDER BY u.primeiroNome ASC, u.ultimoNome ASC";

        var query = em.createQuery(jpql, UserEntity.class);

        if (search != null && !search.trim().isEmpty()) {
            query.setParameter("search", "%" + search.toLowerCase() + "%");
        }

        return query.getResultList();
    }


    public void novoUserDB(UserDto novoUser) {

        UserEntity user = new UserEntity();
        user.setPrimeiroNome(novoUser.getPrimeiroNome());
        user.setUltimoNome(novoUser.getUltimoNome());
        user.setTelefone(novoUser.getTelefone());
        user.setEmail(novoUser.getEmail());
        user.setFotoUrl(novoUser.getFotoUrl());
        user.setUsername(novoUser.getUsername());
        user.setPassword(novoUser.getPassword());
        user.setIsAtivo(false);

        persist(user);
    }

    public void updateUserDB(UserEntity u, UserDto novosDados){

        u.setPrimeiroNome(novosDados.getPrimeiroNome());
        u.setUltimoNome(novosDados.getUltimoNome());
        u.setEmail(novosDados.getEmail());
        u.setTelefone(novosDados.getTelefone());
        u.setFotoUrl(novosDados.getFotoUrl());

        // Faltava isto para garantir que as alterações vão para a Base de Dados!
        merge(u);
    }

    public UserEntity getUserByEmail(String email) {
        try {
            return em.createQuery("SELECT u FROM UserEntity u WHERE LOWER(u.email) = LOWER(:email)", UserEntity.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (jakarta.persistence.NoResultException e) {
            return null;
        }
    }

    // Vai buscar todos os utilizadores ativos, exceto a própria pessoa
    public List<UserEntity> getActiveUsersExcluindo(String meuUsername) {
        return em.createQuery(
                        "SELECT u FROM UserEntity u WHERE u.isAtivo = true AND u.username != :meuUsername", UserEntity.class)
                .setParameter("meuUsername", meuUsername)
                .getResultList();
    }
}
