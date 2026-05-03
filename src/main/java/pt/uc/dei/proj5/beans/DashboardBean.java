package pt.uc.dei.proj5.beans;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import pt.uc.dei.proj5.dao.ClienteDao;
import pt.uc.dei.proj5.dao.LeadDao;
import pt.uc.dei.proj5.dao.UserDao;
import pt.uc.dei.proj5.dto.DashboardDto;
import pt.uc.dei.proj5.entity.UserEntity;

import java.time.format.DateTimeFormatter;
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        // 1. Estatísticas Base (Usando os COUNTs ultra rápidos)
        dto.setTotalLeads(leadDao.countAllLeads(user));
        dto.setTotalClients(user.isAdmin() ? clienteDao.countAllClients() : clienteDao.countAllActiveByUser(user));

        // 2. Gráfico Circular: Leads por Estado
        List<Object[]> porEstado = leadDao.countLeadsByEstado(user);
        Map<Integer, Long> estadoMap = new java.util.HashMap<>();
        for (Object[] row : porEstado) {
            estadoMap.put((Integer) row[0], (Long) row[1]);
        }
        dto.setLeadsPorEstado(estadoMap);

        // 3. Evolução Temporal de Leads (Agrupa apenas as datas)
        List<java.time.LocalDate> leadDates = leadDao.findAllLeadDates(user);
        Map<String, Long> evolucaoL = leadDates.stream()
                .collect(Collectors.groupingBy(d -> d.format(formatter), TreeMap::new, Collectors.counting()));
        dto.setEvolucaoLeads(evolucaoL);

        // 4. ESTATÍSTICAS EXCLUSIVAS DE ADMIN
        if (user.isAdmin()) {
            dto.setTotalUsers(userDao.countTotalUsers());
            dto.setContasConfirmadas(userDao.countActiveUsers());

            // TOP 5 Utilizadores (O DAO já faz o limite e a ordenação)
            List<Object[]> topUsers = leadDao.countTop5LeadsByUser();
            Map<String, Long> top5Map = new java.util.LinkedHashMap<>(); // LinkedHashMap mantém a ordem!
            for (Object[] row : topUsers) {
                top5Map.put((String) row[0], (Long) row[1]);
            }
            dto.setLeadsPorUtilizador(top5Map);

            // Evolução Temporal de Utilizadores
            List<java.time.LocalDate> userDates = userDao.findAllUserCreationDates();
            Map<String, Long> evolucaoU = userDates.stream()
                    .collect(Collectors.groupingBy(d -> d.format(formatter), TreeMap::new, Collectors.counting()));
            dto.setEvolucaoUtilizadores(evolucaoU);
        }

        return dto;
    }
}