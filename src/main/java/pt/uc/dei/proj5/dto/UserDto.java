package pt.uc.dei.proj5.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserDto {
    private Long id;

    @NotBlank(message = "O username é obrigatório")
    @Size(min = 3, max = 100, message = "O username deve ter entre 3 e 20 caracteres")
    private String username;

    @NotBlank(message = "A password é obrigatório")
    @Size(min = 3, max = 100, message = "A password deve ter entre 3 e 100 caracteres")
    private String password;

    @NotBlank(message = "O Primeiro nome é obrigatório")
    @Size(min = 3, max = 100, message = "O primeiro nome deve ter entre 3 e 100 caracteres")
    private String primeiroNome;

    @NotBlank(message = "O último nome é obrigatório")
    @Size(min = 3, max = 100, message = "O último nome deve ter entre 3 e 100 caracteres")
    private String ultimoNome;

    @Email(message = "Formato de email inválido")
    private String email;

    private String fotoUrl;

    @Pattern(regexp = "^(\\+)?\\d{9,13}$", message = "Telefone deve ter entre 9 a 13 dígitos")
    private String telefone;

    private boolean admin;
    private boolean ativo;

    @XmlElement
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    @XmlElement
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    @XmlElement
    public String getTelefone() {
        return telefone;
    }
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    @XmlElement
    public String getFotoUrl() {
        return fotoUrl;
    }
    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    @XmlElement
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @XmlElement
    public String getPrimeiroNome() {
        return primeiroNome;
    }
    public void setPrimeiroNome(String primeiroNome) {
        this.primeiroNome = primeiroNome;
    }

    @XmlElement
    public String getUltimoNome() {
        return ultimoNome;
    }
    public void setUltimoNome(String ultimoNome) {
        this.ultimoNome = ultimoNome;
    }

    @XmlElement
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    @XmlElement
    public boolean isAdmin() {
        return admin;
    }
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @XmlElement
    public boolean isAtivo() {
        return ativo;
    }
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
