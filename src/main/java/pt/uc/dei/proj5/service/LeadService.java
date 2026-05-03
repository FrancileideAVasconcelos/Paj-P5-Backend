package pt.uc.dei.proj5.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.uc.dei.proj5.beans.LeadBean;
import pt.uc.dei.proj5.dto.LeadDto;
import pt.uc.dei.proj5.dto.PaginatedResponseDto;
import pt.uc.dei.proj5.entity.UserEntity;
import pt.uc.dei.proj5.utils.AppConstants;

import java.util.ArrayList;
import java.util.List;

@Path("/leads")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LeadService extends BaseService {

    // 1. INICIALIZAR O LOGGER
    private static final Logger logger = LogManager.getLogger(LeadService.class);

    @Inject
    private LeadBean leadBean;

    @POST
    public Response addLead(@HeaderParam("token") String token, @Valid LeadDto leadDto) {

        UserEntity user = validarAcesso(token);

        LeadDto nova = leadBean.registarLead(leadDto, user);

        logger.info("Utilizador: {} | Ação: Criou a lead '{}'.",
                user.getUsername(), nova.getTitulo());

        return Response.status(Response.Status.CREATED).entity(nova).build();

    }

    @GET
    public Response getLeads(@HeaderParam("token") String token,
                             @QueryParam("estado") Integer estado,
                             @QueryParam("search") String search,
                             @QueryParam("page") @DefaultValue("1") int page,
                             @QueryParam("limit") @DefaultValue("5") int limit) {
        UserEntity user = validarAcesso(token);
        PaginatedResponseDto<LeadDto> response = leadBean.getFilteredLeads(user, estado, search, page, limit);
        return Response.status(Response.Status.OK).entity(response).build();
    }

    @GET
    @Path("/{id}")
    public Response getLeadById(@PathParam("id") Long id, @HeaderParam("token") String token) {
        UserEntity user = validarAcesso(token);

        LeadDto lead = leadBean.getLeadById(id, user);

        if (lead == null) {
            throw new NotFoundException(AppConstants.LEAD_NAO_ENCONTRADA);
        }

        return Response.status(Response.Status.OK).entity(lead).build(); // 200
    }

    @PATCH
    @Path("/{id}")
    public Response editarLead(@PathParam("id") Long id, @HeaderParam("token") String token, @Valid LeadDto dto) throws Exception {

        UserEntity user = validarAcesso(token);

        leadBean.updateLead(id, dto, user.getUsername());

        logger.info("Utilizador: {} | Ação: Editou os dados da lead com o ID: {}.",
                user.getUsername(), id);

        return Response.status(Response.Status.OK).entity(AppConstants.LEAD_ATUALIZADA_SUCESSO).build();
    }

    @DELETE
    @Path("/{id}")
    public Response softDeleteLead(@PathParam("id") Long id, @HeaderParam("token") String token) {

        UserEntity user = validarAcesso(token);

        int success = leadBean.softDeleteLead(id, user.getUsername());

        if (success == 0) {
            throw new NotFoundException(AppConstants.LEAD_NAO_ENCONTRADA); // 404
        }

        logger.warn("Utilizador: {} | Ação: Inativou a lead com o ID: {}.",
                user.getUsername(), id);

        return Response.status(Response.Status.OK).entity(AppConstants.LEAD_REMOVIDA_SUCESSO).build(); // 200
    }
}