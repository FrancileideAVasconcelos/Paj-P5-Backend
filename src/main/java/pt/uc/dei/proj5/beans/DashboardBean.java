package pt.uc.dei.proj5.beans;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import pt.uc.dei.proj5.dao.ClienteDao;
import pt.uc.dei.proj5.dao.LeadDao;
import pt.uc.dei.proj5.dao.UserDao;
import pt.uc.dei.proj5.dto.DashboardDto;
import pt.uc.dei.proj5.dto.LeadDto;
import pt.uc.dei.proj5.entity.ClienteEntity;
import pt.uc.dei.proj5.entity.LeadEntity;
import pt.uc.dei.proj5.entity.UserEntity;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Stateless
public class DashboardBean {

    @Inject LeadDao leadDao;
    @Inject ClienteDao clienteDao;
    @Inject UserDao userDao;
    @Inject LeadBean leadBean;

    public DashboardDto getStats(UserEntity user) {
        DashboardDto dto = new DashboardDto();
        // Formata a data para yyyy-MM-dd para ficar fácil de ler no React
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        if (user.isAdmin()) {
            // ==========================================
            // VISÃO DE ADMINISTRADOR (Global)
            // ==========================================
            List<LeadEntity> allLeads = leadDao.findAllLeads();
            long totalClients = clienteDao.countAllClients();
            List<UserEntity> allUsers = userDao.findAllUsers();

            // 1. Estatísticas Totais
            dto.setTotalLeads(allLeads != null ? allLeads.size() : 0);
            dto.setTotalClients(totalClients);
            dto.setTotalUsers(allUsers != null ? allUsers.size() : 0);

            // 2. Contas Confirmadas (Ativas)
            long ativas = allUsers == null ? 0 : allUsers.stream().filter(UserEntity::isAtivo).count();
            dto.setContasConfirmadas(ativas);

            // 3. Processar Leads
            if (allLeads != null) {
                List<LeadDto> leadsDto = new ArrayList<>();
                for (LeadEntity l : allLeads) leadsDto.add(leadBean.converterParaDto(l));
                dto.setLeads(leadsDto); // Para o Gráfico Circular de Estados

                // Gráfico: Leads por Utilizador (APENAS O TOP 5 - Filtrado e Ordenado no Backend!)
                Map<String, Long> top5LeadsPorUser = allLeads.stream()
                        .filter(l -> l.getUser() != null)
                        .collect(Collectors.groupingBy(l -> l.getUser().getUsername(), Collectors.counting()))
                        .entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed()) // Ordena do maior para o menor
                        .limit(5) // Corta logo aqui no servidor!
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (e1, e2) -> e1,
                                java.util.LinkedHashMap::new // O LinkedHashMap é obrigatório para o Java não baralhar a ordem ao gerar o JSON
                        ));
                dto.setLeadsPorUtilizador(top5LeadsPorUser);

                // Gráfico: Evolução Temporal de Leads
                Map<String, Long> evolucaoL = allLeads.stream()
                        .filter(l -> l.getDataCriacao() != null)
                        .collect(Collectors.groupingBy(
                                l -> l.getDataCriacao().format(formatter),
                                TreeMap::new, // TreeMap garante que as datas vêm ordenadas
                                Collectors.counting()
                        ));
                dto.setEvolucaoLeads(evolucaoL);
            }

            // 4. Processar Utilizadores (Evolução Temporal)
            if (allUsers != null) {
                Map<String, Long> evolucaoU = allUsers.stream()
                        .filter(u -> u.getDataCriacao() != null)
                        .collect(Collectors.groupingBy(
                                u -> u.getDataCriacao().format(formatter),
                                TreeMap::new,
                                Collectors.counting()
                        ));
                dto.setEvolucaoUtilizadores(evolucaoU);
            }

        } else {
            // ==========================================
            // VISÃO DE UTILIZADOR COMUM
            // ==========================================
            List<LeadEntity> myLeads = leadDao.findAllByUser(user);
            long totalClients = clienteDao.countAllActiveByUser(user);
            dto.setTotalLeads(myLeads != null ? myLeads.size() : 0);
            dto.setTotalClients(totalClients);

            if (myLeads != null) {
                List<LeadDto> leadsDto = new ArrayList<>();
                for (LeadEntity l : myLeads) leadsDto.add(leadBean.converterParaDto(l));
                dto.setLeads(leadsDto);

                // Gráfico: Evolução Temporal das suas próprias Leads
                Map<String, Long> evolucaoL = myLeads.stream()
                        .filter(l -> l.getDataCriacao() != null)
                        .collect(Collectors.groupingBy(
                                l -> l.getDataCriacao().format(formatter),
                                TreeMap::new,
                                Collectors.counting()
                        ));
                dto.setEvolucaoLeads(evolucaoL);
            }
        }

        return dto;
    }
}