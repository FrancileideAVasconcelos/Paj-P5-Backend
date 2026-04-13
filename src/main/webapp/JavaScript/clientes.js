const API_URL = "http://localhost:8080/projeto4/rest/clients";
let clienteList = [];

// ==========================================
// 1. CARREGAR E LISTAR
// ==========================================

async function carregarClientes() {
    const token = localStorage.getItem("token");

    if (!token) {
        alert("Sessão expirada ou utilizador não autenticado.");
        return 
    }

    try {
        const response = await fetch(API_URL, {
            method: "GET",
            headers: { "token": token }
        });

        if (response.ok) {
            clienteList = await response.json(); 
            clienteList.sort((a, b) => a.nome.localeCompare(b.nome));
            listarClientes(); // Substitui o ecrã pela lista
        } else {
            alert("Erro ao ir buscar clientes: " + await response.text());
        }
    } catch (error) {
        console.error("Erro na ligação ao servidor:", error);
    }
}

function listarClientes() {
    const main = document.getElementById("content"); 
    if (!main) return;

    let html = `
        <div class="barra-clientes">
            <h2>Clientes</h2>
            <button class="btn" type="button"onclick="formNovoCliente()"><i class="fa-solid fa-user-plus"></i>Novo Cliente</button>
        </div>
        <!-- lista não ordenada de clientes -->
        <ul id="listaClientes"></ul> 
        <br>
    `;

    if (clienteList.length === 0) {
        html += `<p>Nenhum cliente guardado.</p>`;
    } else {
        html += clienteList.map((c, i) => `
            <li class="cliente-item">
                <button class="cliente-item-btn" onclick="mostrarDetalhesCliente(${i})">
                    <strong>${c.nome}</strong> - ${c.empresa}
                </button>
            </li>`).join("");
    }

    html += `</ul>`;
    main.innerHTML = html; 
}

// ==========================================
// 2. MOSTRAR DETALHES
// ==========================================

function mostrarDetalhesCliente(index) {
    const c = clienteList[index];
    const main = document.getElementById("content");

    // Injetamos os detalhes diretamente no content (não precisa mudar de página HTML)
    main.innerHTML = `
        <div class="detalhes-container">
            <h2>Detalhes do Cliente</h2>
            <br>
            <p><strong>Nome:</strong> ${c.nome}</p>
            <p><strong>Email:</strong> ${c.email}</p>
            <p><strong>Telefone:</strong> ${c.telefone}</p>
            <p><strong>Empresa:</strong> ${c.empresa}</p>
            <br>
            <button class="btn" onclick="editarCliente(${index})"><i class="fa-regular fa-pen-to-square"></i>Editar</button>
            <button class="btn" onclick="removerCliente(${index})"><i class="fa-solid fa-trash"></i>Remover</button>
            <button class="btn" onclick="carregarClientes()"><i class="fa-solid fa-arrow-left"></i>Voltar</button>
        </div>
    `;
}

// ==========================================
// 3. FORMULÁRIOS (NOVO E EDITAR)
// ==========================================

function formNovoCliente() {
    const content = document.getElementById("content");
    content.innerHTML = `
        <h2>Novo Cliente</h2>
        <label>Nome</label> <input id="clienteNome" type="text" required><br><br>
        <label>Email</label> <input id="clienteEmail" type="email"><br><br>
        <label>Telefone</label> <input id="clienteTelefone" type="text"><br><br>
        <label>Empresa</label> <input id="clienteEmpresa" type="text" required><br><br>
        
        <button id="btnGuardarCliente" class="btn" disabled type="button" onclick="guardarCliente()">
            <i class="fa-solid fa-floppy-disk"></i>Guardar
        </button>
        <button class="btn" type="button" onclick="carregarClientes()">
            <i class="fa-solid fa-xmark"></i>Cancelar
        </button>
    `;
    ativarValidacaoNovoCliente();
}

function editarCliente(index) {
    const c = clienteList[index];
    const main = document.getElementById("content");
    
    main.innerHTML = `
        <h2>Editar Cliente</h2> 
        <label>Nome</label> <input id="clienteNome" type="text" value="${c.nome}"> <br><br>
        <label>Email</label> <input id="clienteEmail" type="email" value="${c.email}"> <br><br>
        <label>Telefone</label> <input id="clienteTelefone" type="text" value="${c.telefone}"> <br><br>
        <label>Empresa</label> <input id="clienteEmpresa" type="text" value="${c.empresa}"> <br><br>
        
        <button id="btnGuardarClienteEdicao" class="btn" disabled type="button" onclick="guardarCliente(${index})">
            <i class="fa-solid fa-floppy-disk"></i>Guardar
        </button>
        <button class="btn" onclick="mostrarDetalhesCliente(${index})">
            <i class="fa-solid fa-xmark"></i>Cancelar
        </button>
    `;
    
    ativarValidacaoEdicaoCliente(c);
}

// ==========================================
// 4. CRUD (GUARDAR E REMOVER)
// ==========================================

async function guardarCliente(index = null) {
    const nome = document.getElementById("clienteNome").value.trim();
    const email = document.getElementById("clienteEmail").value.trim();
    const telefone = document.getElementById("clienteTelefone").value.trim();
    const empresa = document.getElementById("clienteEmpresa").value.trim();

    if (nome === "" || empresa === "" || (email === "" && telefone === "")) return;
    if (telefone !== "" && !/^[29][0-9]{8}$/.test(telefone)) { alert("Telefone inválido."); return; }
    if (email !== "" && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) { alert("Email inválido."); return; }

    const dados = { nome, email, telefone, empresa };
    const token = localStorage.getItem("token");

    if (!token) {
        alert("Sessão expirada ou utilizador não autenticado.");
        return;
    }

    let response;

    try {
        if (index === null) {
            // POST: Novo Cliente
            response = await fetch(`${API_URL}/add`, {
                method: "POST",
                headers: { "Content-Type": "application/json", "token": token },
                body: JSON.stringify(dados)
            });
        } else {
            // PUT: Editar Cliente
            dados.id = clienteList[index].id; // IMPORTANTE: O backend precisa do ID dentro do DTO

            console.log("dados.id: ", dados.id);
            
            response = await fetch(`${API_URL}/edit`, {
                method: "PATCH",
                headers: { "Content-Type": "application/json", "token": token },
                body: JSON.stringify(dados)
            });
        }

        console.log("response: ", response);
        if (response.ok) {
            alert(index === null ? "Cliente adicionado com sucesso!" : "Cliente atualizado com sucesso!");
            await carregarClientes(); // Limpa e volta à lista
        } else {
            alert("Erro do Java: " + await response.text());
        }
    } catch (error) {
        console.error("Falha ao guardar:", error);
    }
}

async function removerCliente(index) {
    if (!confirm("Tem a certeza que deseja remover este cliente?")) return;
    
    const idGlobal = clienteList[index].id; 
    const token = localStorage.getItem("token");

    if (!token) {
        alert("Sessão expirada ou utilizador não autenticado.");
        return;
    }

    // Construir um objeto com o ID para enviar no corpo do DELETE
    const dados = { id: idGlobal };

    try {
        const response = await fetch(`${API_URL}/remove`, {
            method: "DELETE",
            headers: { "Content-Type": "application/json", "token": token},
            body: JSON.stringify( dados ) // O backend espera um JSON com o ID para remover
        });

        if (response.ok) {
            alert("Cliente removido!");
            await carregarClientes(); // Recarrega a lista
        } else {
            alert("Erro ao remover: " + await response.text());
        }
    } catch (error) {
        console.error("Erro na ligação:", error);
    }
}

// ==========================================
// 5. VALIDAÇÕES
// ==========================================

function ativarValidacaoNovoCliente() {
    const nome = document.getElementById("clienteNome");
    const email = document.getElementById("clienteEmail");
    const telefone = document.getElementById("clienteTelefone");
    const empresa = document.getElementById("clienteEmpresa");
    const btn = document.getElementById("btnGuardarCliente");

    if (!nome || !btn) return;

    const validar = () => {
        const nomeOk = nome.value.trim() !== "";
        const empresaOk = empresa.value.trim() !== "";
        const contactoOk = email.value.trim() !== "" || telefone.value.trim() !== "";
        btn.disabled = !(nomeOk && empresaOk && contactoOk);
    };

    nome.addEventListener("input", validar);
    email.addEventListener("input", validar);
    telefone.addEventListener("input", validar);
    empresa.addEventListener("input", validar);
    validar();
}

function ativarValidacaoEdicaoCliente(clienteOriginal) {
    const nome = document.getElementById("clienteNome");
    const email = document.getElementById("clienteEmail");
    const telefone = document.getElementById("clienteTelefone");
    const empresa = document.getElementById("clienteEmpresa");
    const btn = document.getElementById("btnGuardarClienteEdicao");

    if (!nome || !btn) return;

    const validar = () => {
        const nomeVal = nome.value.trim();
        const emailVal = email.value.trim();
        const telefoneVal = telefone.value.trim();
        const empresaVal = empresa.value.trim();

        const preenchido = nomeVal !== "" && empresaVal !== "" && (emailVal !== "" || telefoneVal !== "");
        const mudou = nomeVal !== clienteOriginal.nome || emailVal !== clienteOriginal.email || 
                      telefoneVal !== clienteOriginal.telefone || empresaVal !== clienteOriginal.empresa;

        btn.disabled = !(preenchido && mudou);
    };

    nome.addEventListener("input", validar);
    email.addEventListener("input", validar);
    telefone.addEventListener("input", validar);
    empresa.addEventListener("input", validar);
    validar();
}