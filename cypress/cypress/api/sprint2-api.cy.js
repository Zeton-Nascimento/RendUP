function makeUser() {
  const unique = `${Date.now()}${Math.floor(Math.random() * 10000)}`;
  const digits = unique.replace(/\D/g, "");
  return {
    nome: "Usuário QA Sprint 2",
    email: `qa-sprint2-${unique}@example.test`,
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

function login(email, senha) {
  return cy.request({
    method: "POST",
    url: "/api/v1/auth/login",
    body: { email, senha },
    failOnStatusCode: false,
  });
}

function withToken(method, url, token, body) {
  return cy.request({
    method,
    url,
    ...(body === undefined ? {} : { body }),
    headers: { Authorization: `Bearer ${token}` },
    failOnStatusCode: false,
  });
}

function withInvestorToken(callback) {
  const user = makeUser();

  return register(user)
    .then((registration) => {
      expect(registration.status, "cadastro do investidor de teste").to.equal(201);
      return login(user.email, user.senha);
    })
    .then((response) => {
      expect(response.status, "login do investidor de teste").to.equal(200);
      expect(response.body.accessToken).to.be.a("string").and.not.be.empty;
      return callback(response.body.accessToken);
    });
}

function withAdminToken(callback) {
  return cy.env(["adminEmail", "adminPassword"], { log: false }).then(
    ({ adminEmail, adminPassword }) => {
      expect(Boolean(adminEmail && adminPassword), "credenciais administrativas").to.equal(true);

      return login(adminEmail, adminPassword).then((response) => {
        expect(response.status, "login administrativo").to.equal(200);
        expect(response.body.accessToken).to.be.a("string").and.not.be.empty;
        return callback(response.body.accessToken);
      });
    },
  );
}

function sincronizarComRetrySeLimitado(token) {
  return withToken("POST", "/api/v1/produtos/sincronizar", token, null).then((response) => {
    if (response.status !== 429) return response;

    const retryAfter = Number.parseInt(
      response.headers?.["retry-after"] ?? response.headers?.["Retry-After"] ?? "15",
      10,
    );
    const esperaMs = Math.max(1000, (Number.isFinite(retryAfter) ? retryAfter : 15) * 1000 + 250);

    return cy.wait(esperaMs, { log: false }).then(() =>
      withToken("POST", "/api/v1/produtos/sincronizar", token, null),
    );
  });
}

const hasAdminCredentials = Boolean(Cypress.expose("hasAdminCredentials"));

describe("RendUP — Sprint 2 — API real", () => {
  it("CT-033 / US-015 no plano (US-008 no código) — entrada inválida retorna 400 e não persiste", () => {
    const user = makeUser();
    const invalid = { ...user, nome: "", email: user.email };

    register(invalid).then((response) => {
      expect(response.status).to.be.oneOf([400, 422]);
      return register(user).then((corrected) => {
        expect(corrected.status).to.equal(201);
        expect(corrected.body.email).to.equal(user.email);
      });
    });
  });

  it("CT-035 / US-011 — catálogo público retorna somente produtos ativos e aceita filtros", () => {
    cy.request({
      method: "GET",
      url: "/api/v1/produtos?nome=CDB&nivelRisco=BAIXO&ordenacao=RENTABILIDADE&page=0&size=6",
    }).then((response) => {
      expect(response.status).to.equal(200);
      expect(response.body.content).to.be.an("array");
      response.body.content.forEach((product) => {
        expect(product.ativo).to.equal(true);
        expect(product.nome.toLowerCase()).to.include("cdb");
        expect(product.nivelRisco).to.equal("BAIXO");
      });
    });
  });

  it("CT-037 / US-012 — detalhe e relacionados retornam os dados de transparência", () => {
    withInvestorToken((token) => {
      return cy.request("/api/v1/produtos?size=2&page=0").then((list) => {
        expect(list.status).to.equal(200);
        const product = list.body.content[0];
        expect(product).to.have.property("id");

        return withToken("GET", `/api/v1/produtos/${product.id}`, token).then((detail) => {
          expect(detail.status).to.equal(200);
          expect(detail.body).to.include.all.keys(
            "nivelRisco",
            "liquidez",
            "carenciaDias",
            "prazoDias",
            "rentabilidadePercentual",
            "taxas",
            "tributacao",
          );

          return withToken(
            "GET",
            `/api/v1/produtos/${product.id}/relacionados`,
            token,
          ).then((related) => {
            expect(related.status).to.equal(200);
            expect(related.body).to.be.an("array");
          });
        });
      });
    });
  });

  it("CT-038 / US-012 — produto inexistente retorna 404 sem permitir contratação", () => {
    withInvestorToken((token) => {
      return withToken(
        "GET",
        "/api/v1/produtos/00000000-0000-0000-0000-000000000000",
        token,
      ).then((response) => {
        expect(response.status).to.equal(404);
        expect(JSON.stringify(response.body)).to.match(/produto|encontrado/i);
      });
    });
  });

  const adminTest = hasAdminCredentials ? it : it.skip;

  adminTest("CT-034 / US-015 no plano (US-008 no código) — excesso de chamadas recebe 429", () => {
    withAdminToken((token) => {
      return sincronizarComRetrySeLimitado(token).then((first) => {
        expect(first.status).to.equal(200);
        return withToken("POST", "/api/v1/produtos/sincronizar", token, null).then((second) => {
          expect(second.status).to.equal(429);
          expect(second.headers).to.have.property("retry-after");
          expect(JSON.stringify(second.body)).to.match(/muitas solicitações|tente novamente/i);
        });
      });
    });
  });

  adminTest("CT-039 / US-013 — administrador consulta o catálogo administrativo", () => {
    withAdminToken((token) => {
      return withToken("GET", "/api/v1/produtos/admin?page=0&size=10", token).then((response) => {
        expect(response.status).to.equal(200);
        expect(response.body.content).to.be.an("array");
      });
    });
  });

  adminTest("CT-041 / US-014 — administrador sincroniza a fonte mockada", () => {
    withAdminToken((token) => {
      return sincronizarComRetrySeLimitado(token).then((response) => {
        expect(response.status).to.equal(200);
        expect(response.body.status).to.equal("SUCESSO");
        expect(response.body.origemCatalogo).to.equal("MOCK_CATALOGO_EXTERNO");
        expect(response.body).to.include.all.keys(
          "status",
          "origemCatalogo",
          "versaoCatalogo",
          "inseridos",
          "atualizados",
          "inativados",
          "rejeitados",
          "mensagem",
          "hashImportacao",
          "erros",
        );
      });
    });
  });
});
