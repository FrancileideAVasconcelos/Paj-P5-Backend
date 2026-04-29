package pt.uc.dei.proj5.service;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.uc.dei.proj5.beans.DashboardBean;
import pt.uc.dei.proj5.dto.DashboardDto;
import pt.uc.dei.proj5.entity.UserEntity;

@Path("/dashboard")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DashboardService extends BaseService {

    @Inject
    DashboardBean dashboardBean;

    @GET
    @Path("/stats")
    public Response getDashboardStats(@HeaderParam("token") String token) {

        // O BaseService valida o token e diz-nos quem é o utilizador
        UserEntity user = validarAcesso(token);

        // Pedimos ao Bean para calcular as estatísticas para ESTE utilizador
        DashboardDto stats = dashboardBean.getStats(user);

        // Devolvemos o JSON pronto para o React
        return Response.ok(stats).build();
    }
}