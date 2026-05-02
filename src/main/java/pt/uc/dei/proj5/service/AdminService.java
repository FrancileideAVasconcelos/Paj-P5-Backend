package pt.uc.dei.proj5.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.uc.dei.proj5.beans.AdminBean;
import pt.uc.dei.proj5.dto.LeadDto;
import pt.uc.dei.proj5.dto.ClientDto;
import pt.uc.dei.proj5.dto.UserDto;
import pt.uc.dei.proj5.entity.UserEntity;
import pt.uc.dei.proj5.utils.AppConstants;

import java.util.List;

@Path("/admin")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AdminService extends BaseService {

    @Inject
    AdminBean adminBean;

    private static final Logger logger = LogManager.getLogger(AdminBean.class);


    @POST
    @Path("/users/invite")
    public Response inviteUser(@HeaderParam("token") String token, UserDto dto) {
        UserEntity admin = validarAdmin(token);
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return Response.status(400).entity(AppConstants.EMAIL_OBRIGATORIO).build();
        }

        try {
            adminBean.inviteUser(dto.getEmail());

            logger.info("Utilizador: {} | Ação: Enviou convite de registo para o email: '{}'",
                    admin.getUsername(), dto.getEmail());

            return Response.status(200).entity(AppConstants.CONVITE_ENVIADO).build();
        } catch (Exception e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/users/{username}")
    public Response getProfileUser(@HeaderParam("token") String token,
                                   @PathParam("username") String usernameAlvo) throws Exception {

        validarAdmin(token); // Garante que quem pede é Admin e tem sessão válida

        UserDto alvo = adminBean.getProfileUser(usernameAlvo);

        return Response.status(Response.Status.OK).entity(alvo).build();
    }

    @GET
    @Path("/users")
    // Adicionamos o @QueryParam("search")
    public Response getAllUsers(@HeaderParam("token") String token, @QueryParam("search") String search) {
        validarAdmin(token);
        List<UserDto> users = adminBean.getAllUsers(search);
        return Response.status(Response.Status.OK).entity(users).build();
    }

    @PATCH
    @Path("/users/{username}")
    public Response editarUtilizadorAdmin(@HeaderParam("token") String token,
                                          @PathParam("username") String usernameAlvo,
                                          @Valid UserDto dto) {
        UserEntity admin = validarAdmin(token);
        adminBean.editarUtilizadorAdmin(usernameAlvo, dto);

        logger.info("Utilizador: {} | Ação: Editou dados do username: '{}'",
                admin.getUsername(), dto.getUsername());

        return Response.status(Response.Status.OK).entity(AppConstants.DADOS_ATUALIZADOS).build();
    }

    @PATCH
    @Path("/users/{username}/reactivate")
    public Response reactivateUser(@HeaderParam("token") String token,
                                   @PathParam("username") String usernameAlvo) {

        UserEntity admin = validarAdmin(token);

        adminBean.reactivateUser(usernameAlvo);

        logger.warn("Utilizador: {} | Ação: Reativou o username: '{}'",
                admin.getUsername(), usernameAlvo);

        return Response.status(Response.Status.OK).entity(AppConstants.UTILIZADOR_REATIVADO).build();
    }

    @DELETE
    @Path("/users/{username}")
    public Response deleteUser(@HeaderParam("token") String token,
                               @PathParam("username") String usernameAlvo,
                               @QueryParam("permanente") boolean permanente) {

        UserEntity admin = validarAdmin(token);

        if (permanente) {
            adminBean.hardDeleteUser(usernameAlvo);

            logger.warn("Utilizador: {} | Ação: Apagou o utilizador '{}' permanentemente.",
                    admin.getUsername(), usernameAlvo);

            // Mensagem corrigida para corresponder à ação "permanente"
            return Response.status(Response.Status.OK).entity("Utilizador apagado permanentemente.").build();
        } else {
            adminBean.softDeleteUser(usernameAlvo);

            logger.warn("Utilizador: {} | Ação: Inativou o utilizador '{}' permanentemente.",
                    admin.getUsername(), usernameAlvo);

            // Mensagem corrigida para corresponder à inativação
            return Response.status(Response.Status.OK).entity(AppConstants.UTILIZADOR_INATIVADO).build();
        }
    }

    // ========================================================
    // ROTAS DE GESTÃO DE CLIENTES (ADMIN)
    // ========================================================
    @GET
    @Path("/users/{username}/clients")
    public Response getClientesDeUser(@HeaderParam("token") String token,
                                      @PathParam("username") String usernameAlvo) {

        validarAdmin(token);
        return Response.status(Response.Status.OK).entity(adminBean.getClientFromUser(usernameAlvo)).build();
    }

    @PATCH
    @Path("/clients/{id}/reactivate")
    public Response reactivateClienteAdmin(@HeaderParam("token") String token,
                                           @PathParam("id") Long idCliente) {

        UserEntity admin = validarAdmin(token);

        adminBean.reactivateClienteAdmin(idCliente);

        logger.warn("Utilizador: {} | Ação: Reativou o cliente com o id: '{}'",
                admin.getUsername(), idCliente);

        return Response.status(Response.Status.OK).entity(AppConstants.CLIENTE_REATIVADO).build();
    }

    @PATCH
    @Path("/users/{username}/clients/reactivate")
    public Response reativarTodosClientesDeUser(@HeaderParam("token") String token,
                                                @PathParam("username") String usernameAlvo) {

        UserEntity admin = validarAdmin(token);
        adminBean.reativarTodosClientesDeUser(usernameAlvo);

        logger.info("Utilizador: {} | Ação: Reativou todos os clientes do username: '{}'",
                admin.getUsername(), usernameAlvo);

        return Response.status(Response.Status.OK).entity(AppConstants.TODOS_CLIENTES_REATIVADOS).build();
    }

    @DELETE
    @Path("/users/{username}/clients")
    public Response apagarTodosClientesDeUser(@HeaderParam("token") String token,
                                              @PathParam("username") String usernameAlvo,
                                              @QueryParam("permanente") boolean permanente) {

        UserEntity admin = validarAdmin(token);

        adminBean.apagarTodosClientesDeUser( usernameAlvo, permanente);
        String msg = permanente ? (AppConstants.TODOS_CLIENTES_EXCLUIDOS) : (AppConstants.TODOS_CLIENTES_INATIVADOS);

        if (permanente) {
            logger.warn("Utilizador: {} | Ação: Apagou TODOS os clientes de '{}' permanentemente.",
                    admin.getUsername(), usernameAlvo);
        } else {
            logger.info("Utilizador: {} | Ação: Inativou TODOS os clientes de '{}'.",
                    admin.getUsername(), usernameAlvo);
        }

        return Response.status(Response.Status.OK).entity(msg).build();

    }

    // Editar um cliente específico
    @PATCH
    @Path("/clients/{id}")
    public Response editarClienteAdmin(@HeaderParam("token") String token,
                                       @PathParam("id") Long idCliente,
                                       @Valid ClientDto dto) throws Exception {

        UserEntity admin = validarAdmin(token);

        adminBean.editarClienteAdmin(idCliente, dto);

        logger.info("Utilizador: {} | Ação: Editou o cliente com o id: '{}'",
                admin.getUsername(), idCliente);

        return Response.status(Response.Status.OK).entity(AppConstants.CLIENTE_ATUALIZADO_SUCESSO).build();

    }

    // Apagar um cliente específico
    @DELETE
    @Path("/clients/{id}")
    public Response apagarClienteAdmin(@HeaderParam("token") String token,
                                       @PathParam("id") Long idCliente,
                                       @QueryParam("permanente") boolean permanente) {

        UserEntity admin = validarAdmin(token);

        adminBean.apagarClienteAdmin(idCliente, permanente);
        String msg = permanente ? (AppConstants.CLIENTE_EXCLUIDO) : (AppConstants.CLIENTE_INATIVADO);

        if (permanente) {
            logger.warn("Utilizador: {} | Ação: Apagou o cliente com o id: '{}' permanentemente.",
                    admin.getUsername(), idCliente);
        } else {
            logger.info("Utilizador: {} | Ação: Inativou oclientecom o id: '{}'.",
                    admin.getUsername(), idCliente);
        }

        return Response.status(Response.Status.OK).entity(msg).build();

    }

    // ========================================================
    // ROTAS DE GESTÃO DE LEADS (ADMIN)
    // ========================================================

    @GET
    @Path("/users/{username}/leads")
    public Response getLeadsDeUser(@HeaderParam("token") String token,
                                   @PathParam("username") String usernameAlvo) {

        validarAdmin(token);
        return Response.status(Response.Status.OK).entity(adminBean.getLeadsFromUser(usernameAlvo)).build();
    }

    // Editar uma lead específica (Admin)
    @PATCH
    @Path("/leads/{id}")
    public Response editarLeadAdmin(@HeaderParam("token") String token,
                                    @PathParam("id") Long idLead,
                                    @Valid LeadDto dto) {

        UserEntity admin = validarAdmin(token);

        adminBean.editarLeadAdmin(idLead, dto);

        logger.info("Utilizador: {} | Ação: Editou a lead com o id: '{}'.",
                admin.getUsername(), idLead);

        return Response.status(Response.Status.OK).entity(AppConstants.LEAD_ATUALIZADA_SUCESSO).build();

    }

    @PATCH
    @Path("/leads/{id}/reactivate")
    public Response reactivateLeadAdmin(@HeaderParam("token") String token, @PathParam("id") Long idLead) {

        UserEntity admin = validarAdmin(token);

        adminBean.reactivateLeadsAdmin(idLead);

        logger.info("Utilizador: {} | Ação: Reativou a lead com o id: '{}'.",
                admin.getUsername(), idLead);

        return Response.status(Response.Status.OK).entity(AppConstants.LEAD_REATIVADA).build();

    }

    @PATCH
    @Path("/users/{username}/leads/reactivate")
    public Response reativarTodasLeadsDeUser(@HeaderParam("token") String token, @PathParam("username") String usernameAlvo) {

        UserEntity admin = validarAdmin(token);

        adminBean.reativarTodasLeadsDeUser(usernameAlvo);

        logger.info("Utilizador: {} | Ação: Reativou TODAS as leads do username: '{}'.",
                admin.getUsername(), usernameAlvo);

        return Response.status(Response.Status.OK).entity(AppConstants.TODAS_LEADS_REATIVADAS).build();
    }

    @DELETE
    @Path("/users/{username}/leads")
    public Response apagarTodasLeadsDeUser(@HeaderParam("token") String token,
                                           @PathParam("username") String usernameAlvo,
                                           @QueryParam("permanente") boolean permanente) {

        UserEntity admin = validarAdmin(token);
        adminBean.apagarTodasLeadsDeUser(usernameAlvo, permanente);

        String msg = permanente ? (AppConstants.TODAS_LEADS_EXCLUIDAS) : (AppConstants.TODAS_LEADS_INATIVADAS);

        if (permanente) {
            logger.warn("Utilizador: {} | Ação: Apagou TODAS as leads do username: '{}' permanentemente.",
                    admin.getUsername(), usernameAlvo);
        } else {
            logger.info("Utilizador: {} | Ação: Inativou TODAS as leads do username: '{}'.",
                    admin.getUsername(), usernameAlvo);
        }

        return Response.status(Response.Status.OK).entity(msg).build();
    }

    @DELETE
    @Path("/leads/{id}")
    public Response apagarLeadAdmin(@HeaderParam("token") String token,
                                    @PathParam("id") Long idLead,
                                    @QueryParam("permanente") boolean permanente) {

        UserEntity admin = validarAdmin(token);
        adminBean.apagarLeadAdmin(idLead, permanente);

        String msg = permanente ? AppConstants.LEAD_EXCLUIDA : AppConstants.LEAD_INATIVADA;

        if (permanente) {
            logger.warn("Utilizador: {} | Ação: Apagou a lead com o id: '{}' permanentemente.",
                    admin.getUsername(), idLead);
        } else {
            logger.info("Utilizador: {} | Ação: Inativou a lead com o id: '{}'.",
                    admin.getUsername(), idLead);
        }

        return Response.status(Response.Status.OK).entity(msg).build();
    }

}
