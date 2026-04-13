package pt.uc.dei.proj5.service;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.uc.dei.proj5.beans.AdminBean;
import pt.uc.dei.proj5.dto.LeadDto;
import pt.uc.dei.proj5.dto.ClientDto;
import pt.uc.dei.proj5.dto.UserDto;

import java.util.List;

@Path("/admin")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AdminService extends BaseService {

    @Inject
    AdminBean adminBean;


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
    public Response getAllUsers(@HeaderParam("token") String token) {

        validarAdmin(token);
        List<UserDto> users = adminBean.getAllUsers();

        return Response.status(Response.Status.OK).entity(users).build();
    }

    @PATCH
    @Path("/users/{username}/reactivate")
    public Response reactivateUser(@HeaderParam("token") String token,
                                   @PathParam("username") String usernameAlvo) {

        validarAdmin(token);

        adminBean.reactivateUser(usernameAlvo);
        return Response.status(Response.Status.OK).entity("Utilizador reativado com sucesso.").build();
    }

    @DELETE
    @Path("/users/{username}")
    public Response deleteUser(@HeaderParam("token") String token,
                               @PathParam("username") String usernameAlvo,
                               @QueryParam("permanente") boolean permanente) {

        validarAdmin(token);

        if (permanente) {
            adminBean.hardDeleteUser(usernameAlvo);
            // Mensagem corrigida para corresponder à ação "permanente"
            return Response.status(Response.Status.OK).entity("Utilizador apagado permanentemente.").build();
        } else {
            adminBean.softDeleteUser(usernameAlvo);
            // Mensagem corrigida para corresponder à inativação
            return Response.status(Response.Status.OK).entity("Utilizador inativado com sucesso.").build();
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

        validarAdmin(token);

        adminBean.reactivateClienteAdmin(idCliente);
        return Response.status(Response.Status.OK).entity("Cliente reativado com sucesso.").build();

    }

    @PATCH
    @Path("/users/{username}/clients/reactivate")
    public Response reativarTodosClientesDeUser(@HeaderParam("token") String token,
                                                @PathParam("username") String usernameAlvo) {

        validarAdmin(token);

            adminBean.reativarTodosClientesDeUser(usernameAlvo);
            return Response.status(Response.Status.OK).entity("Todos os clientes foram reativados com sucesso.").build();

    }

    @DELETE
    @Path("/users/{username}/clients")
    public Response apagarTodosClientesDeUser(@HeaderParam("token") String token,
                                              @PathParam("username") String usernameAlvo,
                                              @QueryParam("permanente") boolean permanente) {

        validarAdmin(token);

            adminBean.apagarTodosClientesDeUser( usernameAlvo, permanente);
            String msg = permanente ? "Todos os clientes excluídos permanentemente." : "Todos os clientes inativados.";
            return Response.status(Response.Status.OK).entity(msg).build();

    }

    // Editar um cliente específico
    @PATCH
    @Path("/clients/{id}")
    public Response editarClienteAdmin(@HeaderParam("token") String token,
                                       @PathParam("id") Long idCliente,
                                       @Valid ClientDto dto) throws Exception {

        validarAdmin(token);

            adminBean.editarClienteAdmin(idCliente, dto);
            return Response.status(Response.Status.OK).entity("Cliente atualizado com sucesso (Admin).").build();

    }

    // Apagar um cliente específico
    @DELETE
    @Path("/clients/{id}")
    public Response apagarClienteAdmin(@HeaderParam("token") String token,
                                       @PathParam("id") Long idCliente,
                                       @QueryParam("permanente") boolean permanente) {

        validarAdmin(token);


            adminBean.apagarClienteAdmin(idCliente, permanente);
            String msg = permanente ? "Cliente excluído permanentemente." : "Cliente inativado com sucesso.";
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

        validarAdmin(token);

            adminBean.editarLeadAdmin(idLead, dto);
            return Response.status(Response.Status.OK).entity("Lead atualizada com sucesso (Admin).").build();

    }

    @PATCH
    @Path("/leads/{id}/reactivate")
    public Response reactivateLeadAdmin(@HeaderParam("token") String token, @PathParam("id") Long idLead) {

        validarAdmin(token);

            adminBean.reactivateLeadsAdmin(idLead);
            return Response.status(Response.Status.OK).entity("Lead reativada com sucesso.").build();

    }

    @PATCH
    @Path("/users/{username}/leads/reactivate")
    public Response reativarTodasLeadsDeUser(@HeaderParam("token") String token, @PathParam("username") String usernameAlvo) {

        validarAdmin(token);

        adminBean.reativarTodasLeadsDeUser(usernameAlvo);

        return Response.status(Response.Status.OK).entity("Todas as leads reativadas com sucesso.").build();
    }

    @DELETE
    @Path("/users/{username}/leads")
    public Response apagarTodasLeadsDeUser(@HeaderParam("token") String token,
                                           @PathParam("username") String usernameAlvo,
                                           @QueryParam("permanente") boolean permanente) {

        validarAdmin(token);

        adminBean.apagarTodasLeadsDeUser(usernameAlvo, permanente);

        String msg = permanente ? "Leads excluídas permanentemente." : "Leads inativadas com sucesso.";
        return Response.status(Response.Status.OK).entity(msg).build();
    }

    @DELETE
    @Path("/leads/{id}")
    public Response apagarLeadAdmin(@HeaderParam("token") String token,
                                    @PathParam("id") Long idLead,
                                    @QueryParam("permanente") boolean permanente) {

        validarAdmin(token);

        adminBean.apagarLeadAdmin(idLead, permanente);

        String msg = permanente ? "Lead excluída permanentemente." : "Lead inativada com sucesso.";
        return Response.status(Response.Status.OK).entity(msg).build();
    }

}
