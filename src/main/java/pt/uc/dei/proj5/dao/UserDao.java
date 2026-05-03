package pt.uc.dei.proj5.dao;

import jakarta.ejb.Stateless;
import jakarta.persistence.NoResultException;
import pt.uc.dei.proj5.dto.UserDto;
import pt.uc.dei.proj5.entity.LeadEntity;
import pt.uc.dei.proj5.entity.UserEntity;
import java.io.Serializable;
import java.util.List;

@Stateless
public class    UserDao extends DefaultDao<UserEntity> implements Serializable {

    public UserDao() {
        super(UserEntity.class);
    }

    // Procura um utilizador pelo seu username
    public UserEntity getLogin(String username, String password) {
        try {
            return em.createQuery("SELECT u FROM UserEntity u WHERE u.username = :username AND u.password = :password", UserEntity.class)
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
    // 1. O método passa a receber a página e o tamanho para a paginação
    public List<UserEntity> findFilteredUsers(String search, int page, int pageSize) {
        String jpql = "SELECT u FROM UserEntity u";

        // Pesquisa
        if (search != null && !search.trim().isEmpty()) {
            jpql += " WHERE LOWER(u.username) LIKE :search OR LOWER(u.email) LIKE :search";
        }

        // Ordenação
        jpql += " ORDER BY u.primeiroNome ASC, u.ultimoNome ASC";

        var query = em.createQuery(jpql, UserEntity.class);

        if (search != null && !search.trim().isEmpty()) {
            query.setParameter("search", "%" + search.toLowerCase() + "%");
        }

        // Paginação
        int offset = (page - 1) * pageSize;
        query.setFirstResult(offset);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    // 2. Método para contar os totais (útil para o React)
    public long countFilteredUsers(String search) {
        String jpql = "SELECT COUNT(u) FROM UserEntity u";

        if (search != null && !search.trim().isEmpty()) {
            jpql += " WHERE LOWER(u.username) LIKE :search OR LOWER(u.email) LIKE :search";
        }

        var query = em.createQuery(jpql, Long.class);

        if (search != null && !search.trim().isEmpty()) {
            query.setParameter("search", "%" + search.toLowerCase() + "%");
        }

        return query.getSingleResult();
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
        user.setIdioma("pt");

        persist(user);
    }

    public void updateUserDB(UserEntity u, UserDto novosDados){

        u.setPrimeiroNome(novosDados.getPrimeiroNome());
        u.setUltimoNome(novosDados.getUltimoNome());
        u.setEmail(novosDados.getEmail());
        u.setTelefone(novosDados.getTelefone());
        u.setFotoUrl(novosDados.getFotoUrl());

        //Se o React enviar um idioma, atualiza. Se não, protege para não apagar.
        if (novosDados.getIdioma() != null) {
            u.setIdioma(novosDados.getIdioma());
        }

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
