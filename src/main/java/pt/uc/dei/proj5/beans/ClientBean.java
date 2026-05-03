package pt.uc.dei.proj5.beans;


import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import pt.uc.dei.proj5.dao.ClienteDao;
import pt.uc.dei.proj5.dao.UserDao;
import pt.uc.dei.proj5.dto.ClientDto;
import pt.uc.dei.proj5.dto.PaginatedResponseDto;
import pt.uc.dei.proj5.entity.ClienteEntity;
import pt.uc.dei.proj5.entity.UserEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class ClientBean implements Serializable {

    @Inject
    ClienteDao clienteDao;

    @Inject
    UserDao userDao;

    public ClientDto registarCliente(ClientDto newClient, UserEntity user) throws Exception {

        if (clienteDao.existsByNomeAndEmpresa(newClient.getNome(),newClient.getEmpresa())){
            throw new Exception("Este cliente já está registado nesta empresa.");
        }

        clienteDao.guardaCliente(newClient, user);

        return newClient;
    }

    public ClientDto getClientById(Long id, UserEntity requester) {
        ClienteEntity client = clienteDao.findClienteById(id);

        if (client == null || (!client.isAtivo() && !requester.isAdmin())) {
            return null;
        }

        if (!client.getUser().getUsername().equals(requester.getUsername()) && !requester.isAdmin()) {
            throw new SecurityException("Acesso Negado: Este cliente pertence a outro utilizador.");
        }

        return converForDto(client);
    }


    public void editarCliente(Long idCliente, ClientDto dtoNovo, String usernameRequester) throws Exception {

        ClienteEntity clienteAtual = clienteDao.findClienteById(idCliente);

        if (clienteAtual == null || !clienteAtual.isAtivo()) {
            throw new Exception("Cliente não encontrado ou inativo.");
        }

        // Verifica se o username do dono na entidade coincide com quem está a pedir
        if (!clienteAtual.getUser().getUsername().equals(usernameRequester)) {
            throw new SecurityException("Acesso Negado: Este cliente pertence a outro utilizador.");
        }

        if (clienteDao.existsByNomeAndEmpresaForEdit(idCliente, dtoNovo.getNome(), dtoNovo.getEmpresa())) {
            throw new Exception("Este cliente já está registado nesta empresa.");
        }

        clienteDao.atualizaCliente(clienteAtual, dtoNovo);
    }

    public PaginatedResponseDto<ClientDto> listClients(UserEntity user, String search, int page, int limit) {
        if (user == null) return new PaginatedResponseDto<>(new ArrayList<>(), 0, page, limit);

        long total = clienteDao.countFilteredClients(user, search);
        List<ClienteEntity> entidades = clienteDao.findFilteredClientsPaginated(user, search, page, limit);

        List<ClientDto> myClients = new ArrayList<>();
        for(ClienteEntity e : entidades){
            myClients.add(converForDto(e));
        }
        return new PaginatedResponseDto<>(myClients, total, page, limit);
    }

    public int softDeleteClient(Long id, String usernameRequester) {
        ClienteEntity cliente = clienteDao.findClienteById(id);

        // Valida propriedade antes de apagar
        if (cliente != null && !cliente.getUser().getUsername().equals(usernameRequester)) {
            throw new SecurityException("Não tem permissão para eliminar este cliente.");
        }
        return clienteDao.softDeleteClient(id);
    }

    public ClientDto converForDto(ClienteEntity e){
        ClientDto c = new ClientDto();
        c.setId(e.getId().longValue());
        c.setNome(e.getNome());
        c.setTelefone(e.getTelefone());
        c.setEmail(e.getEmail());
        c.setEmpresa(e.getEmpresa());

        if (e.getUser() != null) {
            c.setDono(e.getUser().getUsername());
        } else {
            c.setDono("Sem dono");
        }

        c.setAtivo(e.isAtivo());

        return c;
    }

}
