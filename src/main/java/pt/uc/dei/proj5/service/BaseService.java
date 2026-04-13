// BaseService.java
package pt.uc.dei.proj5.service;

import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import pt.uc.dei.proj5.beans.UserBean;
import pt.uc.dei.proj5.entity.UserEntity;
import pt.uc.dei.proj5.utils.AppConstants;

public abstract class BaseService {

    // Injetamos o UserBean aqui para que todos os serviços filhos o possam usar
    @Inject
    protected UserBean userBean;

    // O método centralizado de validação
    protected UserEntity validarAcesso(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new NotAuthorizedException(AppConstants.ERRO_TOKEN_INVALIDO); // 401
        }

        UserEntity user = userBean.getUser(token);

        if (user == null) {
            throw new NotAuthorizedException(AppConstants.ERRO_TOKEN_INVALIDO); // 401
        }

        return user;
    }
    // 2. Validação Restrita (Apenas Administradores)
    protected UserEntity validarAdmin(String token) {
        // Primeiro, verifica se o token é válido chamando o método de cima
        UserEntity user = validarAcesso(token);

        // Depois, verifica se é admin (depende de como está na tua entidade UserEntity)
        // Pode ser user.isAdmin() ou user.getRole().equals("ADMIN")
        if (!user.isAdmin()) {
            // O nosso SecurityExceptionMapper vai apanhar isto e devolver erro 403 Forbidden
            throw new SecurityException("Acesso restrito a administradores.");
        }

        return user;
    }

}
