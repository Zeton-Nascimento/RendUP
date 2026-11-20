import { Before, Given, When, Then } from "@badeball/cypress-cucumber-preprocessor";

const API = "**/api/v1";

const investor = {
  id: "investor-sprint2-001",
  nome: "Ana Souza",
  email: "ana.souza@example.test",
  ativo: true,
  perfilUsuario: "INVESTIDOR",
  perfilInvestidor: "MODERADO",
  cpf: "12345678901",
  telefone: "11987654321",
  dataNascimento: "1990-05-10",
  permissoes: ["ROLE_INVESTIDOR"],
};

const admin = {
  id: "admin-sprint2-001",
  nome: "Admin QA",
  email: "admin.qa@example.test",
  ativo: true,
  perfilUsuario: "ADMIN",
  permissoes: ["ROLE_ADMIN", "PRODUTO_GERIR"],
};

const cdb = {
  id: "produto-cdb-001",
  tipoProduto: "CDB",
  nome: "CDB Liquidez Diária",
  descricao: "Renda fixa para manter seu dinheiro acessível.",
  indexador: "CDI",
  percentualIndexador: 110,
  taxaPrefixada: null,
  carenciaDias: 0,
  taxas: "Sem taxa de administração",
  tributacao: "IR conforme prazo e regra do produto",
  rentabilidadePercentual: 12.5,
  valorMinimo: 100,
  nivelRisco: "BAIXO",
  liquidez: "DIARIA",
  diasLiquidez: 0,
  prazoDias: 365,
  ativo: true,
};

const lci = {
  ...cdb,
  id: "produto-lci-001",
  tipoProduto: "LCI",
  nome: "LCI Mais Rendimento",
  descricao: "Investimento isento de IR para pessoa física.",
  indexador: "CDI",
  percentualIndexador: 95,
  rentabilidadePercentual: 11.2,
  valorMinimo: 500,
  tributacao: "Isento de IR para pessoa física",
};

const page = (content, totalElements = content.length, totalPages = 1) => ({
  content,
  number: 0,
  size: 6,
  totalElements,
  totalPages,
  first: true,
  last: totalPages === 1,
});

const syncSuccess = {
  status: "SUCESSO",
  origemCatalogo: "MOCK_CATALOGO_EXTERNO",
  versaoCatalogo: "v1.0.0",
  inseridos: 1,
  atualizados: 2,
  inativados: 0,
  rejeitados: 0,
  mensagem: "Catálogo sincronizado com sucesso.",
  hashImportacao: "hash-sprint2-qa",
  erros: [],
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

function stubAdminProducts(body = page([cdb])) {
  cy.intercept("GET", `${API}/produtos/admin*`, { statusCode: 200, body }).as("adminProducts");
}

function stubCatalog(body) {
  cy.intercept("GET", `${API}/produtos*`, { statusCode: 200, body }).as("catalog");
}

function stubProfile() {
  cy.intercept("GET", `${API}/usuarios/me`, { statusCode: 200, body: investor }).as("ownProfile");
}

let currentActor = investor;

Before(() => {
  currentActor = investor;
  cy.clearCookies();
  cy.clearLocalStorage();
});

Given("que estou autenticado como investidor", () => {
  currentActor = investor;
});

Given("que estou autenticado como administrador", () => {
  currentActor = admin;
});

Given("que estou na tela de cadastro", () => {
  cy.intercept("POST", `${API}/auth/registrar`).as("register");
  cy.visit("/registro");
});

When("tento enviar o cadastro com campos obrigatórios vazios", () => {
  cy.get("#registro-submit").click();
});

Then("devo ver a validação de campo obrigatório", () => {
  cy.contains("Campo obrigatório.").should("be.visible");
});

Then("a API de cadastro não deve ser chamada", () => {
  cy.get("@register.all").should("have.length", 0);
});

Given("a API do catálogo retorna produtos ativos", () => {
  stubCatalog(page([cdb, lci], 8, 2));
});

Given("a API do catálogo retorna dois produtos ativos", () => {
  stubCatalog(page([cdb, lci]));
});

Given("a API do catálogo não encontra produtos para o filtro", () => {
  stubCatalog(page([]));
});

When("acesso o catálogo de produtos", () => {
  visitWithUser("/produtos", currentActor);
  cy.wait("@catalog");
});

When("aplico o filtro de nome {string} e risco {string}", (nome, risco) => {
  cy.get("#produto-busca").clear().type(nome);
  cy.get("#produto-risco").select(risco);
  cy.get("#produto-ordenacao").select("RENTABILIDADE");
  cy.contains("button", "Buscar produtos").click();
});

When("aplico o filtro de nome {string}", (nome) => {
  cy.get("#produto-busca").clear().type(nome);
  cy.contains("button", "Buscar produtos").click();
});

Then("a consulta deve enviar os filtros selecionados", () => {
  cy.wait("@catalog").its("request.url").should((url) => {
    expect(url).to.include("nome=CDB");
    expect(url).to.include("nivelRisco=BAIXO");
    expect(url).to.include("ordenacao=RENTABILIDADE");
  });
});

Then("devo visualizar apenas produtos ativos do catálogo", () => {
  cy.get(".product-card").should("have.length", 2);
  cy.get(".product-card").each(($card) => {
    cy.wrap($card).should("contain.text", "risco");
  });
  cy.contains("CDB Liquidez Diária").should("be.visible");
  cy.contains("LCI Mais Rendimento").should("be.visible");
});

Then("devo visualizar a paginação do catálogo", () => {
  cy.get("nav[aria-label='Paginação do catálogo']").should("be.visible");
  cy.contains("Página 1 de 2").should("be.visible");
});

Then("devo ver o estado vazio do catálogo", () => {
  cy.contains("Nenhum produto encontrado").should("be.visible");
  cy.contains("Altere os filtros para consultar outras alternativas.").should("be.visible");
});

When("seleciono dois produtos para comparar", () => {
  cy.get(".compare-option input").eq(0).check({ force: true });
  cy.get(".compare-option input").eq(1).check({ force: true });
});

Then("devo ver a tabela de comparação com risco, liquidez e rentabilidade", () => {
  cy.get("section[aria-label='Comparação de produtos']").should("be.visible");
  cy.get(".comparison-table").should("contain.text", "Risco");
  cy.get(".comparison-table").should("contain.text", "Liquidez");
  cy.get(".comparison-table").should("contain.text", "Rentabilidade");
});

Given("a API do detalhe rejeita o produto inexistente", () => {
  cy.intercept("GET", `${API}/produtos/produto-inexistente`, {
    statusCode: 404,
    body: { erro: "Produto financeiro não encontrado." },
  }).as("missingProduct");
});

Given("a API retorna o detalhe do produto e seus relacionados", () => {
  cy.intercept("GET", `${API}/produtos/produto-cdb-001`, { statusCode: 200, body: cdb }).as("productDetail");
  cy.intercept("GET", `${API}/produtos/produto-cdb-001/relacionados`, {
    statusCode: 200,
    body: [lci],
  }).as("relatedProducts");
});

When("acesso os detalhes do produto {string}", (id) => {
  visitWithUser(`/produtos/${id}`, currentActor);
});

Then("devo ver risco, liquidez, carência, vencimento, taxas e tributação", () => {
  cy.wait("@productDetail");
  cy.contains("Dados principais").should("be.visible");
  cy.contains("Risco").should("be.visible");
  cy.contains("Liquidez").should("be.visible");
  cy.contains("Carência").should("be.visible");
  cy.contains("Vencimento").should("be.visible");
  cy.contains("Taxas, impostos e aviso importante").should("be.visible");
  cy.contains("Sem taxa de administração").should("be.visible");
  cy.contains("IR conforme prazo e regra do produto").should("be.visible");
});

Then("devo ver produtos relacionados", () => {
  cy.wait("@relatedProducts");
  cy.contains("Produtos relacionados").should("be.visible");
  cy.contains("LCI Mais Rendimento").should("be.visible");
});

Then("devo ver o erro de produto não encontrado", () => {
  cy.wait("@missingProduct");
  cy.get('[role="alert"]').should("contain.text", "Produto financeiro não encontrado.");
});

Then("não devo ver uma opção de contratação", () => {
  cy.contains(/contratar|contratação/i).should("not.exist");
});

Given("a tela administrativa de produtos está preparada para limitar chamadas", () => {
  stubAdminProducts();
  let calls = 0;
  cy.intercept("POST", `${API}/produtos/sincronizar`, (req) => {
    calls += 1;
    if (calls === 1) {
      req.reply({ statusCode: 200, body: syncSuccess });
      return;
    }
    req.reply({ statusCode: 429, body: { erro: "Muitas solicitações. Tente novamente em instantes." } });
  }).as("syncRateLimit");
});

When("solicito a sincronização do catálogo duas vezes", () => {
  visitWithUser("/admin/produtos", currentActor);
  cy.wait("@adminProducts");
  cy.contains("button", "Sincronizar mock").click();
  cy.wait("@syncRateLimit");
  cy.contains("button", "Sincronizar mock").should("not.be.disabled").click();
  cy.wait("@syncRateLimit");
});

Then("a segunda solicitação deve ser rejeitada por excesso de chamadas", () => {
  cy.get('[role="alert"]').should("contain.text", "Muitas solicitações");
});

Given("a tela administrativa possui um produto ativo", () => {
  let loads = 0;
  cy.intercept("GET", `${API}/produtos/admin*`, (req) => {
    loads += 1;
    req.reply({ statusCode: 200, body: page([loads > 1 ? { ...cdb, ativo: false } : cdb]) });
  }).as("adminProducts");
  cy.intercept("DELETE", `${API}/produtos/${cdb.id}`, { statusCode: 204, body: null }).as("deactivateProduct");
});

When("confirmo a inativação do produto", () => {
  visitWithUser("/admin/produtos", currentActor);
  cy.wait("@adminProducts");
  cy.contains("button", "Inativar").click();
  cy.get('[role="dialog"]').should("contain.text", "Inativar produto?");
  cy.get('[role="dialog"]').contains("button", "Inativar produto").click();
  cy.wait("@deactivateProduct");
  cy.wait("@adminProducts");
});

Then("o produto deve aparecer como inativo", () => {
  cy.get(".status-badge.inactive").should("contain.text", "Inativo");
  cy.contains("button", "Ativar").should("be.visible");
});

Then("devo ver a mensagem de inativação concluída", () => {
  cy.get('[role="status"]').should("contain.text", "Produto inativado com sucesso.");
});

When("tento acessar a administração de produtos", () => {
  stubProfile();
  visitWithUser("/admin/produtos", currentActor);
});

Then("devo ser redirecionado para o meu perfil", () => {
  cy.location("pathname").should("eq", "/perfil");
});

Given("a tela administrativa possui uma fonte mockada preparada", () => {
  stubAdminProducts();
  cy.intercept("POST", `${API}/produtos/sincronizar`, { statusCode: 200, body: syncSuccess }).as("syncCatalog");
});

When("sincronizo o catálogo mockado", () => {
  visitWithUser("/admin/produtos", currentActor);
  cy.wait("@adminProducts");
  cy.contains("button", "Sincronizar mock").click();
  cy.wait("@syncCatalog");
});

Then("devo ver a sincronização concluída", () => {
  cy.get('[role="status"]').should("contain.text", "Sincronização concluída");
  cy.get(".sync-result").should("contain.text", "Sincronização concluída");
});

Then("devo ver a origem e os totais da sincronização", () => {
  cy.get(".sync-result").should("contain.text", "MOCK_CATALOGO_EXTERNO");
  cy.get(".sync-result").should("contain.text", "1 inserido(s)");
  cy.get(".sync-result").should("contain.text", "2 atualizado(s)");
});

Given("a fonte mockada retorna uma falha controlada", () => {
  stubAdminProducts();
  cy.intercept("POST", `${API}/produtos/sincronizar`, {
    statusCode: 200,
    body: {
      ...syncSuccess,
      status: "FALHA",
      inseridos: 0,
      atualizados: 0,
      inativados: 0,
      mensagem: "A fonte mockada não respondeu. O catálogo anterior foi preservado.",
    },
  }).as("syncCatalog");
});

Then("devo ver que a sincronização não foi aplicada", () => {
  cy.get(".sync-result").should("contain.text", "Sincronização não aplicada");
});

Then("devo ver uma mensagem de falha da sincronização", () => {
  cy.get('[role="alert"]').should("contain.text", "A fonte mockada não respondeu");
});

Then("registro a evidência do caso {string}", (caseId) => {
  cy.screenshot(`release2-sprint2/${caseId}`);
});
