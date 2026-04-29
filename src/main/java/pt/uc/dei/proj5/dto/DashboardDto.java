package pt.uc.dei.proj5.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class DashboardDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private long totalLeads;
    private long totalClients;
    private List<LeadDto> leads;
    private long totalUsers;
    private long contasConfirmadas;
    private java.util.Map<String, Long> leadsPorUtilizador;
    private java.util.Map<String, Long> evolucaoUtilizadores;
    private java.util.Map<String, Long> evolucaoLeads;

    // Construtor vazio
    public DashboardDto() {}

    // Getters e Setters
    public long getTotalLeads() {
        return totalLeads;
    }

    public void setTotalLeads(long totalLeads) {
        this.totalLeads = totalLeads;
    }

    public long getTotalClients() {
        return totalClients;
    }

    public void setTotalClients(long totalClients) {
        this.totalClients = totalClients;
    }

    public List<LeadDto> getLeads() {
        return leads;
    }

    public void setLeads(List<LeadDto> leads) {
        this.leads = leads;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Map<String, Long> getEvolucaoLeads() {
        return evolucaoLeads;
    }

    public void setEvolucaoLeads(Map<String, Long> evolucaoLeads) {
        this.evolucaoLeads = evolucaoLeads;
    }

    public long getContasConfirmadas() {
        return contasConfirmadas;
    }

    public void setContasConfirmadas(long contasConfirmadas) {
        this.contasConfirmadas = contasConfirmadas;
    }

    public Map<String, Long> getLeadsPorUtilizador() {
        return leadsPorUtilizador;
    }

    public void setLeadsPorUtilizador(Map<String, Long> leadsPorUtilizador) {
        this.leadsPorUtilizador = leadsPorUtilizador;
    }

    public Map<String, Long> getEvolucaoUtilizadores() {
        return evolucaoUtilizadores;
    }

    public void setEvolucaoUtilizadores(Map<String, Long> evolucaoUtilizadores) {
        this.evolucaoUtilizadores = evolucaoUtilizadores;
    }
}