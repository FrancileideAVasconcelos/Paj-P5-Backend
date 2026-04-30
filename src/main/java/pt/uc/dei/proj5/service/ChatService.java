package pt.uc.dei.proj5.service;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.uc.dei.proj5.beans.ChatBean;
import pt.uc.dei.proj5.dto.MensagemDto;
import pt.uc.dei.proj5.entity.UserEntity;

import java.util.Collections;
import java.util.List;

@Path("/chat")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ChatService extends BaseService {

    @Inject
    ChatBean chatBean;

    @POST
    @Path("/send")
    public Response enviarMensagem(@HeaderParam("token") String token, MensagemDto dto) {
        UserEntity remetente = validarAcesso(token);

        if (dto.getDestinatarioUsername() == null || dto.getConteudo() == null || dto.getConteudo().trim().isEmpty()) {
            return Response.status(400).entity("Dados inválidos para a mensagem.").build();
        }

        try {
            // O React envia apenas o Destinatário e o Conteúdo. O Remetente somos nós (token).
            MensagemDto enviada = chatBean.enviarMensagem(remetente.getUsername(), dto.getDestinatarioUsername(), dto.getConteudo());
            return Response.status(Response.Status.CREATED).entity(enviada).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/historico/{username}")
    public Response getHistorico(@HeaderParam("token") String token, @PathParam("username") String outroUser) {
        UserEntity eu = validarAcesso(token);
        List<MensagemDto> historico = chatBean.getHistorico(eu.getUsername(), outroUser);
        return Response.ok(historico).build();
    }

    @PATCH
    @Path("/lidas/{username}")
    public Response marcarComoLidas(@HeaderParam("token") String token, @PathParam("username") String outroUser) {
        UserEntity eu = validarAcesso(token);
        chatBean.marcarComoLidas(eu.getUsername(), outroUser);
        return Response.ok("Mensagens marcadas como lidas.").build();
    }

    @GET
    @Path("/contactos")
    public Response getContactosOrdenados(@HeaderParam("token") String token) {
        UserEntity eu = validarAcesso(token);
        return Response.ok(chatBean.getContactosOrdenados(eu)).build();
    }
}