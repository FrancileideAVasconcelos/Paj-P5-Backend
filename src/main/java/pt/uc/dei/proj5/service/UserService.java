package pt.uc.dei.proj5.service;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.uc.dei.proj5.beans.TokenBean;
import pt.uc.dei.proj5.beans.UserBean;
import pt.uc.dei.proj5.dto.UserDto;
import pt.uc.dei.proj5.entity.UserEntity;

import java.util.Collections;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserService extends BaseService {

    @Inject
    UserBean userBean;

    @Inject
    TokenBean tokenBean;


    @POST
    @Path("/login")
    public Response login(UserDto user) {
        // Validação básica de campos vazios
        if (user.getUsername() == null || user.getPassword() == null) {
            return Response.status(401).entity("Dados incompletos!").build();
        }

        // Chama o novo método que retorna o token
        String token = userBean.loginToken(user.getUsername(), user.getPassword());

        if (token != null) {
            // Devolve o token ao utilizador em formato JSON
            return Response.status(200).entity(Collections.singletonMap("token", token)).build();
        }

        // Se a autenticação falhar [cite: 115]
        return Response.status(401).entity("Credenciais inválidas ou conta não confirmada!").build();    }


    @POST
    @Path("/logout")
    public Response logout(@HeaderParam("token") String token) {
        userBean.logout(token);
        // endpoint e retorna 200 Success
        return Response.status(200).build();
    }


    @POST
    @Path("/register")
    public Response register(@Valid UserDto newUser) throws Exception {

        userBean.register(newUser);

        return Response.status(Response.Status.CREATED).entity("Utilizador registado com sucesso!").build();
    }

    @POST
    @Path("/confirm")
    public Response confirmAccount(@QueryParam("token") String token) {
        System.out.println("Token que chegou ao backend: [" + token + "]");

        if (token == null) return Response.status(400).entity("Token não fornecido.").build();

        String resultado = userBean.confirmAccount(token);
        if (resultado.equals("Conta ativada com sucesso!")) {
            return Response.ok().entity(resultado).build();
        }
        return Response.status(400).entity(resultado).build();
    }

    @POST
    @Path("/forgot-password")
    public Response forgotPassword(UserDto userDto) { // Enviamos o email no body através do Dto
        if (userDto.getEmail() == null) return Response.status(400).entity("Email obrigatório").build();

        userBean.forgotPassword(userDto.getEmail());
        // Devolvemos sempre OK para não dar pistas a hackers de quais emails existem na BD
        return Response.ok().entity("Se o email existir, receberá as instruções.").build();
    }

    @POST
    @Path("/reset-password")
    public Response resetPassword(@QueryParam("token") String token, UserDto dto) {
        if (token == null || dto.getPassword() == null) {
            return Response.status(400).entity("Dados incompletos.").build();
        }

        String resultado = userBean.resetPassword(token, dto.getPassword());
        if (resultado.equals("Password redefinida com sucesso!")) {
            return Response.ok().entity(resultado).build();
        }
        return Response.status(400).entity(resultado).build();
    }

    @GET
    @Path("/profile")
    public Response getUserProfile(@HeaderParam("token") String token) {

        UserEntity userEntity = validarAcesso(token);

        // 2. Convertes diretamente para DTO e devolves OK (Sem Magic Constants)
        UserDto user = userBean.converterParaDto(userEntity);

        return Response.status(Response.Status.OK).entity(user).build();
    }

    @PATCH
    @Path("/perfil")
    public Response updateProfile(@HeaderParam("token") String token,
                                   @Valid UserDto dadosNovos) {

        UserEntity user = validarAcesso(token);

        // Se falhar (ex: email já existe), o Bean atira um erro e o Mapper devolve 409
        userBean.updateUser(user, dadosNovos);

        // Caminho feliz:
        return Response.status(Response.Status.OK).entity("Perfil atualizado com sucesso!").build();
    }

    @GET
    @Path("/checkPass")
    public Response verificaPassword(
            @HeaderParam("token") String token,
            @HeaderParam("passatual") String password) { // 1. TUDO EM MINÚSCULAS!

        UserEntity user = validarAcesso(token);

        // ADICIONA ESTAS 3 LINHAS PARA DEBUG:
        System.out.println("--- DEBUG DE PASSWORD ---");
        System.out.println("Password na Base de Dados: [" + user.getPassword() + "]");
        System.out.println("Password recebida do React: [" + password + "]");

        boolean passwordCorreta = userBean.verificaPassword(user, password);

        if (!passwordCorreta) {
            // 2. MUDA PARA FORBIDDEN (403) PARA O REACT NÃO FAZER LOGOUT!
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("A password atual está incorreta.")
                    .build();
        }

        return Response.status(Response.Status.OK)
                .entity("Password confirmada com sucesso.")
                .build();
    }
}
