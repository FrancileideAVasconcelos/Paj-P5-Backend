package pt.uc.dei.proj5.dao;


import jakarta.ejb.Stateless;
import pt.uc.dei.proj5.dto.LeadDto;
import pt.uc.dei.proj5.entity.LeadEntity;
import pt.uc.dei.proj5.entity.UserEntity;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;


@Stateless
public class LeadDao extends DefaultDao<LeadEntity> implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;


    public LeadDao() {
        super(LeadEntity.class);
    }

    public void guardaLead(LeadDto lead, UserEntity u) {

        LeadEntity entity = new LeadEntity();

        entity.setTitulo(lead.getTitulo());
        entity.setDescricao(lead.getDescricao());
        entity.setEstado(lead.getEstado());
        entity.setUser(u);
        entity.setIsAtivo(true);

        persist(entity);
    }

    public void updateLead(LeadEntity leadAtual, LeadDto dtoNovo) {

        // Atualiza a entidade existente com os dados que vieram do DTO
        leadAtual.setTitulo(dtoNovo.getTitulo());
        leadAtual.setDescricao(dtoNovo.getDescricao());
        leadAtual.setEstado(dtoNovo.getEstado());
        // Usa o 'merge' para guardar as alterações na base de dados
        merge(leadAtual);
    }

    public LeadEntity findLeadById(Long id) {
        return em.find(LeadEntity.class, id);
    }

    public int softDeleteLead(Long id) {

        return em.createQuery("UPDATE LeadEntity l SET l.isAtivo = false WHERE l.id = :id")
                .setParameter("id", id)
                .executeUpdate();

    }

    // ========= ADMIN =========//

    // Conta os totais rapidamente
    public long countAllLeads(UserEntity user) {
        if (user.isAdmin()) {
            return em.createQuery("SELECT COUNT(l) FROM LeadEntity l", Long.class).getSingleResult();
        } else {
            return em.createQuery("SELECT COUNT(l) FROM LeadEntity l WHERE l.users = :user", Long.class)
                    .setParameter("user", user).getSingleResult();
        }
    }

    // Agrupa as leads por estado diretamente na BD
    public List<Object[]> countLeadsByEstado(UserEntity user) {
        if (user.isAdmin()) {
            return em.createQuery("SELECT l.estado, COUNT(l) FROM LeadEntity l GROUP BY l.estado", Object[].class).getResultList();
        } else {
            return em.createQuery("SELECT l.estado, COUNT(l) FROM LeadEntity l WHERE l.users = :user GROUP BY l.estado", Object[].class)
                    .setParameter("user", user).getResultList();
        }
    }

    public List<Object[]> countTop5LeadsByUser() {
        return em.createQuery("SELECT l.users.username, COUNT(l) as c FROM LeadEntity l WHERE l.users IS NOT NULL GROUP BY l.users.username ORDER BY c DESC", Object[].class)
                .setMaxResults(5)
                .getResultList();
    }

    // Traz APENAS as datas para fazer o gráfico de evolução (MUITO mais leve que trazer a Lead toda)
    public List<java.time.LocalDate> findAllLeadDates(UserEntity user) {
        if (user.isAdmin()) {
            return em.createQuery("SELECT l.dataCriacao FROM LeadEntity l WHERE l.dataCriacao IS NOT NULL", java.time.LocalDate.class).getResultList();
        } else {
            return em.createQuery("SELECT l.dataCriacao FROM LeadEntity l WHERE l.users = :user AND l.dataCriacao IS NOT NULL", java.time.LocalDate.class)
                    .setParameter("user", user).getResultList();
        }
    }

    public List<LeadEntity> findFilteredLeadsPaginated(UserEntity user, Integer estado, String search, int page, int pageSize) {
        boolean isAdmin = user.isAdmin();
        StringBuilder queryStr = new StringBuilder(isAdmin ? "SELECT l FROM LeadEntity l WHERE 1=1"
                : "SELECT l FROM LeadEntity l WHERE l.users = :user AND l.isAtivo = true");
        if (estado != null) queryStr.append(" AND l.estado = :estado");
        if (search != null && !search.trim().isEmpty()) {
            queryStr.append(isAdmin ? " AND (LOWER(l.titulo) LIKE :search OR LOWER(l.users.username) LIKE :search OR LOWER(l.users.primeiroNome) LIKE :search)"
                    : " AND LOWER(l.titulo) LIKE :search");
        }
        queryStr.append(" ORDER BY l.titulo ASC");
        var query = em.createQuery(queryStr.toString(), LeadEntity.class);
        if (!isAdmin) query.setParameter("user", user);
        if (estado != null) query.setParameter("estado", estado);
        if (search != null && !search.trim().isEmpty()) query.setParameter("search", "%" + search.toLowerCase() + "%");

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);
        return query.getResultList();
    }

    public long countFilteredLeads(UserEntity user, Integer estado, String search) {
        boolean isAdmin = user.isAdmin();
        StringBuilder queryStr = new StringBuilder(isAdmin ? "SELECT COUNT(l) FROM LeadEntity l WHERE 1=1"
                : "SELECT COUNT(l) FROM LeadEntity l WHERE l.users = :user AND l.isAtivo = true");
        if (estado != null) queryStr.append(" AND l.estado = :estado");
        if (search != null && !search.trim().isEmpty()) {
            queryStr.append(isAdmin ? " AND (LOWER(l.titulo) LIKE :search OR LOWER(l.users.username) LIKE :search OR LOWER(l.users.primeiroNome) LIKE :search)"
                    : " AND LOWER(l.titulo) LIKE :search");
        }
        var query = em.createQuery(queryStr.toString(), Long.class);
        if (!isAdmin) query.setParameter("user", user);
        if (estado != null) query.setParameter("estado", estado);
        if (search != null && !search.trim().isEmpty()) query.setParameter("search", "%" + search.toLowerCase() + "%");

        return query.getSingleResult();
    }

    // Este método serve tanto para o Admin "espreitar" um user,
    // como para o próprio user ver o seu dashboard.
    public List<LeadEntity> findAllByUser(UserEntity user) {
        return em.createQuery("SELECT l FROM LeadEntity l WHERE l.users = :user", LeadEntity.class)
                .setParameter("user", user)
                .getResultList();
    }

    public void reativarTodasAsLeads(UserEntity user) {
        em.createQuery("UPDATE LeadEntity l SET l.isAtivo = true WHERE l.users = :user")
                .setParameter("user", user)
                .executeUpdate();
    }

    public void inativarTodasAsLeads(UserEntity user) {
        em.createQuery("UPDATE LeadEntity l SET l.isAtivo = false WHERE l.users = :user")
                .setParameter("user", user)
                .executeUpdate();
    }

    public void apagarTodasAsLeadsPermanente(UserEntity user) {
        em.createQuery("DELETE FROM LeadEntity l WHERE l.users = :user")
                .setParameter("user", user)
                .executeUpdate();
    }

    // 4. Apagar/Inativar uma Única Lead (Para o Admin)
    public void apagarLeadAdmin(LeadEntity lead, boolean permanente) {
        if (permanente) {
            remove(lead);
        } else {
            lead.setIsAtivo(false);
            merge(lead);
        }
    }


}