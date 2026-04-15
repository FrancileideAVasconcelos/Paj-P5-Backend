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

    @Inject
    EmailBean emailBean;


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

    public void forgotPassword(String email) throws Exception {
        UserEntity user = userDao.getUserByEmail(email); // Ou o método que uses para buscar por e-mail
        if (user == null || !user.isAtivo()) {
            throw new Exception("Conta não encontrada ou inativa.");
        }

        // 1. Gera o Token (podes usar a mesma entidade VerificationToken com um tipo diferente)
        String tokenRecuperacao = TokenBean.generateToken();
        pt.uc.dei.proj5.entity.VerificationToken vToken = new pt.uc.dei.proj5.entity.VerificationToken();
        vToken.setToken(tokenRecuperacao);
        vToken.setUser(user);
        vToken.setTipo("PASSWORD_RESET"); // Tipo para distinguir do Registo
        vToken.setExpiryDate(java.time.LocalDateTime.now().plusMinutes(30)); // Expira em 30 minutos

        verificationTokenDao.persist(vToken);

        // 2. Cria o texto do e-mail
        String subject = "Recuperação de Password - Plataforma CRM";
        String body = "Olá " + user.getPrimeiroNome() + ",\n\n" +
                "Recebemos um pedido para repor a sua password.\n\n" +
                "Por favor, clique no link abaixo para criar uma nova password:\n" +
                "http://localhost:5173/reset-password?token=" + tokenRecuperacao + "\n\n" +
                "Este link expira em 30 minutos. Se não fez este pedido, pode ignorar este e-mail.";

        // 3. Usa o MailHog para enviar! (Apaga o teu antigo System.out.println)
        emailBean.sendEmail(email, subject, body);
    }

    public String resetPassword(String token, String newPassword) {
        VerificationToken vToken = verificationTokenDao.findByToken(token);

        if (vToken == null || !vToken.getTipo().equals("PASSWORD_RESET")) return "Token inválido.";
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