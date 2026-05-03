package pt.uc.dei.proj5.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;


@Entity
@Table(name="users")
// named queries
public class UserEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // --- COLUNAS

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name="id", nullable = false, unique = true, updatable = false)
    private Long id;


    @Column(name="primeiroNome", nullable = false)
    private String primeiroNome;


    @Column(name="ultimoNome", nullable = false)
    private String ultimoNome;


    @Column(name="email", nullable = false)
    private String email;


    @Column(name="telefone", nullable = false)
    private String telefone;


    @Column(name="username", nullable = false)
    private String username;


    @Column(name="password", nullable = false)
    private String password;


    @Column(name="fotoUrl", nullable = false)
    private String fotoUrl;


    @Column(name="isAdmin", nullable = false, updatable = false)
    private boolean isAdmin;


    @Column(name = "isAtivo", nullable = false)
    private boolean isAtivo;


    @Column(name = "idioma", nullable = false)
    private String idioma = "pt";



    @OneToMany(mappedBy = "users")
    private List<TokenEntity> tokens;

    @OneToMany(mappedBy = "users")
    private List<ClienteEntity> clientes;

    @OneToMany(mappedBy = "users")
    private List<LeadEntity> leads;




    // --- MÉTODOS


    // getters

    public Long getId() {
        return id;
    }

    public String getPrimeiroNome() {
        return primeiroNome;
    }

    public String getUltimoNome() {
        return ultimoNome;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public boolean isAtivo() {
        return isAtivo;
    }

    public String getIdioma() {
        return idioma;
    }

    @CreationTimestamp
    @Column(name = "dataCriacao", nullable = false, updatable = false)
    private LocalDate dataCriacao;


    public List<TokenEntity> getTokens() {
        return tokens;
    }



    // setters

    public void setPrimeiroNome(String primeiroNome) {
        this.primeiroNome = primeiroNome;
    }

    public void setUltimoNome(String ultimoNome) {
        this.ultimoNome = ultimoNome;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public void setIsAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    public void setIsAtivo(boolean ativo) {
        this.isAtivo = ativo;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public LocalDate getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDate dataCriacao) { this.dataCriacao = dataCriacao; }

    public void setTokens(List<TokenEntity> tokens) {
        this.tokens = tokens;
    }
}
