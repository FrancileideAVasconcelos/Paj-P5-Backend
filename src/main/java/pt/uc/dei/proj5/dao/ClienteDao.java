package pt.uc.dei.proj5.dao;

import jakarta.ejb.Stateless;
import pt.uc.dei.proj5.dto.ClientDto;
import pt.uc.dei.proj5.entity.ClienteEntity;
import pt.uc.dei.proj5.entity.UserEntity;

import java.io.Serializable;
import java.util.List;

@Stateless
public class ClienteDao extends DefaultDao<ClienteEntity> implements Serializable {


    public ClienteDao() {
        super(ClienteEntity.class);
    }

    public void guardaCliente(ClientDto newClient,UserEntity u){

        ClienteEntity finalClient = new ClienteEntity();

        finalClient.setNome(newClient.getNome());
        finalClient.setEmail(newClient.getEmail());
        finalClient.setTelefone(newClient.getTelefone());
        finalClient.setEmpresa(newClient.getEmpresa());
        finalClient.setUser(u);

        finalClient.setAtivo(true);

        persist(finalClient);
    }

    public void atualizaCliente(ClienteEntity clienteAtual, ClientDto dtoNovo) {

        // Atualiza a entidade existente com os dados que vieram do DTO
        clienteAtual.setNome(dtoNovo.getNome());
        clienteAtual.setEmail(dtoNovo.getEmail());
        clienteAtual.setTelefone(dtoNovo.getTelefone());
        clienteAtual.setEmpresa(dtoNovo.getEmpresa());

        // Usa o 'merge' para guardar as alterações na base de dados
        merge(clienteAtual);
    }

    // Procura um cliente pelo ID
    public ClienteEntity findClienteById(Long id) {
        return em.find(ClienteEntity.class, id);
    }

    // Lista apenas os clientes de um determinado utilizador
    public List<ClienteEntity> findAllActiveByUser(UserEntity user) {
        return em.createQuery("SELECT c FROM ClienteEntity c WHERE c.users = :user AND c.isAtivo = true", ClienteEntity.class)
                .setParameter("user", user)
                .getResultList();
    }

    public boolean existsByNomeAndEmpresa(String nome, String empresa) {
        Long count = em.createQuery(
                        "SELECT COUNT(c) FROM ClienteEntity c WHERE LOWER(c.nome) = LOWER(:nome) AND LOWER(c.empresa) = LOWER(:empresa) AND c.isAtivo = true", Long.class)
                .setParameter("nome", nome)
                .setParameter("empresa", empresa)
                .getSingleResult();
        return count > 0;
    }

    public boolean existsByNomeAndEmpresaForEdit(Long idToIgnore, String nome, String empresa) {
        Long count = em.createQuery(
                        "SELECT COUNT(c) FROM ClienteEntity c WHERE LOWER(c.nome) = LOWER(:nome) AND LOWER(c.empresa) = LOWER(:empresa) AND c.id != :id AND c.isAtivo = true", Long.class)
                .setParameter("nome", nome)
                .setParameter("empresa", empresa)
                .setParameter("id", idToIgnore)
                .getSingleResult();
        return count > 0;
    }

    public int softDeleteClient(Long id) {

        return em.createQuery("UPDATE ClienteEntity c SET c.isAtivo = false WHERE c.id = :id")
                .setParameter("id", id)
                .executeUpdate();

    }

    // ======== ADMIN ===========//

    public List<ClienteEntity> findAllByUserForAdmin(UserEntity user) {
        return em.createQuery("SELECT c FROM ClienteEntity c WHERE c.users = :user", ClienteEntity.class)
                .setParameter("user", user)
                .getResultList();
    }

    public void reativarTodosOsClients(UserEntity user) {
        em.createQuery("UPDATE ClienteEntity c SET c.isAtivo = true WHERE c.users = :user")
                .setParameter("user", user)
                .executeUpdate();
    }

    public void inativarTodosOsClients(UserEntity user) {
        em.createQuery("UPDATE ClienteEntity c SET c.isAtivo = false WHERE c.users = :user")
                .setParameter("user", user)
                .executeUpdate();
    }

    public void apagarTodosOsClientsPermanente(UserEntity user) {
        em.createQuery("DELETE FROM ClienteEntity c WHERE c.users = :user")
                .setParameter("user", user)
                .executeUpdate();
    }

    // 4. Apagar/Inativar uma Única Lead (Para o Admin)
    public void apagarClientAdmin(ClienteEntity client, boolean permanente) {
        if (permanente) {
            remove(client);
        } else {
            client.setAtivo(false);
            merge(client);
        }
    }

}
