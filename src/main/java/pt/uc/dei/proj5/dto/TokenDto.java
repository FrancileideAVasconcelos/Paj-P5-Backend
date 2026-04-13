package pt.uc.dei.proj5.dto;

import jakarta.xml.bind.annotation.XmlElement;

import java.time.LocalDateTime;

public class TokenDto {

    private long userId;
    private String token;
    private LocalDateTime dataSessao;
    private LocalDateTime expireTime;


    public TokenDto() {}

    public TokenDto(long userId, String token) {
        this.userId = userId;
        this.token = token;
        this.dataSessao = LocalDateTime.now();
        this.expireTime = dataSessao.plusHours(1);
    }

    @XmlElement
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @XmlElement
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @XmlElement
    public LocalDateTime getDataSessao() {
        return dataSessao;
    }

    public void setDataSessao(LocalDateTime dataSessao) {
        this.dataSessao = dataSessao;
    }

    @XmlElement
    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public boolean isValido() {
        return LocalDateTime.now().isBefore(expireTime);
    }

}
