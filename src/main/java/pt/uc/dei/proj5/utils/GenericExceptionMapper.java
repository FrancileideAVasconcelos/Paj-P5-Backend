package pt.uc.dei.proj5.utils;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pt.uc.dei.proj5.beans.UserBean;
import pt.uc.dei.proj5.entity.UserEntity;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger logger = LogManager.getLogger(GenericExceptionMapper.class);

    // 1. Injeta os cabeçalhos do pedido atual
    @Context
    private HttpHeaders headers;

    // 2. Injeta o Bean para descodificar o token
    @Inject
    private UserBean userBean;

    @Override
    public Response toResponse(Exception exception) {
        String username = "Desconhecido/Sistema";

        try {
            // 3. Tenta descobrir quem causou o erro extraindo o token do cabeçalho
            String token = headers.getHeaderString("token");
            if (token != null && !token.isEmpty()) {
                UserEntity user = userBean.getUser(token);
                if (user != null) {
                    username = user.getUsername();
                }
            }
        } catch (Exception ignored) {
            // Ignora falhas ao tentar ler o utilizador para não quebrar o tratamento de erro principal
        }

        // 4. GRAVA O LOG DE AUDITORIA DO ERRO
        logger.warn("Utilizador: {} | Tentativa falhada: {}", username, exception.getMessage());

        // Apanha erros gerais do sistema e duplicações[cite: 15]
        return Response.status(Response.Status.CONFLICT)
                .entity(exception.getMessage())
                .build();
    }
}