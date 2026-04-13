package pt.uc.dei.proj5.service;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.uc.dei.proj5.beans.ClientBean;
import pt.uc.dei.proj5.dto.ClientDto;
import pt.uc.dei.proj5.entity.UserEntity;
import pt.uc.dei.proj5.utils.AppConstants; // A tua nova classe de constantes
import java.util.List;

@Path("/clients")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ClientService extends BaseService {

    @Inject
    private ClientBean clientBean;

    @POST
    public Response addCliente(@HeaderParam("token") String token, @Valid ClientDto dto) throws Exception {
        UserEntity user = validarAcesso(token);

        // Se houver duplicação, o Bean lança Exception e o teu GenericExceptionMapper devolve 409
        ClientDto novo = clientBean.registarCliente(dto, user);

        return Response.status(Response.Status.CREATED).entity(novo).build(); // 201
    }

    @GET
    public Response getClientes(@HeaderParam("token") String token) {
        UserEntity user = validarAcesso(token);

        List<ClientDto> clientes = clientBean.listClients(user);
        return Response.status(Response.Status.OK).entity(clientes).build(); // 200
    }

    @GET
    @Path("/{id}")
    public Response getClientById(@PathParam("id") Long id, @HeaderParam("token") String token) {
        UserEntity user = validarAcesso(token);

        ClientDto client = clientBean.getClientById(id, user.getUsername());

        // Se não existir, atiramos NotFoundException e o Java devolve 404 automaticamente
        if (client == null) {
            throw new NotFoundException(AppConstants.CLIENTE_NAO_ENCONTRADO);
        }

        return Response.status(Response.Status.OK).entity(client).build(); // 200
    }

    @PATCH
    @Path("/{id}")
    public Response editClient(@PathParam("id") Long id, @HeaderParam("token") String token, @Valid ClientDto dto) throws Exception {
        UserEntity user = validarAcesso(token);

        // Se não for dono -> Lança SecurityException -> Mapper devolve 403
        // Se duplicar nome -> Lança Exception -> Mapper devolve 409
        clientBean.editarCliente(id, dto, user.getUsername());

        return Response.status(Response.Status.OK).entity("Cliente atualizado com sucesso").build();
    }

    @DELETE
    @Path("/{id}")
    public Response softDeleteClient(@PathParam("id") Long id, @HeaderParam("token") String token) throws Exception {
        UserEntity user = validarAcesso(token);

        // O Bean valida se ele é o dono e tenta apagar
        int sucess = clientBean.softDeleteClient(id, user.getUsername());

        // Se o sucesso for 0, o cliente não existia
        if (sucess == 0) {
            throw new NotFoundException(AppConstants.CLIENTE_NAO_ENCONTRADO); // 404
        }

        return Response.status(Response.Status.OK).entity(AppConstants.CLIENTE_REMOVIDO_SUCESSO).build(); // 200
    }
}