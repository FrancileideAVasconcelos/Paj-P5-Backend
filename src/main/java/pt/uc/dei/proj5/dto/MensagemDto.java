package pt.uc.dei.proj5.dto;

import java.time.LocalDateTime;

public class MensagemDto {
    private String remetenteUsername;
    private String destinatarioUsername;
    private String conteudo;
    private LocalDateTime dataEnvio;
    private boolean lida;

    // Construtores
    public MensagemDto() {}

    public MensagemDto(String remetenteUsername, String destinatarioUsername, String conteudo, LocalDateTime dataEnvio, boolean lida) {
        this.remetenteUsername = remetenteUsername;
        this.destinatarioUsername = destinatarioUsername;
        this.conteudo = conteudo;
        this.dataEnvio = dataEnvio;
        this.lida = lida;
    }

    // --- GETTERS E SETTERS ---
    public String getRemetenteUsername() { return remetenteUsername; }
    public void setRemetenteUsername(String remetenteUsername) { this.remetenteUsername = remetenteUsername; }

    public String getDestinatarioUsername() { return destinatarioUsername; }
    public void setDestinatarioUsername(String destinatarioUsername) { this.destinatarioUsername = destinatarioUsername; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }

    public boolean isLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }
}