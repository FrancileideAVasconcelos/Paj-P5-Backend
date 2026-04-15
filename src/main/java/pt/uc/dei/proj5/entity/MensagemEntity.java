package pt.uc.dei.proj5.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name="mensagem")
public class MensagemEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "remetente_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity remetente;

    @ManyToOne
    @JoinColumn(name = "destinatario_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity destinatario;

    @Column(nullable = false, length = 1000)
    private String conteudo;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataEnvio;

    @Column(nullable = false)
    private boolean lida = false; // Começa sempre como não lida (false)

    // --- GETTERS E SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserEntity getRemetente() { return remetente; }
    public void setRemetente(UserEntity remetente) { this.remetente = remetente; }

    public UserEntity getDestinatario() { return destinatario; }
    public void setDestinatario(UserEntity destinatario) { this.destinatario = destinatario; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }

    public boolean isLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }
}