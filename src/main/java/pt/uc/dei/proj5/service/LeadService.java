package pt.uc.dei.proj5.service;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.uc.dei.proj5.beans.LeadBean;
import pt.uc.dei.proj5.dto.LeadDto;
import pt.uc.dei.proj5.entity.UserEntity;
import pt.uc.dei.proj5.utils.AppConstants;

//test
import java.util.ArrayList;
import java.util.List;

@Path("/leads")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LeadService extends BaseService{


    @Inject
    private LeadBean leadBean;

    @POST
    public Response addLead(@HeaderParam("token") String token, @Valid LeadDto leadDto) {

        UserEntity user = validarAcesso(token);

        LeadDto nova = leadBean.registarLead(leadDto, user);

        return Response.status(Response.Status.CREATED).entity(nova).build();

    }

    @GET
    public Response getLeads(@HeaderParam("token") String token, @QueryParam("estado") Integer estado) {

        UserEntity user = validarAcesso(token);

        // Alteramos para chamar um novo método no Bean que aceita os filtros
        List<LeadDto> leads = leadBean.getFilteredLeads(user, estado);

        if (leads == null) {
            leads = new ArrayList<>();
        }
        return Response.status(Response.Status.OK).entity(leads).build();
    }

    @GET
    @Path("/{id}")
    public Response getLeadById(@PathParam("id") Long id,@HeaderParam("token") String token) {
        UserEntity user = validarAcesso(token);

        LeadDto lead = leadBean.getLeadById(id, user);

        if (lead == null) {
            throw new NotFoundException(AppConstants.LEAD_NAO_ENCONTRADA);
        }

        return Response.status(Response.Status.OK).entity(lead).build(); // 200
    }

    @PATCH
    @Path("/{id}")
    public Response editarLead(@PathParam("id") Long id,@HeaderParam("token") String token, @Valid LeadDto dto) throws Exception {

        UserEntity user = validarAcesso(token);

        leadBean.updateLead(id,dto, user.getUsername());

        return Response.status(Response.Status.OK).entity("Lead atualizada com sucesso").build();
    }

    @DELETE
    @Path("/{id}")
    public Response softDeleteLead(@PathParam("id") Long id,@HeaderParam("token") String token) {

        UserEntity user = validarAcesso(token);

        int success = leadBean.softDeleteLead(id, user.getUsername());

        if (success == 0) {
            throw new NotFoundException(AppConstants.LEAD_NAO_ENCONTRADA); // 404
        }

        return Response.status(Response.Status.OK).entity(AppConstants.LEAD_REMOVIDA_SUCESSO).build(); // 200
    }
}