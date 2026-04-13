// ==========================================
// admin.js - Painel de Administração Dinâmico
// ==========================================
let clientesAlvoAdmin = [];
let leadsAlvoAdmin = [];

const token = localStorage.getItem("token");

// ==========================================
// 1. PONTO DE ENTRADA
// ==========================================
async function carregarPaginaAdmin() {
    const content = document.getElementById("content");

    if (localStorage.getItem("isAdmin") !== "true") {
        content.innerHTML = `<h2 style="color:red; text-align:center; margin-top: 50px;">Acesso Negado</h2>`;
        return;
    }

    if (window.location.hash !== "#admin") window.location.hash = "#admin";

    content.innerHTML = `
        <div class="barra-clientes" style="margin-bottom: 20px;">
            <h2>Painel de Administração</h2>
            <button id="btnVoltarAdmin" class="btn" style="display: none;" onclick="carregarListaUtilizadores()"><i class="fa-solid fa-arrow-left"></i> Voltar à Lista</button>
        </div>
        <div id="adminConteudo" class="admin-container">
            <p><i class="fa-solid fa-spinner fa-spin"></i> A carregar utilizadores...</p>
        </div>
    `;

    await carregarListaUtilizadores();
}

// ==========================================
// 2. FUNÇÕES PRINCIPAIS DE CARREGAMENTO
// ==========================================

async function carregarListaUtilizadores() {
    const conteudo = document.getElementById("adminConteudo");

    // Esconde o botão de voltar à lista sempre que estamos na própria lista
    const btnVoltar = document.getElementById("btnVoltarAdmin");
    if (btnVoltar) btnVoltar.style.display = "none";

    try {
        const response = await fetch("http://localhost:8080/projeto4/rest/admin/users", {
            method: "GET", headers: {"token": token}
        });

        if (response.ok) {
            const users = await response.json();

            users.sort((a, b) => a.primeiroNome.localeCompare(b.primeiroNome)); // lista users por ordem alfabetica

            conteudo.innerHTML = gerarHtmlListaUtilizadores(users);
        } else {
            conteudo.innerHTML = `<p style="color:red;">Erro ao carregar utilizadores: ${await response.text()}</p>`;
        }
    } catch (error) {
        conteudo.innerHTML = `<p style="color:red;">Falha na ligação ao servidor.</p>`;
    }
}

async function abrirDetalhesUtilizador(username) {
    const conteudo = document.getElementById("adminConteudo");
    conteudo.innerHTML = `<p><i class="fa-solid fa-spinner fa-spin"></i> A carregar dados de ${username}...</p>`;

    // Mostra o botão de voltar à lista quando entramos nos detalhes
    const btnVoltar = document.getElementById("btnVoltarAdmin");
    if (btnVoltar) btnVoltar.style.display = "inline-block";

    try {
        const resUser = await fetch(`http://localhost:8080/projeto4/rest/admin/users/${username}`, {
            method: 'GET',
            headers: {"token": token}
        });
        const resClients = await fetch(`http://localhost:8080/projeto4/rest/admin/users/${username}/clients`, {
            method: 'GET',
            headers: {"token": token}
        });
        const resLeads = await fetch(`http://localhost:8080/projeto4/rest/admin/users/${username}/leads`, {
            method: 'GET',
            headers: {"token": token}
        });

        if (resUser.ok && resClients.ok && resLeads.ok) {
            const user = await resUser.json();
            const clientes = await resClients.json();
            const leads = await resLeads.json();

            // --- ORDENAÇÃO ALFABÉTICA (Clientes e Leads) ---
            clientes.sort((a, b) => a.nome.localeCompare(b.nome));
            leads.sort((a, b) => (a.titulo || "").localeCompare(b.titulo || ""));

            clientesAlvoAdmin = clientes;
            leadsAlvoAdmin = leads;

            conteudo.innerHTML = `
                ${gerarHtmlUserCard(user)}
                <div class="cards-container">
                    ${gerarHtmlClientesCard(clientes, user.username)}
                    ${gerarHtmlLeadsCard(leads, user.username)}
                </div>
            `;
        } else {
            conteudo.innerHTML = `<p style="color:red;">Erro ao carregar detalhes do utilizador.</p>`;
        }
    } catch (error) {
        conteudo.innerHTML = `<p style="color:red;">Falha na ligação ao servidor.</p>`;
    }
}

// ==========================================
// 3. COMPONENTES HTML (PARCIAIS)
// ==========================================

function gerarHtmlListaUtilizadores(users) {
    let html = `
        <div class="admin-panel">
            <h3><i class="fa-solid fa-users"></i> Utilizadores Registados (${users.length})</h3>
            <ul class="admin-list">
    `;

    users.forEach(u => {
        const statusBadge = u.ativo ? "" : `<span style="background-color: #d9534f; color: white; padding: 3px 8px; border-radius: 12px; font-size: 11px; margin-left: 10px; font-weight: bold;"><i class="fa-solid fa-ban"></i> Inativo</span>`;
        html += `
            <li class="admin-list-item">
                <div>
                    <strong>${u.primeiroNome} ${u.ultimoNome}</strong> 
                    <span style="color: #888;">(@${u.username})</span>
                    ${u.admin ? '<i class="fa-solid fa-crown" style="color: gold; margin-left: 5px;" title="Administrador"></i>' : ''}
                    ${statusBadge}
                </div>
                <button class="btn" onclick="abrirDetalhesUtilizador('${u.username}')">
                    Gerir <i class="fa-solid fa-arrow-right"></i>
                </button>
            </li>
        `;
    });

    html += `</ul></div>`;
    return html;
}

function gerarHtmlUserCard(user) {
    const btnAtivarInativar = user.ativo
        ? `<button class="btn btn-warning" onclick="apagarUtilizadorAdmin('${user.username}', false)"><i class="fa-solid fa-user-slash"></i> Inativar Conta</button>`
        : `<button class="btn btn-success" onclick="reativarUtilizadorAdmin('${user.username}')"><i class="fa-solid fa-user-check"></i> Reativar Conta</button>`;

    return `
        <div class="user-card">
            <div class="user-card-info">
                <img src="${user.fotoUrl || './imagens/favicon1.png'}" class="user-avatar">
                <div>
                    <h3>${user.primeiroNome} ${user.ultimoNome} ${user.admin ? '<i class="fa-solid fa-crown" style="color: gold;"></i>' : ''}</h3>
                    <p class="user-details-text"><i class="fa-solid fa-at"></i> ${user.username} | <i class="fa-solid fa-envelope"></i> ${user.email} | <i class="fa-solid fa-phone"></i> ${user.telefone}</p>
                </div>
            </div>
            <div class="user-card-actions">
                ${btnAtivarInativar}
                <button class="btn btn-danger" onclick="apagarUtilizadorAdmin('${user.username}', true)"><i class="fa-solid fa-trash"></i> Excluir Conta</button>
            </div>
        </div>
    `;
}

function gerarHtmlClientesCard(clientes, username) {
    return `
        <div class="info-card clientes">
            <div class="card-header">
                <h3><i class="fa-solid fa-briefcase"></i> Clientes (${clientes.length})</h3>
                <div class="action-buttons">
                    <button class="btn btn-success btn-sm" onclick="reativarTodosClientesAdmin('${username}')" title="Reativar Todos"><i class="fa-solid fa-folder-open"></i></button>
                    <button class="btn btn-warning btn-sm" onclick="apagarTodosClientesAdmin('${username}', false)" title="Inativar Todos"><i class="fa-solid fa-ban"></i></button>
                    <button class="btn btn-danger btn-sm" onclick="apagarTodosClientesAdmin('${username}', true)" title="Excluir Definitivamente Todos"><i class="fa-solid fa-fire"></i></button>
                </div>
            </div>
            ${gerarHtmlListaClientesAdmin(clientes, username)}
        </div>
    `;
}

function gerarHtmlListaClientesAdmin(clientes, username) {
    if (clientes.length === 0) return `<p style="color: #888;">Nenhum cliente registado.</p>`;

    let html = `<ul class="admin-list">`;
    clientes.forEach((c, index) => {
        const statusBadge = c.ativo ? "" : `<span style="background-color: #d9534f; color: white; padding: 3px 8px; border-radius: 12px; font-size: 11px; margin-left: 10px; font-weight: bold;"><i class="fa-solid fa-ban"></i> Inativo</span>`;
        const btnAtivarInativarCliente = c.ativo
            ? `<button class="btn btn-warning btn-sm" onclick="apagarClienteAdminBtn(${c.id}, false, '${username}')" title="Inativar Cliente"><i class="fa-solid fa-folder-closed"></i></button>`
            : `<button class="btn btn-success btn-sm" onclick="reativarClienteAdminBtn(${c.id}, '${username}')" title="Reativar Cliente"><i class="fa-solid fa-folder-open"></i></button>`;

        html += `
            <li class="admin-list-item">
                <div>
                    <strong>${c.nome}</strong><br>
                    <span class="item-subtitle"><i class="fa-regular fa-building"></i> ${c.empresa}</span>
                    ${statusBadge}
                </div>
                <div class="action-buttons">
                    <button class="btn btn-sm" style="background-color: #000080; color: white;" onclick="mostrarFormEdicaoClienteAdmin(${index}, '${username}')" title="Editar Cliente"><i class="fa-solid fa-pen"></i></button>
                    ${btnAtivarInativarCliente}
                    <button class="btn btn-danger btn-sm" onclick="apagarClienteAdminBtn(${c.id}, true, '${username}')" title="Excluir Definitivamente"><i class="fa-solid fa-trash"></i></button>
                </div>
            </li>`;
    });
    html += `</ul>`;
    return html;
}

function gerarHtmlLeadsCard(leads, username) {
    return `
        <div class="info-card leads">
            <div class="card-header" style="flex-wrap: wrap; gap: 10px;">
                <h3><i class="fa-solid fa-filter"></i> Leads (<span id="countLeadsAdmin">${leads.length}</span>)</h3>
                
                <select id="filtroLeadsAdmin" style="padding: 5px; border-radius: 4px; border: 1px solid #ccc; font-size: 13px;" onchange="filtrarLeadsAdmin('${username}')">
                    <option value="">Todos os Estados</option>
                    <option value="0">Novo</option>
                    <option value="1">Em análise</option>
                    <option value="2">Proposta</option>
                    <option value="3">Ganho</option>
                    <option value="4">Perdido</option>
                </select>

                <div class="action-buttons">
                    <button class="btn btn-success btn-sm" onclick="reativarTodasLeadsAdmin('${username}')" title="Reativar Todas"><i class="fa-solid fa-folder-open"></i></button>
                    <button class="btn btn-warning btn-sm" onclick="apagarTodasLeadsAdmin('${username}', false)" title="Inativar Todas"><i class="fa-solid fa-ban"></i></button>
                    <button class="btn btn-danger btn-sm" onclick="apagarTodasLeadsAdmin('${username}', true)" title="Excluir Definitivamente Todas"><i class="fa-solid fa-fire"></i></button>
                </div>
            </div>
            
            <div id="containerListaLeadsAdmin">
                ${gerarHtmlListaLeadsAdmin(leads, username)}
            </div>
        </div>
    `;
}

function gerarHtmlListaLeadsAdmin(leadsFiltradas, username) {
    if (leadsFiltradas.length === 0) return `<p style="color: #888; margin-top: 15px;">Nenhuma lead encontrada.</p>`;

    let html = `<ul class="admin-list" style="margin-top: 15px;">`;
    leadsFiltradas.forEach((l) => {
        const statusOptions = ["Novo", "Em análise", "Proposta", "Ganho", "Perdido"];
        const nomeEstado = statusOptions[Number(l.estado)] ?? "Desconhecido";
        const statusBadge = l.ativo ? "" : `<span style="background-color: #d9534f; color: white; padding: 3px 8px; border-radius: 12px; font-size: 11px; margin-left: 10px; font-weight: bold;"><i class="fa-solid fa-ban"></i> Inativo</span>`;

        const btnAtivarInativarLead = l.ativo
            ? `<button class="btn btn-warning btn-sm" onclick="apagarLeadAdminBtn(${l.id}, false, '${username}')" title="Inativar Lead"><i class="fa-solid fa-folder-closed"></i></button>`
            : `<button class="btn btn-success btn-sm" onclick="reativarLeadAdminBtn(${l.id}, '${username}')" title="Reativar Lead"><i class="fa-solid fa-folder-open"></i></button>`;

        html += `
            <li class="admin-list-item">
                <div>
                    <strong>${l.titulo || 'Sem Título'}</strong><br>
                    <span class="item-subtitle"><i class="fa-solid fa-flag"></i> Estado: ${nomeEstado}</span>
                    ${statusBadge}
                </div>
                <div class="action-buttons">
                    <button class="btn btn-sm" style="background-color: #000080; color: white;" onclick="mostrarFormEdicaoLeadAdmin(${l.id}, '${username}')" title="Editar Lead"><i class="fa-solid fa-pen"></i></button>
                    ${btnAtivarInativarLead}
                    <button class="btn btn-danger btn-sm" onclick="apagarLeadAdminBtn(${l.id}, true, '${username}')" title="Excluir Lead"><i class="fa-solid fa-trash"></i></button>
                </div>
            </li>`;
    });
    html += `</ul>`;
    return html;
}

// ==========================================
// 4. FUNÇÕES DE COMUNICAÇÃO (FETCH)
// ==========================================

// --- UTILIZADORES ---
async function apagarUtilizadorAdmin(username, permanente) {
    if (!confirm(`Tens a certeza que queres ${permanente ? 'EXCLUIR DEFINITIVAMENTE' : 'inativar'} o utilizador @${username}?`)) return;
    try {
        let url = `http://localhost:8080/projeto4/rest/admin/users/${username}` + (permanente ? "?permanente=true" : "");
        const response = await fetch(url, {method: 'DELETE', headers: {"token": token}});
        alert(await response.text());
        permanente ? await carregarListaUtilizadores() : await abrirDetalhesUtilizador(username);
    } catch (error) {
        alert("Falha na comunicação.");
    }
}

async function reativarUtilizadorAdmin(username) {
    if (!confirm(`Tens a certeza que queres reativar o utilizador @${username}?`)) return;
    try {
        const response = await fetch(`http://localhost:8080/projeto4/rest/admin/users/${username}/reactivate`, {
            method: 'PATCH',
            headers: {"token": token}
        });
        alert(await response.text());
        await abrirDetalhesUtilizador(username);
    } catch (error) {
        alert("Falha na comunicação.");
    }
}

// --- CLIENTES ---
async function apagarTodosClientesAdmin(username, permanente) {
    const acao = permanente ? 'EXCLUIR DEFINITIVAMENTE' : 'inativar';
    if (!confirm(`Tens a certeza que queres ${acao} TODOS os clientes de @${username}?`)) return;
    try {
        let url = `http://localhost:8080/projeto4/rest/admin/users/${username}/clients` + (permanente ? "?permanente=true" : "");
        const response = await fetch(url, {method: 'DELETE', headers: {"token": token}});
        alert(await response.text());
        await abrirDetalhesUtilizador(username);
    } catch (error) {
        alert("Falha na comunicação.");
    }
}

async function reativarTodosClientesAdmin(username) {
    if (!confirm(`Tens a certeza que queres reativar TODOS os clientes inativos de @${username}?`)) return;
    try {
        let url = `http://localhost:8080/projeto4/rest/admin/users/${username}/clients/reactivate`;
        const response = await fetch(url, {method: 'PATCH', headers: {"token": token}});
        alert(await response.text());
        await abrirDetalhesUtilizador(username);
    } catch (error) {
        alert("Falha na comunicação.");
    }
}

async function apagarClienteAdminBtn(idCliente, permanente, usernameToRefresh) {
    const acao = permanente ? 'Excluir definitivamente' : 'Inativar';
    if (!confirm(`${acao} este Cliente?`)) return;
    try {
        let url = `http://localhost:8080/projeto4/rest/admin/clients/${idCliente}` + (permanente ? "?permanente=true" : "");
        const response = await fetch(url, {method: 'DELETE', headers: {"token": token}});
        alert(await response.text());
        await abrirDetalhesUtilizador(usernameToRefresh);
    } catch (error) {
        alert("Falha na comunicação.");
    }
}

async function reativarClienteAdminBtn(idCliente, usernameToRefresh) {
    if (!confirm(`Tens a certeza que queres reativar este cliente?`)) return;
    try {
        const response = await fetch(`http://localhost:8080/projeto4/rest/admin/clients/${idCliente}/reactivate`, {
            method: 'PATCH',
            headers: {"token": token}
        });
        alert(await response.text());
        await abrirDetalhesUtilizador(usernameToRefresh);
    } catch (error) {
        alert("Falha na comunicação.");
    }
}

// --- LEADS ---
function filtrarLeadsAdmin(username) {
    const filtroValor = document.getElementById("filtroLeadsAdmin").value;
    let leadsParaMostrar = leadsAlvoAdmin;
    if (filtroValor !== "") leadsParaMostrar = leadsAlvoAdmin.filter(l => Number(l.estado) === Number(filtroValor));
    document.getElementById("countLeadsAdmin").innerText = leadsParaMostrar.length;
    document.getElementById("containerListaLeadsAdmin").innerHTML = gerarHtmlListaLeadsAdmin(leadsParaMostrar, username);
}

async function apagarTodasLeadsAdmin(username, permanente) {
    const acao = permanente ? 'EXCLUIR DEFINITIVAMENTE' : 'inativar';
    if (!confirm(`Tens a certeza que queres ${acao} TODAS as leads de @${username}?`)) return;
    try {
        let url = `http://localhost:8080/projeto4/rest/admin/users/${username}/leads` + (permanente ? "?permanente=true" : "");
        const response = await fetch(url, {method: 'DELETE', headers: {"token": token}});
        alert(await response.text());
        await abrirDetalhesUtilizador(username);
    } catch (error) {
        alert("Falha na comunicação.");
    }
}

async function reativarTodasLeadsAdmin(username) {
    if (!confirm(`Tens a certeza que queres reativar TODAS as leads inativas de @${username}?`)) return;
    try {
        let url = `http://localhost:8080/projeto4/rest/admin/users/${username}/leads/reactivate`;
        const response = await fetch(url, {method: 'PATCH', headers: {"token": token}});
        alert(await response.text());
        await abrirDetalhesUtilizador(username);
    } catch (error) {
        alert("Falha na comunicação.");
    }
}

async function apagarLeadAdminBtn(idLead, permanente, usernameToRefresh) {
    const acao = permanente ? 'Excluir definitivamente' : 'Inativar';
    if (!confirm(`${acao} esta Lead?`)) return;
    try {
        let url = `http://localhost:8080/projeto4/rest/admin/leads/${idLead}` + (permanente ? "?permanente=true" : "");
        const response = await fetch(url, {method: 'DELETE', headers: {"token": token}});
        alert(await response.text());
        await abrirDetalhesUtilizador(usernameToRefresh);
    } catch (error) {
        alert("Falha na comunicação.");
    }
}

async function reativarLeadAdminBtn(idLead, usernameToRefresh) {
    if (!confirm(`Tens a certeza que queres reativar esta Lead?`)) return;
    try {
        const response = await fetch(`http://localhost:8080/projeto4/rest/admin/leads/${idLead}/reactivate`, {
            method: 'PATCH',
            headers: {"token": token}
        });
        alert(await response.text());
        await abrirDetalhesUtilizador(usernameToRefresh);
    } catch (error) {
        alert("Falha na comunicação.");
    }
}

// ==========================================
// 5. FORMULÁRIOS DE EDIÇÃO
// ==========================================

function mostrarFormEdicaoClienteAdmin(index, username) {
    const c = clientesAlvoAdmin[index];
    const conteudo = document.getElementById("adminConteudo");

    conteudo.innerHTML = `
        <button class="btn" style="margin-bottom: 20px;" onclick="abrirDetalhesUtilizador('${username}')"><i class="fa-solid fa-arrow-left"></i> Voltar ao perfil</button>
        <div class="edit-form-card">
            <h3 style="margin-top:0;"><i class="fa-solid fa-pen"></i> Editar Cliente</h3>
            <p style="color: #666; margin-bottom: 20px; font-size: 14px;">A alterar dados de <strong>@${username}</strong>.</p>
            <label class="form-label">Nome</label> <input id="editAdminCliNome" type="text" value="${c.nome}" class="form-input">
            <label class="form-label">Email</label> <input id="editAdminCliEmail" type="email" value="${c.email || ''}" class="form-input">
            <label class="form-label">Telefone</label> <input id="editAdminCliTelefone" type="text" value="${c.telefone || ''}" class="form-input">
            <label class="form-label">Empresa</label> <input id="editAdminCliEmpresa" type="text" value="${c.empresa}" class="form-input" style="margin-bottom: 25px;">
            <button class="btn btn-success btn-block" onclick="guardarEdicaoClienteAdmin(${c.id}, '${username}')"><i class="fa-solid fa-floppy-disk"></i> Guardar Alterações</button>
        </div>
    `;
}

async function guardarEdicaoClienteAdmin(idCliente, username) {
    const nome = document.getElementById("editAdminCliNome").value.trim();
    const email = document.getElementById("editAdminCliEmail").value.trim();
    const telefone = document.getElementById("editAdminCliTelefone").value.trim();
    const empresa = document.getElementById("editAdminCliEmpresa").value.trim();

    if (!nome || !empresa || (!email && !telefone)) {
        alert("Nome, Empresa e pelo menos um contacto são obrigatórios.");
        return;
    }
    const dados = {id: idCliente, nome, email, telefone, empresa};
    try {
        const response = await fetch(`http://localhost:8080/projeto4/rest/admin/clients/${idCliente}`, {
            method: 'PATCH', headers: {"Content-Type": "application/json", "token": token}, body: JSON.stringify(dados)
        });
        const msg = await response.text();
        if (response.ok) {
            alert("Sucesso: " + msg);
            await abrirDetalhesUtilizador(username);
        } else {
            alert("Erro: " + msg);
        }
    } catch (error) {
        alert("Falha na comunicação.");
    }
}

function mostrarFormEdicaoLeadAdmin(idLead, username) {
    const l = leadsAlvoAdmin.find(lead => lead.id === idLead);
    if (!l) return;
    const conteudo = document.getElementById("adminConteudo");
    const statusOptions = ["Novo", "Em análise", "Proposta", "Ganho", "Perdido"];
    let opcoesHtml = statusOptions.map((s, idx) => `<option value="${idx}" ${Number(l.estado) === idx ? "selected" : ""}>${s}</option>`).join("");

    conteudo.innerHTML = `
        <button class="btn" style="margin-bottom: 20px;" onclick="abrirDetalhesUtilizador('${username}')"><i class="fa-solid fa-arrow-left"></i> Voltar ao perfil</button>
        <div class="edit-form-card" style="border-top: 4px solid #5cb85c;">
            <h3 style="margin-top:0;"><i class="fa-solid fa-pen"></i> Editar Lead</h3>
            <p style="color: #666; margin-bottom: 20px; font-size: 14px;">A alterar dados de <strong>@${username}</strong>.</p>
            <label class="form-label">Título</label> <input id="editAdminLeadTitulo" type="text" value="${l.titulo}" class="form-input">
            <label class="form-label">Descrição</label> <textarea id="editAdminLeadDescricao" class="form-input" style="min-height: 80px;">${l.descricao}</textarea>
            <label class="form-label">Estado</label> <select id="editAdminLeadEstado" class="form-input" style="margin-bottom: 25px;">${opcoesHtml}</select>
            <button class="btn btn-success btn-block" onclick="guardarEdicaoLeadAdmin(${l.id}, '${username}')"><i class="fa-solid fa-floppy-disk"></i> Guardar Alterações</button>
        </div>
    `;
}

async function guardarEdicaoLeadAdmin(idLead, username) {
    const titulo = document.getElementById("editAdminLeadTitulo").value.trim();
    const descricao = document.getElementById("editAdminLeadDescricao").value.trim();
    const estado = parseInt(document.getElementById("editAdminLeadEstado").value);

    if (!titulo || !descricao) {
        alert("Título e Descrição são obrigatórios.");
        return;
    }
    const dados = {id: idLead, titulo, descricao, estado};
    try {
        const response = await fetch(`http://localhost:8080/projeto4/rest/admin/leads/${idLead}`, {
            method: 'PATCH', headers: {"Content-Type": "application/json", "token": token}, body: JSON.stringify(dados)
        });
        const msg = await response.text();
        if (response.ok) {
            alert("Sucesso: " + msg);
            await abrirDetalhesUtilizador(username);
        } else {
            alert("Erro: " + msg);
        }
    } catch (error) {
        alert("Falha na comunicação.");
    }
}