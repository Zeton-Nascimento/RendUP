function makeUser() {
  const unique = `${Date.now()}${Math.floor(Math.random() * 10000)}`;
  const digits = unique.replace(/\D/g, "");
  return {
    nome: "Usuário QA Sprint 3",
    email: `qa-sprint3-${unique}@example.test`,
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

function withToken(method, url, token, body, headers = {}) {
  return cy.request({
    method,
    url,
    ...(body === undefined ? {} : { body }),
    headers: { Authorization: `Bearer ${token}`, ...headers },
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

function getProducts(token) {
  return withToken("GET", "/api/v1/produtos?page=0&size=10", token).then((response) => {
    expect(response.status).to.equal(200);
    expect(response.body.content).to.be.an("array").and.not.be.empty;
    return response.body.content;
  });
}

function simulationRequest(productIds) {
  return {
    valorInicial: 1000,
    aporteMensal: 100,
    prazoMeses: 12,
    produtoIds: productIds,
    perfil: "CONSERVADOR",
  };
}

describe("RendUP — Sprint 3 — API real", () => {
  it("CT-061 / US-019 — cria simulação com aporte inicial, mensal e múltiplos produtos", () => {
    withInvestorToken((token) => getProducts(token).then((products) => {
      const productIds = products.slice(0, 2).map((product) => product.id);
      return withToken("POST", "/api/v1/simulacoes", token, simulationRequest(productIds)).then((response) => {
        expect(response.status).to.equal(201);
        expect(response.body.id).to.be.a("string");
        expect(response.body.cenarios).to.have.length(productIds.length);
        expect(response.body).to.include.all.keys(
          "fonteDados",
          "tipoDado",
          "dataReferencia",
          "metodologia",
          "limitacao",
          "disponivelParaDecisao",
        );
      });
    }));
  });

  it("CT-062 / US-019 — rejeita simulação sem produto ou com parâmetro inválido", () => {
    withInvestorToken((token) => withToken("POST", "/api/v1/simulacoes", token, {
      valorInicial: 0,
      aporteMensal: 0,
      prazoMeses: 0,
      produtoIds: [],
      perfil: "CONSERVADOR",
    }).then((response) => {
      expect(response.status).to.be.oneOf([400, 422]);
    }));
  });

  it("CT-064 / US-022 — não recupera simulação inexistente", () => {
    withInvestorToken((token) => withToken(
      "GET",
      "/api/v1/simulacoes/00000000-0000-0000-0000-000000000000",
      token,
    ).then((response) => {
      expect(response.status).to.equal(404);
      expect(JSON.stringify(response.body)).to.match(/simulação|encontrada/i);
    }));
  });

  it("CT-066 / US-022 — histórico e resumo recuperam a simulação salva", () => {
    withInvestorToken((token) => getProducts(token).then((products) => {
      const ids = products.slice(0, 2).map((product) => product.id);
      return withToken("POST", "/api/v1/simulacoes", token, simulationRequest(ids)).then((created) => {
        expect(created.status).to.equal(201);
        const id = created.body.id;
        return withToken("GET", "/api/v1/simulacoes", token).then((history) => {
          expect(history.status).to.equal(200);
          expect(history.body.map((item) => item.id)).to.include(id);
          return withToken("GET", `/api/v1/simulacoes/${id}/resumo`, token).then((summary) => {
            expect(summary.status).to.equal(200);
            expect(summary.body.textoCompartilhavel).to.not.match(/Bearer|access_token|senha/i);
            expect(summary.body).to.have.property("aviso");
          });
        });
      });
    }));
  });

  it("CT-067 / US-017 — o mesmo depósito com a mesma chave deve criar uma única operação", () => {
    withInvestorToken((token) => {
      const key = `qa-sprint3-deposito-${Date.now()}`;
      const body = { valor: 17.5, confirmar: true };
      return withToken("POST", "/api/v1/depositos", token, body, { "Idempotency-Key": key }).then((first) =>
        withToken("POST", "/api/v1/depositos", token, body, { "Idempotency-Key": key }).then((second) => {
          expect(first.status).to.be.oneOf([200, 201]);
          expect(second.status).to.be.oneOf([200, 201]);
          expect(second.body.saldoDisponivel).to.equal(first.body.saldoDisponivel);
        }),
      );
    });
  });

  it("CT-070 / US-023 — depósito válido atualiza o saldo disponível", () => {
    withInvestorToken((token) => withToken("GET", "/api/v1/saldo", token).then((before) => {
      expect(before.status).to.equal(200);
      return withToken("POST", "/api/v1/depositos", token, { valor: 19.25, confirmar: true }).then((deposit) => {
        expect(deposit.status).to.equal(200);
        expect(Number(deposit.body.saldoDisponivel)).to.be.greaterThan(Number(before.body.saldoDisponivel));
      });
    }));
  });

  it("CT-071 / US-023 — depósito inválido não altera o saldo", () => {
    withInvestorToken((token) => withToken("GET", "/api/v1/saldo", token).then((before) =>
      withToken("POST", "/api/v1/depositos", token, { valor: 0, confirmar: true }).then((response) => {
        expect(response.status).to.be.oneOf([400, 422]);
        return withToken("GET", "/api/v1/saldo", token).then((after) => {
          expect(after.body.saldoDisponivel).to.equal(before.body.saldoDisponivel);
        });
      }),
    ));
  });

  it("CT-072 / US-023 — saldo continua disponível em nova consulta autenticada", () => {
    withInvestorToken((token) => withToken("GET", "/api/v1/saldo", token).then((first) => {
      expect(first.status).to.equal(200);
      return withToken("GET", "/api/v1/saldo", token).then((second) => {
        expect(second.status).to.equal(200);
        expect(second.body.saldoDisponivel).to.equal(first.body.saldoDisponivel);
      });
    }));
  });
});
