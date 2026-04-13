package pt.uc.dei.proj5.service;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        // Garanta que todos os headers enviados pelo seu script estão aqui
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization, username, token, password, passatual");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PATCH, DELETE, PUT, OPTIONS");

        // Se o navegador perguntar quais são as regras (OPTIONS), respondemos OK imediatamente
        if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
            responseContext.setStatus(200);
        }
    }
}