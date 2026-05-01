package pt.uc.dei.proj5.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.uc.dei.proj5.beans.ChatBean;
import pt.uc.dei.proj5.dto.MensagemDto;
import pt.uc.dei.proj5.entity.UserEntity;
import pt.uc.dei.proj5.utils.AppConstants;

import java.util.List;

@Path("/chat")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ChatService extends BaseService {

    private static final Logger logger = LogManager.getLogger(ChatService.class);

    @Inject
    ChatBean chatBean;

    @POST
    @Path("/send")
    public Response enviarMensagem(@HeaderParam("token") String token, MensagemDto dto) throws Exception {
        UserEntity remetente = validarAcesso(token);

        if (dto.getDestinatarioUsername() == null || dto.getConteudo() == null || dto.getConteudo().trim().isEmpty()) {
            return Response.status(400).entity(AppConstants.DADOS_INVALIDOS_MENSAGEM).build();
        }

        MensagemDto enviada = chatBean.enviarMensagem(remetente.getUsername(), dto.getDestinatarioUsername(), dto.getConteudo());

        logger.info("Utilizador: {} | Ação: Enviou uma mensagem no chat para '{}'.",
                remetente.getUsername(), dto.getDestinatarioUsername());

        return Response.status(Response.Status.CREATED).entity(enviada).build();
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

        logger.info("AUDITORIA | Utilizador: {} | Ação: Marcou as mensagens do utilizador '{}' como lidas.",
                eu.getUsername(), outroUser);

        return Response.ok(AppConstants.MENSAGENS_LIDAS).build();
    }

    @GET
    @Path("/contactos")
    public Response getContactosOrdenados(@HeaderParam("token") String token) {
        UserEntity eu = validarAcesso(token);
        return Response.ok(chatBean.getContactosOrdenados(eu)).build();
    }
}