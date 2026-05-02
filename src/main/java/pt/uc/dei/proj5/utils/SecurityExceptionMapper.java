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
public class SecurityExceptionMapper implements ExceptionMapper<SecurityException> {

    private static final Logger logger = LogManager.getLogger(SecurityExceptionMapper.class);

    // 1. Injeta os cabeçalhos do pedido atual para podermos ler o token
    @Context
    private HttpHeaders headers;

    // 2. Injeta o Bean para ir à base de dados procurar o dono do token
    @Inject
    private UserBean userBean;

    @Override
    public Response toResponse(SecurityException exception) {
        String username = "Desconhecido/Sistema";

        try {
            // 3. Tenta descobrir quem tentou violar as regras de acesso
            String token = headers.getHeaderString("token");
            if (token != null && !token.isEmpty()) {
                UserEntity user = userBean.getUser(token);
                if (user != null) {
                    username = user.getUsername();
                }
            }
        } catch (Exception ignored) {
            // Se falhar a leitura do utilizador (ex: token inválido/expirado), ignoramos
            // para garantir que o erro 403 Forbidden é sempre devolvido!
        }

        logger.warn("Utilizador: {} | Bloqueio de Acesso: {}", username, exception.getMessage());

        // 5. Devolve o Erro 403 ao Frontend
        return Response.status(Response.Status.FORBIDDEN)
                .entity(exception.getMessage())
                .build();
    }
}