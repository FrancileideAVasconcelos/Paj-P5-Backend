package pt.uc.dei.proj5.service;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.uc.dei.proj5.beans.UserBean;
import pt.uc.dei.proj5.dao.MensagemDao;
import pt.uc.dei.proj5.dao.UserDao;
import pt.uc.dei.proj5.dto.UserDto;
import pt.uc.dei.proj5.entity.UserEntity;
import pt.uc.dei.proj5.utils.AppConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserService extends BaseService {

    private static final Logger logger = LogManager.getLogger(UserService.class);

    @Inject
    UserBean userBean;

    @Inject
    UserDao userDao;

    @Inject
    MensagemDao mensagemDao;

    @POST
    @Path("/login")
    public Response login(UserDto user) {
        // Validação básica de campos vazios
        if (user.getUsername() == null || user.getPassword() == null) {
            logger.warn("Tentativa de login com dados incompletos.");
            return Response.status(401).entity("Dados incompletos!").build();
        }

        try {
            String token = userBean.loginToken(user.getUsername(), user.getPassword());

            if (token != null) {
                logger.info("Utilizador: {} | Ação: Fez login com sucesso no sistema.", user.getUsername());
                return Response.status(200).entity(Collections.singletonMap("token", token)).build();
            }

            // Se devolver null (Password ou Username errados)
            logger.warn("Utilizador: {} | Ação: Falhou a autenticação (Credenciais inválidas).", user.getUsername());
            return Response.status(401).entity(AppConstants.CREDENCIAIS_INVALIDAS).build();

        } catch (SecurityException e) {
            // A MAGIA DO ERRO 403 FICA AQUI!
            logger.warn("Utilizador: {} | Ação: Tentativa de login em conta inativa/não confirmada.", user.getUsername());
            return Response.status(403).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/logout")
    public Response logout(@HeaderParam("token") String token) {
        // Vamos buscar o utilizador antes de invalidar o token para podermos saber quem fez logout
        UserEntity user = validarAcesso(token);

        userBean.logout(token);

        logger.info("Utilizador: {} | Ação: Terminou a sessão (Logout).",
                user.getUsername());

        return Response.status(200).build();
    }

    @POST
    @Path("/complete-registration")
    public Response completeRegistration(@QueryParam("token") String token, UserDto dto) {
        if (token == null) {
            logger.warn("Tentativa de completar registo sem fornecer token.");
            return Response.status(400).entity("Token não fornecido.").build();
        }

        String resultado = userBean.completeRegistration(token, dto);

        if (resultado.equals("Registo concluído com sucesso!")) {
            logger.info("Utilizador: {} | Ação: Completou o registo da conta com sucesso.",
                    dto.getUsername());
            return Response.ok().entity(resultado).build();
        }

        logger.warn("Tentativa falhada ao completar registo: {}", resultado);
        return Response.status(400).entity(resultado).build();
    }

    @POST
    @Path("/forgot-password")
    public Response forgotPassword(UserDto userDto) throws Exception {
        if (userDto.getEmail() == null) {
            logger.warn("Pedido de recuperação de password rejeitado (Sem email).");
            return Response.status(400).entity("Email obrigatório").build();
        }

        userBean.forgotPassword(userDto.getEmail());

        logger.info("Ação: Pedido de recuperação de password processado para o email '{}'.",
                userDto.getEmail());

        return Response.ok().entity("Se o email existir, receberá as instruções.").build();
    }

    @POST
    @Path("/reset-password")
    public Response resetPassword(@QueryParam("token") String token, UserDto dto) {
        if (token == null || dto.getPassword() == null) {
            logger.warn("Tentativa de reset de password com dados incompletos.");
            return Response.status(400).entity("Dados incompletos.").build();
        }

        String resultado = userBean.resetPassword(token, dto.getPassword());

        if (resultado.equals("Password redefinida com sucesso!")) {
            logger.info("Ação: Password redefinida com sucesso através do token de recuperação.");
            return Response.ok().entity(resultado).build();
        }

        logger.warn("Tentativa falhada de reset de password: {}", resultado);
        return Response.status(400).entity(resultado).build();
    }

    @GET
    @Path("/profile")
    public Response getUserProfile(@HeaderParam("token") String token) {
        UserEntity userEntity = validarAcesso(token);
        UserDto user = userBean.converterParaDto(userEntity);

        return Response.status(Response.Status.OK).entity(user).build();
    }

    @PATCH
    @Path("/idioma")
    public Response updateIdioma(@HeaderParam("token") String token, UserDto dto) {
        UserEntity user = validarAcesso(token);

        if (dto.getIdioma() != null) {
            user.setIdioma(dto.getIdioma());
            userDao.merge(user); // Grava a nova preferência na BD
        }

        return Response.status(Response.Status.OK).build();
    }

    @PATCH
    @Path("/perfil")
    public Response updateProfile(@HeaderParam("token") String token, @Valid UserDto dadosNovos) {
        UserEntity user = validarAcesso(token);

        userBean.updateUser(user, dadosNovos);

        logger.info("Utilizador: {} | Ação: Atualizou os seus dados de perfil.",
                user.getUsername());

        return Response.status(Response.Status.OK).entity("Perfil atualizado com sucesso!").build();
    }

    @GET
    @Path("/checkPass")
    public Response verificaPassword(@HeaderParam("token") String token, @HeaderParam("passatual") String password) {
        UserEntity user = validarAcesso(token);

        boolean passwordCorreta = userBean.verificaPassword(user, password);

        if (!passwordCorreta) {
            logger.warn("Utilizador: {} | Ação: Falhou a confirmação da password atual durante a edição do perfil.",
                    user.getUsername());

            return Response.status(Response.Status.FORBIDDEN)
                    .entity("A password atual está incorreta.")
                    .build();
        }

        logger.info("Utilizador: {} | Ação: Confirmou a password atual com sucesso.",
                user.getUsername());

        return Response.status(Response.Status.OK)
                .entity("Password confirmada com sucesso.")
                .build();
    }

    @GET
    @Path("/ativos")
    public Response getActiveUsers(@HeaderParam("token") String token) {
        UserEntity eu = validarAcesso(token);
        List<UserEntity> ativos = userDao.getActiveUsersExcluindo(eu.getUsername());
        List<UserDto> dtos = new java.util.ArrayList<>();

        for (UserEntity u : ativos) {
            UserDto dto = new UserDto();
            dto.setUsername(u.getUsername());
            dto.setPrimeiroNome(u.getPrimeiroNome());
            dto.setUltimoNome(u.getUltimoNome());
            dto.setFotoUrl(u.getFotoUrl());

            long unread = mensagemDao.contarNaoLidasDe(u, eu);
            dto.setUnreadCount(unread);

            dtos.add(dto);
        }

        return Response.ok(dtos).build();
    }
}