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

    // Novo método para filtrar diretamente na base de dados
    public List<LeadEntity> findFilteredLeads(UserEntity user, Integer estado) {

        StringBuilder queryStr = new StringBuilder("SELECT l FROM LeadEntity l WHERE l.users = :user AND l.isAtivo = true");

        // Se o frontend enviar um estado, adicionamos a clausula WHERE
        if (estado != null) {
            queryStr.append(" AND l.estado = :estado");
        }

        var query = em.createQuery(queryStr.toString(), LeadEntity.class);
        query.setParameter("user", user);

        if (estado != null) {
            query.setParameter("estado", estado);
        }

        return query.getResultList();
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

    public List<LeadEntity> findAllByUserForAdmin(UserEntity user) {
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