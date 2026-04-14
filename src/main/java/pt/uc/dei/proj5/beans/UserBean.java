package pt.uc.dei.proj5.beans;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import pt.uc.dei.proj5.dao.TokenDao;
import pt.uc.dei.proj5.dao.UserDao;
import pt.uc.dei.proj5.dao.VerificationTokenDao;
import pt.uc.dei.proj5.dto.UserDto;
import pt.uc.dei.proj5.entity.UserEntity;
import pt.uc.dei.proj5.entity.VerificationToken;

import java.io.Serializable;

// Passamos a @Stateless (EJB) para gerir as transações automaticamente com a Base de Dados
@Stateless
public class UserBean implements Serializable {

    @Inject
    UserDao userDao;

    @Inject
    TokenDao tokenDao;

    @Inject
    VerificationTokenDao verificationTokenDao;


    public String loginToken(String username, String password) {

        UserEntity u = userDao.getLogin(username, password);

        if (u == null) return null;

        // 1. Gera o token limpo (para devolver no REST)
        String tokenLimpo = TokenBean.generateToken();

        tokenDao.guardarTokenDB(tokenLimpo, u);

        return tokenLimpo; // Retorna a versão não encriptada para o Frontend/Postman
    }

    // Adiciona este método:
    public String completeRegistration(String token, UserDto dadosFinais) {
        pt.uc.dei.proj5.entity.VerificationToken vToken = verificationTokenDao.findByToken(token);

        if (vToken == null || !vToken.getTipo().equals("REGISTRATION")) return "Token inválido ou não encontrado.";
        if (vToken.getExpiryDate().isBefore(java.time.LocalDateTime.now())) return "O link de convite expirou.";

        // Verifica se o username escolhido já está a ser usado por outra pessoa
        if (userDao.checkUsername(dadosFinais.getUsername()) != null) {
            return "O username escolhido já está em uso.";
        }

        UserEntity user = vToken.getUser();

        // Substitui os dados fantasma pelos dados reais enviados do frontend
        user.setUsername(dadosFinais.getUsername());
        user.setPassword(dadosFinais.getPassword());
        user.setPrimeiroNome(dadosFinais.getPrimeiroNome());
        user.setUltimoNome(dadosFinais.getUltimoNome());
        user.setTelefone(dadosFinais.getTelefone());
        user.setFotoUrl(dadosFinais.getFotoUrl() != null ? dadosFinais.getFotoUrl() : "");
        user.setIsAtivo(true); // A conta fica finalmente ativa!

        userDao.merge(user);
        verificationTokenDao.remove(vToken); // Apaga o token para não ser reutilizado

        return "Registo concluído com sucesso!";
    }

    public boolean register(UserDto newUser) {

        if (userDao.checkUsername(newUser.getUsername()) != null) {
            return false;
        }

        userDao.novoUserDB(newUser); // Guarda na BD
        // Vai buscar a entidade acabada de criar
        UserEntity guardado = userDao.checkUsername(newUser.getUsername());

        // Gera token temporário
        String tokenConfirmacao = TokenBean.generateToken();

        VerificationToken vToken = new VerificationToken();
        vToken.setToken(tokenConfirmacao);
        vToken.setUser(guardado);
        vToken.setTipo("CONFIRMATION");
        vToken.setExpiryDate(java.time.LocalDateTime.now().plusHours(24)); // Expira em 24h

        verificationTokenDao.persist(vToken);

        // SIMULAÇÃO DO ENVIO DE E-MAIL NO CONSOLA DO SERVIDOR
        System.out.println("=====================================================");
        System.out.println("E-MAIL DE CONFIRMAÇÃO PARA: " + guardado.getEmail());
        System.out.println("Link: http://localhost:5173/confirm-account?token=" + tokenConfirmacao);
        System.out.println("=====================================================");

        return true;
    }

    public String confirmAccount(String token) {
        VerificationToken vToken = verificationTokenDao.findByToken(token);

        if (vToken == null || !vToken.getTipo().equals("CONFIRMATION")) return "Token inválido ou não encontrado.";
        if (vToken.getExpiryDate().isBefore(java.time.LocalDateTime.now())) return "O link expirou.";

        UserEntity user = vToken.getUser();
        user.setIsAtivo(true);
        userDao.merge(user);

        verificationTokenDao.remove(vToken); // Invalida o token após uso
        return "Conta ativada com sucesso!";
    }

    public boolean forgotPassword(String email) {
        UserEntity user = userDao.getUserByEmail(email);
        if (user == null || !user.isAtivo()) return false;

        String resetToken = TokenBean.generateToken();
        VerificationToken vToken = new VerificationToken();
        vToken.setToken(resetToken);
        vToken.setUser(user);
        vToken.setTipo("RESET");
        vToken.setExpiryDate(java.time.LocalDateTime.now().plusHours(1)); // Expira em 1 hora
        verificationTokenDao.persist(vToken);

        System.out.println("=====================================================");
        System.out.println("E-MAIL DE RECUPERAÇÃO DE PASSWORD PARA: " + email);
        System.out.println("Link: http://localhost:5173/reset-password?token=" + resetToken);
        System.out.println("=====================================================");

        return true;
    }

    public String resetPassword(String token, String newPassword) {
        VerificationToken vToken = verificationTokenDao.findByToken(token);

        if (vToken == null || !vToken.getTipo().equals("RESET")) return "Token inválido.";
        if (vToken.getExpiryDate().isBefore(java.time.LocalDateTime.now())) return "O link expirou.";

        UserEntity user = vToken.getUser();
        user.setPassword(newPassword);
        userDao.merge(user);

        verificationTokenDao.remove(vToken); // Invalida após uso
        return "Password redefinida com sucesso!";
    }

    public void logout(String token) {
        tokenDao.setExpired(token);
    }

    public UserEntity getUser(String token) {
        return tokenDao.getUserByToken(token);
    }

    public void updateUser(UserEntity user, UserDto novosDados) {

        userDao.updateUserDB(user, novosDados);
    }

    public UserDto getUserByToken(String token) {
        UserEntity entity = tokenDao.getUserByToken(token);
        if (entity == null) return null;
        return converterParaDto(entity);
    }

    public boolean verificaPassword(UserEntity user, String password) {
        return user.getPassword().equals(password);
    }

    // Função auxiliar para mapear de Entity (BD) para DTO (Frontend)
    public UserDto converterParaDto(UserEntity e) {
        UserDto dto = new UserDto();
        dto.setId(e.getId());
        dto.setPrimeiroNome(e.getPrimeiroNome());
        dto.setUltimoNome(e.getUltimoNome());
        dto.setEmail(e.getEmail());
        dto.setTelefone(e.getTelefone());
        dto.setUsername(e.getUsername());
        dto.setPassword(e.getPassword());
        dto.setFotoUrl(e.getFotoUrl());
        dto.setAdmin(e.isAdmin());
        dto.setAtivo(e.isAtivo());

        return dto;
    }
}