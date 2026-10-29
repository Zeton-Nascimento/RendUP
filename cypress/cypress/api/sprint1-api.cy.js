function makeUser() {
  const unique = `${Date.now()}${Math.floor(Math.random() * 10000)}`;
  const digits = unique.replace(/\D/g, "");
  return {
    nome: "Usuário de Teste Cypress",
    email: `qa-rendup-${unique}@example.test`,
    senha: "SenhaTeste123",
    cpf: digits.slice(-11).padStart(11, "0"),
    telefone: "11987654321",
    dataNascimento: "1990-01-10",
    banco: "Banco QA",
    agencia: "1234",
    numeroConta: digits.slice(-7).padStart(7, "0"),
    perfilInvestidor: "CONSERVADOR",
  };
}

function register(user) {
  return cy.request({
    method: "POST",
    url: "/api/v1/auth/registrar",
    body: user,
    failOnStatusCode: false,
  });
}

function login(user, failOnStatusCode = false) {
  return cy.request({
    method: "POST",
    url: "/api/v1/auth/login",
    body: { email: user.email, senha: user.senha },
    failOnStatusCode,
  });
}

function withToken(method, url, token, body) {
  return cy.request({
    method,
    url,
    body,
    headers: { Authorization: `Bearer ${token}` },
    failOnStatusCode: false,
  });
}

function registerAndLogin() {
  const user = makeUser();
  return register(user).then((registration) => {
    expect(registration.status).to.equal(201);
    return login(user, true).then((auth) => {
      expect(auth.status).to.equal(200);
      expect(auth.body.accessToken).to.be.a("string").and.not.be.empty;
      return { user, userId: registration.body.id, token: auth.body.accessToken };
    });
  });
}

describe("RendUP — Sprint 1 — API real (use apenas banco local/de teste)", () => {
  it("CT-API-001 / US-006 — rejeita consulta protegida sem token com HTTP 401", () => {
    cy.request({
      method: "GET",
      url: "/api/v1/usuarios/me",
      failOnStatusCode: false,
    }).its("status").should("equal", 401);
  });

  it("CT-API-002 / US-001 e US-007 — cadastro cria investidor sem retornar senha/hash/token", () => {
    const user = makeUser();
    register(user).then((response) => {
      expect(response.status).to.equal(201);
      expect(response.body.email).to.equal(user.email);
      expect(response.body.perfilUsuario).to.equal("INVESTIDOR");
      expect(response.body.ativo).to.equal(true);
      const serialized = JSON.stringify(response.body).toLowerCase();
      expect(serialized).not.to.include('"senha"');
      expect(serialized).not.to.include("senhahash");
      expect(serialized).not.to.include('"password"');
      expect(serialized).not.to.include('"accesstoken"');
    });
  });

  it("CT-API-003 / US-004 e US-007 — usuário autenticado consulta somente dados de perfil, sem credenciais", () => {
    registerAndLogin().then(({ token, user }) => {
      withToken("GET", "/api/v1/usuarios/me", token).then((response) => {
        expect(response.status).to.equal(200);
        expect(response.body.email).to.equal(user.email);
        const serialized = JSON.stringify(response.body).toLowerCase();
        expect(serialized).not.to.include('"senha"');
        expect(serialized).not.to.include("senhahash");
        expect(serialized).not.to.include('"password"');
        expect(serialized).not.to.include('"accesstoken"');
      });
    });
  });

  it("CT-API-004 / US-006 e US-010 — investidor recebe HTTP 403 ao listar usuários", () => {
    registerAndLogin().then(({ token }) => {
      withToken("GET", "/api/v1/usuarios", token).its("status").should("equal", 403);
    });
  });

  it("CT-API-005 / US-002 — atualiza e persiste apenas os dados permitidos no próprio perfil", () => {
    registerAndLogin().then(({ token }) => {
      const update = {
        nome: "Nome QA Atualizado",
        telefone: "11912345678",
        dataNascimento: "1991-06-12",
        perfilInvestidor: "MODERADO",
      };
      withToken("PUT", "/api/v1/usuarios/me", token, update).then((response) => {
        expect(response.status).to.equal(200);
        expect(response.body.nome).to.equal(update.nome);
        expect(response.body.perfilInvestidor).to.equal("MODERADO");
        return withToken("GET", "/api/v1/usuarios/me", token).then((profile) => {
          expect(profile.status).to.equal(200);
          expect(profile.body.nome).to.equal(update.nome);
          expect(profile.body.telefone).to.equal(update.telefone);
          expect(profile.body.dataNascimento).to.equal(update.dataNascimento);
          expect(profile.body.perfilInvestidor).to.equal(update.perfilInvestidor);
        });
      });
    });
  });

  it("CT-API-006 / US-001 e US-005 — rejeita e-mail duplicado com HTTP 422", () => {
    const user = makeUser();
    register(user).then((first) => {
      expect(first.status).to.equal(201);
      register(user).then((second) => {
        expect(second.status).to.equal(422);
        expect(JSON.stringify(second.body)).to.match(/e-mail|email/i);
      });
    });
  });

  it("CT-API-007 / US-009 e US-007 — altera senha e aceita apenas a nova senha no próximo login", () => {
    registerAndLogin().then(({ token, user }) => {
      const newPassword = "NovaSenhaTeste456";
      withToken("PUT", "/api/v1/usuarios/me/senha", token, {
        senhaAtual: user.senha,
        novaSenha: newPassword,
      }).then((changed) => {
        expect(changed.status).to.equal(204);
        login({ email: user.email, senha: user.senha }).then((oldLogin) => {
          expect(oldLogin.status).to.equal(401);
          login({ email: user.email, senha: newPassword }).then((newLogin) => {
            expect(newLogin.status).to.equal(200);
            expect(newLogin.body.accessToken).to.be.a("string").and.not.be.empty;
          });
        });
      });
    });
  });
});
