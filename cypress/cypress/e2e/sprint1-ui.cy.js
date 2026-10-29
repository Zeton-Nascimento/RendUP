const API = "**/api/v1";

const investor = {
  id: "investor-qa-001",
  nome: "Ana Souza",
  email: "ana.souza@example.test",
  ativo: true,
  perfilUsuario: "INVESTIDOR",
  perfilInvestidor: "CONSERVADOR",
  cpf: "12345678901",
  telefone: "11987654321",
  dataNascimento: "1990-05-10",
  banco: "Banco Teste",
  agencia: "1234",
  numeroConta: "1234567",
  permissoes: ["ROLE_INVESTIDOR"],
};

const admin = {
  id: "admin-qa-001",
  nome: "Admin QA",
  email: "admin.qa@example.test",
  ativo: true,
  perfilUsuario: "ADMIN",
  perfilInvestidor: "CONSERVADOR",
  cpf: "98765432100",
  telefone: "11912345678",
  dataNascimento: "1985-02-20",
  banco: "Banco Teste",
  agencia: "4321",
  numeroConta: "7654321",
  permissoes: ["ROLE_ADMIN"],
};

function tokenFor(user) {
  const payload = {
    sub: user.email,
    email: user.email,
    nome: user.nome,
    perfilUsuario: user.perfilUsuario,
    perfilInvestidor: user.perfilInvestidor,
    permissoes: user.permissoes,
    authorities: user.permissoes,
    exp: Math.floor(Date.now() / 1000) + 3600,
  };
  return `e30.${btoa(JSON.stringify(payload))}.cypress-test-signature`;
}

function visitWithUser(path, user) {
  cy.visit(path, {
    onBeforeLoad(win) {
      win.localStorage.setItem("access_token", tokenFor(user));
    },
  });
}

function stubOwnProfile(user = investor) {
  cy.intercept("GET", `${API}/usuarios/me`, {
    statusCode: 200,
    body: user,
  }).as("getOwnProfile");
}

function fillValidRegistration(email = "qa.investidor@example.test") {
  cy.get("#registro-nome").type("Beatriz QA");
  cy.get("#registro-email").type(email);
  cy.get("#registro-senha").type("SenhaTeste123");
  cy.get("#registro-cpf").type("32165498700");
  cy.get("#registro-telefone").type("11999998888");
  cy.get("#registro-data-nascimento").type("1992-08-15");
  cy.get("#registro-banco").type("Banco Teste");
  cy.get("#registro-agencia").type("1234");
  cy.get("#registro-numero-conta").type("1234567");
  cy.get("#registro-perfil").select("MODERADO");
}

describe("RendUP — Sprint 1 — E2E de interface (API simulada)", () => {
  beforeEach(() => {
    cy.clearCookies();
    cy.clearLocalStorage();
  });

  it("CT-UI-001 / US-006 — redireciona visitante de /perfil para login", () => {
    cy.visit("/perfil");
    cy.location("pathname").should("eq", "/login");
    cy.location("search").should("contain", "returnUrl");
  });

  it("CT-UI-002 / US-008 — autentica investidor e abre o próprio perfil", () => {
    const accessToken = tokenFor(investor);
    cy.intercept("POST", `${API}/auth/login`, {
      statusCode: 200,
      body: { accessToken, tipo: "Bearer", usuario: investor, expiresIn: 3600 },
    }).as("login");
    stubOwnProfile(investor);

    cy.visit("/login");
    cy.get("#login-email").type(investor.email);
    cy.get("#login-senha").type("SenhaTeste123");
    cy.get("#login-submit").click();

    cy.wait("@login").its("request.body").should("deep.equal", {
      email: investor.email,
      senha: "SenhaTeste123",
    });
    cy.wait("@getOwnProfile");
    cy.location("pathname").should("eq", "/perfil");
    cy.contains("h1", "Meu perfil").should("be.visible");
    cy.contains("Ana Souza").should("be.visible");
    cy.window().its("localStorage").invoke("getItem", "access_token").should("equal", accessToken);
    cy.reload();
    cy.wait("@getOwnProfile");
    cy.contains("Ana Souza").should("be.visible");
  });

  it("CT-UI-003 / US-008 — mostra erro compreensível para login inválido", () => {
    cy.intercept("POST", `${API}/auth/login`, {
      statusCode: 401,
      body: { erro: "Usuário ou senha incorretos." },
    }).as("loginInvalid");

    cy.visit("/login");
    cy.get("#login-email").type("nao.existe@example.test");
    cy.get("#login-senha").type("SenhaErrada123");
    cy.get("#login-submit").click();

    cy.wait("@loginInvalid");
    cy.get('[role="alert"]').should("contain.text", "Usuário ou senha incorretos.");
    cy.location("pathname").should("eq", "/login");
    cy.window().its("localStorage").invoke("getItem", "access_token").should("be.null");
  });

  it("CT-UI-004 / US-001 e US-007 — cadastra investidor e não exibe credenciais na resposta", () => {
    cy.intercept("POST", `${API}/auth/registrar`, (req) => {
      expect(req.body).to.include({
        email: "qa.investidor@example.test",
        perfilInvestidor: "MODERADO",
      });
      expect(req.body.senha).to.equal("SenhaTeste123");
      req.reply({
        statusCode: 201,
        body: {
          ...investor,
          id: "new-investor-001",
          nome: "Beatriz QA",
          email: "qa.investidor@example.test",
          perfilInvestidor: "MODERADO",
        },
      });
    }).as("register");

    cy.visit("/registro");
    fillValidRegistration();
    cy.get("#registro-submit").click();
    cy.wait("@register").then(({ response }) => {
      expect(response.statusCode).to.equal(201);
      const serialized = JSON.stringify(response.body).toLowerCase();
      expect(serialized).not.to.include('"senha"');
      expect(serialized).not.to.include("senhahash");
      expect(serialized).not.to.include('"password"');
      expect(serialized).not.to.include('"accesstoken"');
    });
    cy.get('[role="status"]').should("contain.text", "Conta criada");
    cy.get("#registro-success-login").click();
    cy.location("pathname").should("eq", "/login");
  });

  it("CT-UI-005 / US-001 — impede envio de cadastro com campos obrigatórios vazios", () => {
    cy.intercept("POST", `${API}/auth/registrar`).as("register");
    cy.visit("/registro");
    cy.get("#registro-submit").click();
    cy.contains("Campo obrigatório.").should("be.visible");
    cy.get("@register.all").should("have.length", 0);
  });

  it("CT-UI-006 / US-001 e US-005 — apresenta mensagem quando o e-mail já está cadastrado", () => {
    cy.intercept("POST", `${API}/auth/registrar`, {
      statusCode: 422,
      body: { erro: "Este e-mail já está sendo usado." },
    }).as("duplicateEmail");

    cy.visit("/registro");
    fillValidRegistration("ja.cadastrado@example.test");
    cy.get("#registro-submit").click();
    cy.wait("@duplicateEmail");
    cy.get('[role="alert"]').should("contain.text", "Este e-mail já está em uso.");
  });

  it("CT-UI-007 / US-002 — atualiza somente os campos permitidos do próprio perfil", () => {
    const updated = {
      ...investor,
      nome: "Ana Souza Atualizada",
      telefone: "11912345678",
      dataNascimento: "1991-06-12",
      perfilInvestidor: "MODERADO",
    };
    stubOwnProfile(investor);
    cy.intercept("PUT", `${API}/usuarios/me`, (req) => {
      expect(Object.keys(req.body).sort()).to.deep.equal([
        "dataNascimento",
        "nome",
        "perfilInvestidor",
        "telefone",
      ]);
      expect(req.body).to.deep.equal({
        nome: "Ana Souza Atualizada",
        telefone: "11912345678",
        dataNascimento: "1991-06-12",
        perfilInvestidor: "MODERADO",
      });
      req.reply({ statusCode: 200, body: updated });
    }).as("updateOwnProfile");

    visitWithUser("/perfil", investor);
    cy.wait("@getOwnProfile");
    cy.contains("button", "Editar perfil").click();
    cy.contains("label", "Nome completo").find("input").clear().type("Ana Souza Atualizada");
    cy.contains("label", "Telefone").find("input").clear().type("(11) 91234-5678");
    cy.contains("label", "Data de nascimento").find("input").clear().type("1991-06-12");
    cy.contains("label", "Perfil do investidor").find("select").select("MODERADO");
    cy.contains("button", "Salvar alterações").click();

    cy.wait("@updateOwnProfile");
    cy.get('[role="status"]').should("contain.text", "Dados atualizados com sucesso.");
    cy.contains("Ana Souza Atualizada").should("be.visible");
    cy.contains("E-mail").parent().should("contain.text", investor.email);
  });

  it("CT-UI-008 / US-009 — rejeita confirmação de senha diferente sem chamar a API", () => {
    stubOwnProfile(investor);
    cy.intercept("PUT", `${API}/usuarios/me/senha`).as("changePassword");
    visitWithUser("/perfil", investor);
    cy.wait("@getOwnProfile");
    cy.contains("button", "Alterar senha").click();
    cy.contains("label", "Senha atual").find("input").type("SenhaAtual123");
    cy.contains("label", "Nova senha").find("input").type("SenhaNova123");
    cy.contains("label", "Confirmar nova senha").find("input").type("OutraSenha123");
    cy.contains("button", "Alterar senha").click();
    cy.get('[role="alert"]').should("contain.text", "As senhas novas precisam ser iguais.");
    cy.get("@changePassword.all").should("have.length", 0);
  });

  it("CT-UI-009 / US-009 e US-007 — envia apenas senha atual e nova senha", () => {
    stubOwnProfile(investor);
    cy.intercept("PUT", `${API}/usuarios/me/senha`, (req) => {
      expect(Object.keys(req.body).sort()).to.deep.equal(["novaSenha", "senhaAtual"]);
      expect(req.body).to.deep.equal({ senhaAtual: "SenhaAtual123", novaSenha: "SenhaNova123" });
      req.reply({ statusCode: 204, body: "" });
    }).as("changePassword");

    visitWithUser("/perfil", investor);
    cy.wait("@getOwnProfile");
    cy.contains("button", "Alterar senha").click();
    cy.contains("label", "Senha atual").find("input").type("SenhaAtual123");
    cy.contains("label", "Nova senha").find("input").type("SenhaNova123");
    cy.contains("label", "Confirmar nova senha").find("input").type("SenhaNova123");
    cy.contains("button", "Alterar senha").click();
    cy.wait("@changePassword");
    cy.get('[role="status"]').should("contain.text", "Senha alterada com sucesso");
  });

  it("CT-UI-010 / US-006 e US-010 — investidor não acessa a rota administrativa", () => {
    stubOwnProfile(investor);
    visitWithUser("/admin/usuarios", investor);
    cy.location("pathname").should("eq", "/perfil");
    cy.contains("h1", "Meu perfil").should("be.visible");
  });

  it("CT-UI-011 / US-004 e US-007 — administrador consulta usuário sem exibir CPF completo ou senha", () => {
    const inactive = { ...investor, ativo: false };
    cy.intercept("GET", `${API}/usuarios`, {
      statusCode: 200,
      body: [admin, investor],
    }).as("listUsers");
    cy.intercept("GET", `${API}/usuarios/${investor.id}`, {
      statusCode: 200,
      body: investor,
    }).as("getUserById");

    visitWithUser("/admin/usuarios", admin);
    cy.wait("@listUsers");
    cy.contains(".user-row", investor.nome).contains("button", "Consultar").click();
    cy.wait("@getUserById");

    cy.get("#user-detail-title").should("be.visible");
    cy.get(".user-detail-card").should("contain.text", "***.***.789-01");
    cy.get(".user-detail-card").should("not.contain.text", investor.cpf);
    cy.get(".user-detail-card").should("not.contain.text", "SenhaTeste123");
  });

  it("CT-UI-012 / US-003 — administrador confirma inativação e a interface atualiza o status", () => {
    cy.intercept("GET", `${API}/usuarios`, {
      statusCode: 200,
      body: [admin, investor],
    }).as("listUsers");
    cy.intercept("DELETE", `${API}/usuarios/${investor.id}*`, (req) => {
      expect(req.url).to.include("confirmar=true");
      req.reply({ statusCode: 200, body: { ...investor, ativo: false } });
    }).as("deactivateUser");
    cy.on("window:confirm", (message) => {
      expect(message).to.contain("Deseja inativar o usuário Ana Souza?");
      return true;
    });

    visitWithUser("/admin/usuarios", admin);
    cy.wait("@listUsers");
    cy.contains(".user-row", investor.nome).contains("button", "Inativar").click();
    cy.wait("@deactivateUser");
    cy.contains(".user-row", investor.nome).should("contain.text", "Inativo");
    cy.get('[role="status"]').should("contain.text", "Usuário inativado com sucesso.");
  });

  it("CT-UI-013 / US-003 — cancelar a confirmação não executa inativação", () => {
    cy.intercept("GET", `${API}/usuarios`, {
      statusCode: 200,
      body: [admin, investor],
    }).as("listUsers");
    cy.intercept("DELETE", `${API}/usuarios/${investor.id}*`).as("deactivateUser");
    cy.on("window:confirm", () => false);

    visitWithUser("/admin/usuarios", admin);
    cy.wait("@listUsers");
    cy.contains(".user-row", investor.nome).contains("button", "Inativar").click();
    cy.get("@deactivateUser.all").should("have.length", 0);
    cy.contains(".user-row", investor.nome).should("contain.text", "Ativo");
  });

  it("CT-UI-014 / US-005 — permite tentar carregar o perfil novamente após falha da API", () => {
    let attempts = 0;
    cy.intercept("GET", `${API}/usuarios/me`, (req) => {
      attempts += 1;
      if (attempts === 1) {
        req.reply({ statusCode: 500, body: { erro: "Erro interno do servidor." } });
      } else {
        req.reply({ statusCode: 200, body: investor });
      }
    }).as("ownProfileRetry");

    visitWithUser("/perfil", investor);
    cy.wait("@ownProfileRetry");
    cy.get('[role="alert"]').should("contain.text", "Não foi possível carregar seu perfil.");
    cy.contains("button", "Tentar novamente").click();
    cy.wait("@ownProfileRetry");
    cy.contains("h1", "Meu perfil").should("be.visible");
    cy.contains("Ana Souza").should("be.visible");
  });

  it("CT-UI-015 / US-005 — apresenta estado vazio na listagem administrativa", () => {
    cy.intercept("GET", `${API}/usuarios`, { statusCode: 200, body: [] }).as("emptyUsers");
    visitWithUser("/admin/usuarios", admin);
    cy.wait("@emptyUsers");
    cy.contains("Nenhum usuário encontrado.").should("be.visible");
  });
});
