package pt.uc.dei.proj5.beans;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import pt.uc.dei.proj5.dao.ClienteDao;
import pt.uc.dei.proj5.dao.LeadDao;
import pt.uc.dei.proj5.dao.UserDao;
import pt.uc.dei.proj5.dao.VerificationTokenDao;
import pt.uc.dei.proj5.dto.ClientDto;
import pt.uc.dei.proj5.dto.LeadDto;
import pt.uc.dei.proj5.entity.ClienteEntity;
import pt.uc.dei.proj5.dto.UserDto;
import pt.uc.dei.proj5.entity.LeadEntity;
import pt.uc.dei.proj5.entity.UserEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Passamos a @Stateless (EJB) para gerir as transações automaticamente com a Base de Dados
@Stateless
public class AdminBean implements Serializable {

    @Inject
    UserBean userBean;

    @Inject
    UserDao userDao;

    @Inject
    ClienteDao clienteDao;

    @Inject
    ClientBean clientBean;

    @Inject
    LeadDao leadDao;

    @Inject
    LeadBean leadBean;

    @Inject
    EmailBean emailBean;

    @Inject
    VerificationTokenDao verificationTokenDao;

    public void inviteUser(String email) throws Exception {
        if (userDao.getUserByEmail(email) != null) {
            throw new Exception("Já existe um utilizador com este e-mail no sistema.");
        }

        // 1. Cria o utilizador fantasma com dados provisórios
        UserDto dummyUser = new UserDto();
        dummyUser.setEmail(email);
        dummyUser.setUsername("temp_" + java.util.UUID.randomUUID().toString().substring(0,8));
        dummyUser.setPassword("pendente");
        dummyUser.setPrimeiroNome("Pendente");
        dummyUser.setUltimoNome("Pendente");
        dummyUser.setTelefone("000000000");
        dummyUser.setFotoUrl("");

        userDao.novoUserDB(dummyUser); // Vai nascer inativo devido à alteração anterior no DAO

        UserEntity guardado = userDao.checkUsername(dummyUser.getUsername());

        // 2. Gera o Token de Registo
        String tokenConfirmacao = TokenBean.generateToken();
        pt.uc.dei.proj5.entity.VerificationToken vToken = new pt.uc.dei.proj5.entity.VerificationToken();
        vToken.setToken(tokenConfirmacao);
        vToken.setUser(guardado);
        vToken.setTipo("REGISTRATION"); // Tipo específico para registo
        vToken.setExpiryDate(java.time.LocalDateTime.now().plusHours(48)); // Expira em 48h
        verificationTokenDao.persist(vToken);

        // 3. Envia o E-mail usando o MailHog
        String subject = "Convite para a Plataforma CRM";
        String body = "Bem-vindo!\n\nFoi convidado pelo administrador para se juntar ao CRM.\n\n" +
                "Por favor, complete o seu registo e crie a sua conta clicando no link abaixo:\n" +
                "http://localhost:5173/complete-registration?token=" + tokenConfirmacao + "\n\n" +
                "Este link expira em 48 horas.";

        emailBean.sendEmail(email, subject, body);
    }

    // Devolve a lista de todos os utilizadores em formato DTO
    public List<UserDto> getAllUsers() {
        List<UserEntity> users = userDao.findAllUsers();
        List<UserDto> dtos = new ArrayList<>();

        for (UserEntity u : users) {
            dtos.add(userBean.converterParaDto(u));
        }
        return dtos;
    }

    public UserDto getProfileUser(String usernameAlvo){

        UserEntity alvo = userDao.checkUsername(usernameAlvo);
        if (alvo == null) throw new NotFoundException("Utilizador não encontrado.");

        return userBean.converterParaDto(alvo);
    }

    public void reactivateUser(String usernameAlvo) {

        UserEntity alvo = userDao.checkUsername(usernameAlvo);
        if (alvo == null) throw new NotFoundException("Utilizador não encontrado.");

        alvo.setIsAtivo(true); // Passa o estado novamente para ativo
        userDao.merge(alvo);
    }

    public void softDeleteUser(String usernameAlvo){

        UserEntity alvo = userDao.checkUsername(usernameAlvo);
        if (alvo == null) throw new NotFoundException("Utilizador não encontrado.");

        alvo.setIsAtivo(false);
        userDao.merge(alvo);
    }

    public void hardDeleteUser(String usernameAlvo) {

        UserEntity alvo = userDao.checkUsername(usernameAlvo);
        if (alvo == null) throw new NotFoundException("Utilizador não encontrado.");


        userDao.remove(alvo);
    }

    // metodos de clientes

    public List<ClientDto> getClientFromUser(String usernameAlvo) {

        UserEntity alvo = userDao.checkUsername(usernameAlvo);
        if (alvo == null) throw new NotFoundException("Utilizador alvo não encontrado.");

        List<ClienteEntity> clients = clienteDao.findAllByUserForAdmin(alvo);
        List<ClientDto> dtos = new ArrayList<>();
        for (ClienteEntity c : clients) {
            dtos.add(clientBean.converForDto(c));
        }
        return dtos;
    }

    //Editar Clientes de outros utilizadores
    public void editarClienteAdmin(Long idCliente, ClientDto dtoNovo) throws Exception {

        ClienteEntity clientAtual = clienteDao.findClienteById(idCliente);
        if (clientAtual == null) throw new NotFoundException("Cliente não encontrada.");

        if (clienteDao.existsByNomeAndEmpresaForEdit(idCliente, dtoNovo.getNome(), dtoNovo.getEmpresa())) {
            throw new Exception("Este cliente já está registado nesta empresa.");
        }

        clienteDao.atualizaCliente(clientAtual, dtoNovo);
    }

    // Reativar um cliente inativo
    public void reactivateClienteAdmin(Long idCliente) {

        ClienteEntity c = clienteDao.findClienteById(idCliente);
        if (c == null) throw new NotFoundException("Cliente não encontrada.");

        c.setAtivo(true);
        clienteDao.merge(c);
    }

    // Reativar TODOS os clientes inativos de um utilizador
    public void reativarTodosClientesDeUser(String usernameAlvo) {

        UserEntity alvo = userDao.checkUsername(usernameAlvo);
        if (alvo == null) throw new NotFoundException("Utilizador não encontrado.");

        clienteDao.reativarTodosOsClients(alvo);
    }

    // Apagar Cliente (Soft ou Hard Delete)
    public void apagarClienteAdmin(Long idCliente, boolean permanente){

        ClienteEntity cliente = clienteDao.findClienteById(idCliente);
        if (cliente == null) throw new NotFoundException("Cliente não encontrada.");

        clienteDao.apagarClientAdmin(cliente, permanente);
    }

    // Apagar todos os clientes criados por um utilizador
    public void apagarTodosClientesDeUser( String usernameAlvo, boolean permanente) {

        UserEntity alvo = userDao.checkUsername(usernameAlvo);
        if (alvo == null) throw new NotFoundException("Utilizador não encontrado.");

        if (permanente) {
            clienteDao.apagarTodosOsClientsPermanente(alvo);
        } else {
            clienteDao.inativarTodosOsClients(alvo);
        }
    }
    // ==========================================
    // MÉTODOS DE LEADS (ADMIN)
    // ==========================================

    public List<LeadDto> getLeadsFromUser(String usernameAlvo) {

        UserEntity alvo = userDao.checkUsername(usernameAlvo);
        if (alvo == null) throw new NotFoundException("Utilizador alvo não encontrado.");

        List<LeadEntity> leads = leadDao.findAllByUserForAdmin(alvo);
        List<LeadDto> dtos = new ArrayList<>();
        for (LeadEntity l : leads) {
            dtos.add(leadBean.converterParaDto(l));
        }
        return dtos;
    }

    public void editarLeadAdmin(Long idLead, LeadDto dtoNovo){

        LeadEntity lead = leadDao.findLeadById(idLead);
        if (lead == null) throw new NotFoundException("Lead não encontrada.");

        // Atualiza a lead usando o método que já tens no LeadDao
        leadDao.updateLead(lead, dtoNovo);
    }

    public void reactivateLeadsAdmin(Long idLead) {

        LeadEntity l = leadDao.findLeadById(idLead);
        if (l == null) throw new NotFoundException("Lead não encontrada.");

        l.setIsAtivo(true);
        leadDao.merge(l); // Guarda a alteração
    }

    public void reativarTodasLeadsDeUser(String usernameAlvo)  {
        UserEntity alvo = userDao.checkUsername(usernameAlvo);
        if (alvo == null) throw new NotFoundException("Utilizador não encontrado.");

        // O DAO trata da query em bloco de forma ultra rápida
        leadDao.reativarTodasAsLeads(alvo);
    }

    public void apagarTodasLeadsDeUser(String usernameAlvo, boolean permanente) {
        UserEntity alvo = userDao.checkUsername(usernameAlvo);
        if (alvo == null) throw new NotFoundException("Utilizador não encontrado.");

        if (permanente) {
            leadDao.apagarTodasAsLeadsPermanente(alvo);
        } else {
            leadDao.inativarTodasAsLeads(alvo);
        }
    }

    public void apagarLeadAdmin(Long idLead, boolean permanente){
        LeadEntity lead = leadDao.findLeadById(idLead);
        if (lead == null) throw new NotFoundException("Lead não encontrada.");

        leadDao.apagarLeadAdmin(lead, permanente);
    }
}