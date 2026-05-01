package pt.uc.dei.proj5.beans;


import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import pt.uc.dei.proj5.dao.LeadDao;
import pt.uc.dei.proj5.dao.UserDao;
import pt.uc.dei.proj5.dto.LeadDto;
import pt.uc.dei.proj5.entity.LeadEntity;
import pt.uc.dei.proj5.entity.UserEntity;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;


@Stateless
public class LeadBean implements Serializable {


    @Inject
    UserDao userDao;

    @Inject
    LeadDao leadDao;

    @Inject
    UserBean userBean;


    public LeadDto converterParaDto(LeadEntity lead) {
        if (lead == null) return null;
        LeadDto dto = new LeadDto(
                lead.getId(),
                lead.getTitulo(),
                lead.getDescricao(),
                lead.getEstado(),
                lead.getDataCriacao(),
                userBean.converterParaDto(lead.getUser())
        );
        dto.setAtivo(lead.isAtivo());

        return dto;
    }

    // Criar Lead
    public LeadDto registarLead(LeadDto leadDto, UserEntity u) {

        leadDao.guardaLead(leadDto, u);

        return leadDto;
    }

    public LeadDto getLeadById(Long id, UserEntity requester) {

        LeadEntity lead = leadDao.findLeadById(id);

        if (lead == null || (!lead.isAtivo() && !requester.isAdmin())) {
            return null;
        }

        if (!lead.getUser().getUsername().equals(requester.getUsername()) && !requester.isAdmin()){
            throw new SecurityException("Acesso Negado: Esta Lead pertence a outro utilizador.");
        }

        return converterParaDto(lead);
    }

    // lista filtrada por estado
    public List<LeadDto> getFilteredLeads(UserEntity user, Integer estado){
        List<LeadEntity> leads;

        // A MAGIA PARA AS LEADS:
        if (user.isAdmin()) {
            leads = leadDao.findAllFilteredLeadsGlobal(estado);
        } else {
            leads = leadDao.findFilteredLeads(user, estado);
        }

        List<LeadDto> dtoLeads = new ArrayList<>();
        if (leads != null) {
            for (LeadEntity l : leads) {
                dtoLeads.add(converterParaDto(l));
            }
        }
        return dtoLeads;
    }

    // Editar Lead
    public void updateLead(Long id, LeadDto dto, String usernameRequester) throws Exception {
        // Extract data from DTO and pass to DAO
        LeadEntity leadAtual = leadDao.findLeadById(id);

        if (leadAtual == null || !leadAtual.isAtivo()) {
            throw new Exception("Lead não encontrada ou inativa.");
        }

        // Verifica se o username do dono na entidade coincide com quem está a pedir
        if (!leadAtual.getUser().getUsername().equals(usernameRequester)) {
            throw new SecurityException("Acesso Negado: Esta lead pertence a outro utilizador.");
        }

        leadDao.updateLead(leadAtual, dto);
    }

    // Apagar Lead
    public int softDeleteLead(Long id, String usernameRequester) {
        LeadEntity lead = leadDao.findLeadById(id);

        if (lead != null && !lead.getUser().getUsername().equals(usernameRequester)){
            throw new SecurityException("Não tem permissão para eliminar esta Lead.");
        }

        return leadDao.softDeleteLead(id);
    }

}