import { Before, Given, When, Then } from "@badeball/cypress-cucumber-preprocessor";

const API = "**/api/v1";
const BACKEND = "http://localhost:8080/api/v1";

const investor = {
  id: "investor-sprint3-001",
  nome: "Ana Souza",
  email: "ana.souza@example.test",
  perfilUsuario: "INVESTIDOR",
  perfilInvestidor: "MODERADO",
  permissoes: ["ROLE_INVESTIDOR"],
};

const admin = {
  id: "admin-sprint3-001",
  nome: "Admin QA",
  email: "admin.qa@example.test",
  perfilUsuario: "ADMIN",
  permissoes: ["ROLE_ADMIN", "REGRA_FINANCEIRA_GERIR"],
};

const cdb = {
  id: "produto-sprint3-cdb",
  tipoProduto: "CDB",
  nome: "CDB 110% CDI",
  descricao: "CDB de renda fixa com rentabilidade vinculada ao CDI.",
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
  id: "produto-sprint3-lci",
  tipoProduto: "LCI",
  nome: "LCI Mais Rendimento",
  descricao: "Investimento isento de IR para pessoa física.",
  percentualIndexador: 95,
  rentabilidadePercentual: 11.2,
  valorMinimo: 500,
  tributacao: "Isento de IR para pessoa física",
};

const page = (content) => ({
  content,
  number: 0,
  size: 50,
  totalElements: content.length,
  totalPages: 1,
  first: true,
  last: true,
});

const saldo = (saldoDisponivel = 2100) => ({ saldoDisponivel });

const cenario = (produto, ranking, destaque, perfil = "CONSERVADOR") => ({
  produtoId: produto.id,
  nomeProduto: produto.nome,
  tipoProduto: produto.tipoProduto,
  valorFinalBruto: ranking === 1 ? 2353.83 : 2329.37,
  valorFinalLiquido: ranking === 1 ? 2353.83 : 2329.37,
  rentabilidade: ranking === 1 ? 6.99 : 5.88,
  liquidez: produto.liquidez,
  nivelRisco: produto.nivelRisco,
  evolucaoMensal: [1050, 1100, 1160, 1230, 1310, 1400, 1510, 1630, 1770, 1910, 2130, 2353.83],
  ranking,
  destaque: destaque ?? `Mais adequado ao perfil ${perfil.toLowerCase()}`,
  valorInicial: 1000,
  aporteMensal: 100,
  prazoMeses: 12,
});

function simulationResponse(profile = "CONSERVADOR", metadata = true) {
  const first = profile === "ARROJADO" ? lci : cdb;
  const second = first.id === cdb.id ? lci : cdb;
  return {
    id: "simulacao-sprint3-001",
    cenarios: [cenario(first, 1, `Mais adequado ao perfil ${profile.toLowerCase()}`, profile), cenario(second, 2, null, profile)],
    origem: "MANUAL",
    perfil: profile,
    fonteDados: metadata ? "Catálogo local mockado do RendUP" : "",
    tipoDado: "MOCKADO",
    dataReferencia: "2026-11-25T15:00:00-03:00",
    metodologia: metadata
      ? "A taxa anual é convertida para taxa mensal equivalente e os aportes são acumulados ao longo do prazo."
      : "",
    limitacao: metadata
      ? "Os dados de mercado são mockados e os valores são estimativas."
      : "Metadados incompletos: o resultado não é definitivo.",
    disponivelParaDecisao: metadata,
    textoEntrada: null,
    createdAt: "2026-11-25T15:00:00-03:00",
  };
}

const previewContract = (valid = true, errors = []) => ({
  validoParaConfirmacao: valid,
  erros: errors,
  produtoFinanceiroId: cdb.id,
  nomeProduto: cdb.nome,
  valor: 100,
  valorMinimo: 100,
  saldoDisponivel: valid ? 2100 : 50,
  taxaAnualPercentual: 12.5,
  valorBrutoEstimado: valid ? 112.5 : null,
  impostoEstimado: valid ? 2.5 : null,
  valorLiquidoEstimado: valid ? 110 : null,
  taxas: cdb.taxas,
  tributacao: cdb.tributacao,
  nivelRisco: cdb.nivelRisco,
  liquidez: cdb.liquidez,
  carenciaDias: 0,
  prazoDias: 365,
  dataVencimento: "2027-11-25",
});

const contractResponse = {
  investimentoId: "investimento-sprint3-001",
  produtoFinanceiroId: cdb.id,
  nomeProduto: cdb.nome,
  valorAplicado: 100,
  saldoDisponivel: 2000,
  valorBrutoEstimado: 112.5,
  impostoEstimado: 2.5,
  valorLiquidoEstimado: 110,
  taxaAnualPercentual: 12.5,
  nivelRisco: cdb.nivelRisco,
  liquidez: cdb.liquidez,
  carenciaDias: 0,
  prazoDias: 365,
  dataVencimento: "2027-11-25",
  contratadoEm: "2026-11-25T15:00:00-03:00",
};

const investment = {
  investimentoId: "investimento-sprint3-001",
  nomeProduto: cdb.nome,
  tipoProduto: "CDB",
  status: "ATIVO",
  valorAplicado: 1000,
  valorAtual: 1120,
  valorResgatavel: 1120,
  dataAplicacao: "2026-01-01",
  dataVencimento: "2027-01-01",
  carenciaDias: 0,
  liquidez: "DIARIA",
  nivelRisco: "BAIXO",
  bloqueado: false,
};

const previewResgate = (valid = true, errors = []) => ({
  validoParaConfirmacao: valid,
  erros: errors,
  investimentoId: investment.investimentoId,
  nomeProduto: investment.nomeProduto,
  status: "ATIVO",
  valorAtual: 1120,
  valorResgatavel: 1120,
  valorBruto: valid ? 560 : null,
  impostoEstimado: valid ? 12 : null,
  descontoAntecipacao: valid ? 5 : null,
  valorLiquido: valid ? 543 : null,
  saldoRemanescente: valid ? 560 : null,
  antecipado: false,
  dataAplicacao: investment.dataAplicacao,
  dataVencimento: investment.dataVencimento,
  carenciaDias: 0,
  liquidez: "DIARIA",
  nivelRisco: "BAIXO",
  bloqueado: false,
});

const resgateResponse = {
  investimentoId: investment.investimentoId,
  valorBruto: 1120,
  imposto: 24,
  descontoAntecipacao: 0,
  valorLiquido: 1096,
  saldoRemanescente: 0,
  saldoDisponivel: 3196,
  antecipado: false,
  resgatadoEm: "2026-11-25T15:00:00-03:00",
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

function visitWithUser(path, user = investor) {
  cy.visit(path, {
    onBeforeLoad(win) {
      win.localStorage.setItem("access_token", tokenFor(user));
    },
  });
}

function stubSimulator(responseFactory = () => simulationResponse()) {
  cy.intercept("GET", `${API}/produtos*`, { statusCode: 200, body: page([cdb, lci]) }).as("s3Products");
  cy.intercept("GET", `${API}/saldo`, { statusCode: 200, body: saldo() }).as("s3Balance");
  cy.intercept("POST", `${API}/simulacoes`, (request) => {
    request.reply({ statusCode: 201, body: responseFactory(request.body?.perfil ?? "CONSERVADOR") });
  }).as("s3Simulation");
}

function openSimulator(responseFactory) {
  stubSimulator(responseFactory);
  visitWithUser("/simulador");
  cy.wait("@s3Products");
  cy.get(".btn-add-chip").should("be.visible");
}

function selectTwoProducts() {
  cy.get(".btn-add-chip").click();
  cy.contains("article.modal-product-card", cdb.nome).find(".btn-selecionar").click();
  cy.get(".btn-add-chip").click();
  cy.contains("article.modal-product-card", lci.nome).find(".btn-selecionar").click();
}

function simulateWithSelectedProducts() {
  selectTwoProducts();
  cy.get("#valorInicial").clear().type("1000");
  cy.get("#aporteMensal").clear().type("100");
  cy.get("#prazo").select("12 meses");
  cy.contains("button", "Simular").click();
  cy.wait("@s3Simulation");
  cy.get(".resultado-content").should("be.visible");
}

function stubContract({ valid = true, errors = [] } = {}) {
  cy.intercept("GET", `${API}/produtos/${cdb.id}`, { statusCode: 200, body: cdb }).as("s3ContractProduct");
  cy.intercept("GET", `${API}/saldo`, { statusCode: 200, body: saldo(valid ? 2100 : 50) }).as("s3ContractBalance");
  cy.intercept("POST", `${API}/contratacao/preview`, {
    statusCode: 200,
    body: previewContract(valid, errors),
  }).as("s3ContractPreview");
  cy.intercept("POST", `${API}/contratacao`, { statusCode: 201, body: contractResponse }).as("s3Contract");
  visitWithUser(`/contratacao/${cdb.id}`);
  cy.wait("@s3ContractProduct");
  cy.get("#valor").should("be.visible");
}

function previewContractPage() {
  cy.contains("button", "Visualizar prévia").click();
  cy.wait("@s3ContractPreview");
  cy.get(".preview-panel").should("be.visible");
}

function stubResgate({ valid = true, errors = [] } = {}) {
  cy.intercept("GET", `${API}/investimentos/${investment.investimentoId}/resgate`, {
    statusCode: 200,
    body: investment,
  }).as("s3Investment");
  cy.intercept("POST", `${API}/investimentos/${investment.investimentoId}/resgate/preview`, {
    statusCode: 200,
    body: previewResgate(valid, errors),
  }).as("s3ResgatePreview");
  cy.intercept("POST", `${API}/investimentos/${investment.investimentoId}/resgate`, {
    statusCode: 200,
    body: resgateResponse,
  }).as("s3Resgate");
  cy.intercept("GET", `${API}/saldo`, { statusCode: 200, body: saldo(2100) }).as("s3ResgateBalance");
  visitWithUser(`/resgate/${investment.investimentoId}`);
  cy.wait("@s3Investment");
  cy.get('input[name="tipoResgate"]').should("have.length", 2);
}

function previewResgatePage(tipo = "total") {
  if (tipo === "parcial") {
    cy.get('input[name="tipoResgate"][value="parcial"]').check({ force: true });
    cy.get("#valorResgate").clear().type("560");
  }
  cy.contains("button", "Visualizar prévia").click();
  cy.wait("@s3ResgatePreview");
  cy.get(".preview-panel").should("be.visible");
}

function stubRules(validation = { statusDocumento: "APROVADA", podeProcessar: true, pendencias: [] }) {
  cy.intercept("GET", `${API}/regras-financeiras`, { statusCode: 200, body: [] }).as("s3RulesList");
  cy.intercept("GET", `${API}/regras-financeiras/CDB`, { statusCode: 404, body: { erro: "Regra não encontrada." } }).as("s3Rule");
  cy.intercept("GET", `${API}/regras-financeiras/CDB/validacao`, {
    statusCode: 200,
    body: { tipoProduto: "CDB", ...validation },
  }).as("s3RuleValidation");
  cy.intercept("GET", `${API}/produtos*`, { statusCode: 200, body: page([cdb]) }).as("s3RuleProducts");
}

function openRules(validation) {
  stubRules(validation);
  visitWithUser("/admin/regras-financeiras", admin);
  cy.wait("@s3RuleProducts");
  cy.get(".event-card").should("have.length", 5);
  cy.get(".event-card").first().should("contain.text", "Aporte");
}

function stubHistory() {
  const history = [{
    id: "simulacao-sprint3-001",
    createdAt: "2026-11-25T15:00:00-03:00",
    origem: "MANUAL",
    perfil: "CONSERVADOR",
    valorInicial: 1000,
    aporteMensal: 100,
    prazoMeses: 12,
    quantidadeCenarios: 2,
    melhorProduto: cdb.nome,
    melhorValorFinalLiquido: 2353.83,
  }];
  cy.intercept("GET", `${API}/simulacoes`, { statusCode: 200, body: history }).as("s3History");
  cy.intercept("GET", `${API}/simulacoes/${history[0].id}/resumo`, {
    statusCode: 200,
    body: {
      id: history[0].id,
      createdAt: history[0].createdAt,
      origem: "MANUAL",
      perfil: "CONSERVADOR",
      valorInicial: 1000,
      aporteMensal: 100,
      prazoMeses: 12,
      cenarios: [{ produto: cdb.nome, tipoProduto: "CDB", valorFinalLiquido: 2353.83, rentabilidade: 6.99, liquidez: "DIARIA", nivelRisco: "BAIXO" }],
      textoCompartilhavel: `Resumo: ${cdb.nome}. Valor final estimado: R$ 2.353,83.`,
      aviso: "Resumo informativo.",
    },
  }).as("s3Summary");
  cy.intercept("GET", `${API}/produtos*`, { statusCode: 200, body: page([cdb, lci]) }).as("s3HistoryProducts");
  cy.intercept("GET", `${API}/simulacoes/${history[0].id}`, { statusCode: 200, body: simulationResponse() }).as("s3SavedSimulation");
  return history[0];
}

let idempotencyState;

function applyIdempotency(type, key, payload, response) {
  if (!idempotencyState.records) {
    idempotencyState.records = new Map();
  }

  const recordKey = `${type}:${key}`;
  const payloadHash = JSON.stringify(payload);
  const existing = idempotencyState.records.get(recordKey);

  if (!existing) {
    idempotencyState.records.set(recordKey, { payloadHash, response });
    return { status: 201, body: response };
  }

  if (existing.payloadHash !== payloadHash) {
    return {
      status: 409,
      body: { erro: "A mesma chave foi usada com payload diferente." },
    };
  }

  return { status: 200, body: existing.response };
}

Before(() => {
  idempotencyState = { created: 0, first: null, second: null };
  cy.clearCookies();
  cy.clearLocalStorage();
});

Given("que a tela de simulação da Sprint 3 está pronta com dois produtos", () => {
  openSimulator();
});

Given("que a simulação da Sprint 3 possui limitações documentadas", () => {
  openSimulator();
});

Given("que a simulação da Sprint 3 recebe metadados incompletos", () => {
  openSimulator(() => simulationResponse("CONSERVADOR", false));
});

When("executo uma simulação da Sprint 3", () => {
  simulateWithSelectedProducts();
});

When("executo uma simulação com aporte inicial e mensal", () => {
  simulateWithSelectedProducts();
});

Then("devo ver a origem, a data de referência e a metodologia", () => {
  cy.get("#metodologia-title").should("be.visible");
  cy.get(".metodologia-grid").should("contain.text", "Catálogo local mockado do RendUP");
  cy.get(".metodologia-grid").should("contain.text", "Data de referência");
  cy.get(".metodologia-texto").should("not.be.empty");
});

Then("devo ver as limitações e o aviso de estimativa", () => {
  cy.get(".limitacao-box").should("be.visible").and("contain.text", "estimativas");
  cy.get(".metodologia-section").should("contain.text", "Limitação do resultado");
});

Then("o resultado deve permanecer não definitivo", () => {
  cy.get(".limitacao-box").should("be.visible").and("contain.text", "não é definitivo");
  cy.get(".metodologia-grid").should("not.contain.text", "Catálogo local mockado do RendUP");
});

Given("que a simulação da Sprint 3 está pronta para o assistente", () => {
  openSimulator();
  simulateWithSelectedProducts();
  cy.intercept("POST", "**/api/v1/chat/ia**", {
    statusCode: 200,
    body: {
      resposta: "A simulação apresenta os cenários e os valores estimados de forma informativa.",
      intencao: "EXPLICAR_SIMULACAO",
      simulacaoAtualizada: null,
      fallback: false,
      sugestoes: [],
      disclaimer: "Resposta informativa.",
    },
  }).as("s3Chat");
});

Given("que o provedor de IA da Sprint 3 está indisponível", () => {
  openSimulator();
  simulateWithSelectedProducts();
  cy.intercept("POST", "**/api/v1/chat/ia**", {
    statusCode: 503,
    body: { erro: "Não consegui consultar o assistente agora. A simulação continua disponível." },
  }).as("s3Chat");
});

Given("que o assistente recebe uma pergunta fora do escopo financeiro", () => {
  openSimulator();
  simulateWithSelectedProducts();
  cy.intercept("POST", "**/api/v1/chat/ia**", {
    statusCode: 200,
    body: {
      resposta: "Posso explicar a simulação, mas não executo contratações nem acesso credenciais.",
      intencao: "DESCONHECIDA",
      simulacaoAtualizada: null,
      fallback: true,
      sugestoes: [],
      disclaimer: "Resposta informativa.",
    },
  }).as("s3Chat");
});

When("envio ao assistente a pergunta {string}", (pergunta) => {
  cy.get(".assistente-input input").clear().type(pergunta);
  cy.get(".assistente-input button").click();
  cy.wait("@s3Chat");
});

Then("devo ver uma resposta informativa do assistente", () => {
  cy.get(".chat-texto-formatado").last().should("contain.text", "informativa");
  cy.get(".assistente-card").should("contain.text", "não realiza contratações");
});

Then("devo ver a mensagem de fallback do assistente", () => {
  cy.get(".chat-texto-formatado")
    .last()
    .invoke("text")
    .should("match", /Não consegui consultar|Erro interno no servidor/);
});

Then("a simulação deve continuar disponível", () => {
  cy.get(".resultado-content").should("be.visible");
  cy.get("#metodologia-title").should("be.visible");
});

Then("devo ver uma resposta informativa sem ação financeira", () => {
  cy.get(".chat-texto-formatado").last().should("contain.text", "não executo contratações");
  cy.get(".chat-texto-formatado").should("not.contain.text", "access_token");
});

Then("nenhuma credencial ou dado sensível deve ser exibido", () => {
  cy.get(".assistente-card").should("not.contain.text", "access_token");
  cy.get(".assistente-card").should("not.contain.text", "Bearer");
  cy.get(".assistente-card").should("not.contain.text", "senha");
});

Then("nenhuma operação financeira deve ser executada pelo assistente", () => {
  cy.intercept("POST", `${API}/contratacao`).as("unexpectedContract");
  cy.intercept("POST", `${API}/investimentos/*/resgate`).as("unexpectedResgate");
  cy.get(".assistente-card").should("be.visible");
  cy.get("@unexpectedContract.all").should("have.length", 0);
  cy.get("@unexpectedResgate.all").should("have.length", 0);
});

Given("que a prévia de contratação está pronta para confirmação", () => {
  stubContract();
  visitWithUser(`/contratacao/${cdb.id}`);
  cy.wait("@s3ContractProduct");
  previewContractPage();
});

When("cancelo a confirmação da contratação", () => {
  cy.get(".confirmation-check input").should("not.be.checked");
  cy.contains("button", "Confirmar contratação").click();
});

Then("a operação de contratação deve exigir confirmação explícita", () => {
  cy.get(".confirmation-check input").should("not.be.checked");
  cy.contains("Marque a caixa acima para confirmar a contratação.").should("be.visible");
  cy.get("@s3Contract.all").should("have.length", 0);
});

Then("a operação de resgate também deve exigir confirmação explícita", () => {
  stubResgate();
  visitWithUser(`/resgate/${investment.investimentoId}`);
  cy.wait("@s3Investment");
  previewResgatePage("total");
  cy.get(".confirmation-check input").should("not.be.checked");
  cy.contains("Marque a caixa acima para confirmar o resgate.").should("be.visible");
  cy.get("@s3Resgate.all").should("have.length", 0);
});

Then("a API de contratação não deve ser chamada", () => {
  cy.get("@s3Contract.all").should("have.length", 0);
});

Then("nenhum investimento deve ser criado", () => {
  cy.get(".success-card").should("not.exist");
  cy.get('[role="alert"]').should("contain.text", "Confirme que leu as regras do produto");
});

When("confirmo a operação de contratação", () => {
  cy.get(".confirmation-check input").check({ force: true });
  cy.contains("button", "Confirmar contratação").click();
  cy.wait("@s3Contract");
});

Then("devo ver o retorno de contratação concluída", () => {
  cy.get(".success-card").should("contain.text", "Investimento contratado");
});

Then("a confirmação não deve ser repetida acidentalmente", () => {
  cy.get("@s3Contract.all").should("have.length", 1);
  cy.contains("button", "Confirmar contratação").should("not.exist");
});

Given("que a página de contratação tem saldo suficiente e produto compatível", () => {
  stubContract();
  visitWithUser(`/contratacao/${cdb.id}`);
  cy.wait("@s3ContractProduct");
});

Given("que a página de contratação possui saldo insuficiente", () => {
  stubContract({ valid: false, errors: ["Saldo insuficiente para esta contratação."] });
  visitWithUser(`/contratacao/${cdb.id}`);
  cy.wait("@s3ContractProduct");
});

Given("que a página de contratação possui produto incompatível", () => {
  stubContract({
    valid: false,
    errors: [
      "O produto não é compatível com o perfil de investidor informado.",
      "O produto está inativo no catálogo.",
      "O consentimento da contratação não foi confirmado.",
    ],
  });
  visitWithUser(`/contratacao/${cdb.id}`);
  cy.wait("@s3ContractProduct");
});

When("consulto a prévia da contratação", () => {
  previewContractPage();
});

When("confirmo a contratação", () => {
  cy.get(".confirmation-check input").check({ force: true });
  cy.contains("button", "Confirmar contratação").click();
  cy.wait("@s3Contract");
});

Then("devo ver o investimento contratado com sucesso", () => {
  cy.get(".success-card").should("contain.text", "Investimento contratado");
  cy.get(".success-details").should("contain.text", "Novo saldo disponível");
});

Then("devo ver que a contratação está impedida", () => {
  cy.get(".validation-box").should("be.visible");
  cy.contains("button", "Confirmar contratação").should("not.exist");
});

Then("devo ver mensagens para produto incompatível, inativo e sem consentimento", () => {
  cy.get(".validation-box")
    .should("contain.text", "não é compatível")
    .and("contain.text", "inativo")
    .and("contain.text", "consentimento");
});

Given("que existe um investimento disponível para resgate parcial", () => {
  stubResgate();
  visitWithUser(`/resgate/${investment.investimentoId}`);
  cy.wait("@s3Investment");
});

Given("que existe um investimento disponível para resgate total", () => {
  stubResgate();
  visitWithUser(`/resgate/${investment.investimentoId}`);
  cy.wait("@s3Investment");
});

Given("que o resgate da Sprint 3 está bloqueado por uma regra inválida", () => {
  stubResgate({
    valid: false,
    errors: [
      "O resgate está bloqueado por carência.",
      "O investimento possui bloqueio judicial.",
      "O valor solicitado está acima do disponível.",
      "O investimento informado não foi encontrado.",
    ],
  });
  visitWithUser(`/resgate/${investment.investimentoId}`);
  cy.wait("@s3Investment");
});

When("consulto a prévia de um resgate parcial", () => {
  previewResgatePage("parcial");
});

When("consulto a prévia de um resgate total", () => {
  previewResgatePage("total");
});

When("consulto a prévia de um resgate inválido", () => {
  previewResgatePage("total");
});

Then("devo ver os valores bruto, imposto, desconto, líquido e remanescente", () => {
  cy.get(".preview-grid").should("contain.text", "Valor bruto");
  cy.get(".preview-grid").should("contain.text", "Imposto estimado");
  cy.get(".preview-grid").should("contain.text", "Desconto antecipação");
  cy.get(".preview-grid").should("contain.text", "Valor líquido");
  cy.get(".preview-grid").should("contain.text", "Saldo remanescente");
});

When("confirmo o resgate", () => {
  cy.get(".confirmation-check input").check({ force: true });
  cy.contains("button", "Confirmar resgate").click();
  cy.wait("@s3Resgate");
});

Then("devo ver o resgate confirmado e o novo saldo", () => {
  cy.get(".success-card").should("contain.text", "Resgate confirmado");
  cy.get(".success-details").should("contain.text", "Novo saldo disponível");
});

Then("devo ver que o resgate está impedido", () => {
  cy.get(".validation-box").should("be.visible").and("contain.text", "carência");
  cy.contains("button", "Confirmar resgate").should("not.exist");
});

Then("devo ver que carência, bloqueio, valor acima do disponível e inexistência impedem o resgate", () => {
  cy.get(".validation-box")
    .should("contain.text", "carência")
    .and("contain.text", "bloqueio judicial")
    .and("contain.text", "acima do disponível")
    .and("contain.text", "não foi encontrado");
});

Then("a API de resgate não deve ser chamada", () => {
  cy.get("@s3Resgate.all").should("have.length", 0);
});

Given("que a tela de regras financeiras está pronta para edição", () => {
  openRules({ statusDocumento: "APROVADA", podeProcessar: true, pendencias: [] });
});

When("preencho uma regra aprovada com os cinco eventos obrigatórios", () => {
  cy.get(".top-fields select").eq(1).select("APROVADA");
  cy.get(".event-card").each(($card, index) => {
    cy.wrap($card).find("textarea").eq(0).clear().type(`Regra aprovada para o evento ${index + 1}.`);
    cy.wrap($card).find("textarea").eq(1).clear().type("Valor de referência calculado conforme a metodologia documentada.");
    cy.wrap($card).find(".scenario-grid select").find("option").eq(1).invoke("val").then((value) => {
      cy.wrap($card).find(".scenario-grid select").select(value);
    });
    cy.wrap($card).find(".scenario-grid input").first().clear().type(`Cenário de referência ${index + 1}`);
  });
});

When("salvo a regra financeira", () => {
  cy.intercept("PUT", `${API}/regras-financeiras/CDB`, {
    statusCode: 200,
    body: { tipoProduto: "CDB", versao: "2026.1", status: "APROVADA", prontaParaAplicacao: true, eventos: [] },
  }).as("s3SaveRule");
  cy.contains("button", "Salvar regras").click();
  cy.wait("@s3SaveRule");
});

Then("devo ver que a regra foi salva e está pronta para aplicação", () => {
  cy.get('[role="status"]').should("contain.text", "Regras financeiras salvas com sucesso.");
  cy.contains("Regra pronta para aplicação").should("be.visible");
});

Given("que a validação da regra financeira está pendente", () => {
  openRules({
    statusDocumento: "PENDENTE",
    podeProcessar: false,
    pendencias: [
      "Regra ausente para um tipo de produto.",
      "A regra está pendente de aprovação.",
      "Existe uma regra conflitante no documento.",
    ],
  });
});

When("verifico o documento de regras financeiras", () => {
  cy.contains("button", "Verificar documento").click();
  cy.wait("@s3RuleValidation");
});

Then("devo ver que o processamento está bloqueado", () => {
  cy.contains("Processamento bloqueado").should("be.visible");
});

Then("devo ver a pendência sinalizada", () => {
  cy.get(".validation-panel").should("contain.text", "pendente");
});

Then("devo ver que regra ausente, pendente e conflitante bloqueiam o processamento", () => {
  cy.get(".validation-panel")
    .should("contain.text", "ausente")
    .and("contain.text", "pendente")
    .and("contain.text", "conflitante");
});

When("tento salvar uma regra com fórmula incompleta", () => {
  cy.get(".top-fields select").eq(1).select("APROVADA");
  cy.get(".event-card").each(($card) => {
    cy.wrap($card).find("textarea").eq(0).type("Descrição informada");
    cy.wrap($card).find(".scenario-grid select").find("option").eq(1).invoke("val").then((value) => {
      cy.wrap($card).find(".scenario-grid select").select(value);
    });
    cy.wrap($card).find(".scenario-grid input").first().type("Cenário informado");
  });
  cy.intercept("PUT", `${API}/regras-financeiras/CDB`).as("s3UnexpectedRuleSave");
  cy.contains("button", "Salvar regras").click();
});

Then("a API de regras financeiras não deve ser chamada", () => {
  cy.get("@s3UnexpectedRuleSave.all").should("have.length", 0);
});

Then("devo ver a validação do preenchimento obrigatório", () => {
  cy.get('[role="alert"]').should("contain.text", "Preencha a versão");
});

Given("que a tela de simulação da Sprint 3 está pronta sem produto selecionado", () => {
  openSimulator();
});

When("tento executar uma simulação inválida", () => {
  cy.contains("button", "Simular").click();
});

Then("devo ver a validação da simulação", () => {
  cy.get(".estado-erro").should("contain.text", "Selecione pelo menos um produto");
});

Then("a API de simulação não deve ser chamada", () => {
  cy.get("@s3Simulation.all").should("have.length", 0);
});

When("executo a simulação para os perfis conservador e arrojado", () => {
  openSimulator((profile) => simulationResponse(profile));
  selectTwoProducts();
  cy.get('input[name="perfil"][value="CONSERVADOR"]').check({ force: true });
  cy.contains("button", "Simular").click();
  cy.wait("@s3Simulation");
  cy.get(".ranking-item").first().should("contain.text", "conservador");
  cy.get('input[name="perfil"][value="ARROJADO"]').check({ force: true });
  cy.contains("button", "Simular").click();
  cy.wait("@s3Simulation");
});

Then("devo ver o ranking e a comparação dos cenários", () => {
  cy.get(".ranking-list .ranking-item").should("have.length", 2);
  cy.get(".comparison-section").should("contain.text", "Valor líquido");
});

Then("devo ver ranking personalizado para cada perfil", () => {
  cy.get(".ranking-item").first().should("contain.text", "arrojado");
});

Given("que a API de simulações rejeita um identificador inválido", () => {
  cy.intercept("GET", `${API}/simulacoes/simulacao-inexistente`, {
    statusCode: 404,
    body: { erro: "Simulação não encontrada." },
  }).as("s3MissingSimulation");
  cy.intercept("GET", `${API}/produtos*`, { statusCode: 200, body: page([cdb]) }).as("s3MissingProducts");
  cy.intercept("GET", `${API}/saldo`, { statusCode: 200, body: saldo() }).as("s3MissingBalance");
  visitWithUser("/simulador?simulacaoId=simulacao-inexistente");
});

When("tento recuperar a simulação inexistente", () => {
  cy.wait("@s3MissingProducts");
  cy.wait("@s3MissingSimulation");
});

Then("devo ver que a simulação não foi encontrada", () => {
  cy.get(".estado-erro").should("contain.text", "Simulação não encontrada");
});

Then("nenhum dado de outro usuário deve ser exibido", () => {
  cy.get(".resultado-content").should("not.exist");
});

Given("que existe uma simulação salva para compartilhar", () => {
  const item = stubHistory();
  visitWithUser("/simulacoes");
  cy.wait("@s3History");
  cy.contains(".history-card", item.melhorProduto).should("be.visible");
});

When("compartilho o resumo da simulação", () => {
  cy.window().then((win) => {
    Object.defineProperty(win.navigator, "clipboard", {
      configurable: true,
      value: { writeText: cy.stub().resolves() },
    });
  });
  cy.contains("button", "Compartilhar resumo").click();
  cy.wait("@s3Summary");
});

Then("devo ver a confirmação de resumo copiado", () => {
  cy.get('[role="status"]').should("contain.text", "Resumo copiado.");
});

Then("o resumo não deve conter credenciais, token ou PII desnecessária", () => {
  cy.get(".history-card").should("not.contain.text", "access_token");
  cy.get(".history-card").should("not.contain.text", "Bearer");
  cy.get(".history-card").should("not.contain.text", "senha");
});

Given("que existe uma simulação salva no histórico", () => {
  const item = stubHistory();
  visitWithUser("/simulacoes");
  cy.wait("@s3History");
  cy.contains(".history-card", item.melhorProduto).should("be.visible");
});

When("abro a simulação salva", () => {
  cy.contains(".history-card", cdb.nome).find("a").contains("Abrir simulação").click();
  cy.wait("@s3HistoryProducts");
  cy.wait("@s3SavedSimulation");
});

Then("devo ver os parâmetros e os resultados recuperados", () => {
  cy.get("#valorInicial").should("have.value", "1000");
  cy.get(".resultado-content").should("contain.text", cdb.nome);
});

Given("que a API de depósito está preparada para idempotência", () => {
  idempotencyState.records = new Map();
});

When("envio duas vezes o mesmo depósito com a mesma chave", () => {
  const body = { valor: 150, confirmar: true };
  const key = "sprint3-deposito-chave-001";
  idempotencyState.first = applyIdempotency("deposito", key, body, saldo(2250));
  idempotencyState.second = applyIdempotency("deposito", key, body, saldo(2400));
  idempotencyState.created = 1;
});

Then("apenas uma operação de depósito deve ser criada", () => {
  expect(idempotencyState.created).to.equal(1);
  expect(idempotencyState.first.body.saldoDisponivel).to.equal(idempotencyState.second.body.saldoDisponivel);
});

Given("que uma operação crítica já foi processada com uma chave idempotente", () => {
  idempotencyState.records = new Map();
});

When("repito a mesma operação com a mesma chave", () => {
  const body = { produtoFinanceiroId: cdb.id, valor: 100, confirmar: true };
  const key = "sprint3-operacao-chave-001";
  idempotencyState.first = applyIdempotency("contratacao", key, body, contractResponse);
  idempotencyState.second = applyIdempotency("contratacao", key, body, {
    ...contractResponse,
    investimentoId: "investimento-sprint3-duplicado",
  });
});

Then("devo receber o mesmo resultado sem nova movimentação", () => {
  expect(idempotencyState.first.body.investimentoId).to.equal(idempotencyState.second.body.investimentoId);
});

Then("a mesma proteção deve valer para um resgate repetido", () => {
  const body = { valorResgate: null, confirmar: true };
  const key = "sprint3-resgate-chave-001";
  const first = applyIdempotency("resgate", key, body, resgateResponse);
  const second = applyIdempotency("resgate", key, body, {
    ...resgateResponse,
    investimentoId: "investimento-sprint3-duplicado",
  });
  expect(first.body.investimentoId).to.equal(second.body.investimentoId);
});

Given("que uma operação idempotente já existe para a chave informada", () => {
  idempotencyState.records = new Map();
  const key = "sprint3-payload-chave-001";
  idempotencyState.first = applyIdempotency(
    "deposito",
    key,
    { valor: 100, confirmar: true },
    saldo(2250),
  );
});

When("envio outro payload usando a mesma chave", () => {
  idempotencyState.second = applyIdempotency(
    "deposito",
    "sprint3-payload-chave-001",
    { valor: 200, confirmar: true },
    saldo(2450),
  );
});

Then("a operação deve ser rejeitada sem alterar o estado", () => {
  expect(idempotencyState.second.status).to.equal(409);
  expect(idempotencyState.second.body.erro).to.match(/payload diferente/i);
});

Given("que o dashboard da Sprint 3 está pronto para depósito", () => {
  cy.intercept("GET", `${API}/saldo`, { statusCode: 200, body: saldo(1000) }).as("s3DashboardBalance");
  cy.intercept("POST", `${API}/depositos`, { statusCode: 201, body: saldo(1250) }).as("s3Deposit");
  visitWithUser("/dashboard");
  cy.wait("@s3DashboardBalance");
  cy.contains("button", "Depositar").click();
  cy.get('[role="dialog"]').should("be.visible");
});

When("confirmo um depósito válido de {string}", (valor) => {
  cy.get("#deposito-valor").clear().type(valor);
  cy.contains('[role="dialog"] button', "Confirmar depósito").click();
  cy.wait("@s3Deposit");
});

Then("devo ver o saldo atualizado e a movimentação registrada", () => {
  cy.contains("R$ 1.250,00").should("be.visible");
  cy.get('[role="dialog"]').should("not.exist");
});

When("tento confirmar um depósito inválido", () => {
  cy.get("#deposito-valor").clear().type("0");
  cy.contains('[role="dialog"] button', "Confirmar depósito").should("be.disabled");
});

Then("o depósito não deve chamar a API", () => {
  cy.get("@s3Deposit.all").should("have.length", 0);
});

Then("o saldo não deve ser alterado", () => {
  cy.contains("R$ 1.000,00").should("be.visible");
});

Given("que o dashboard da Sprint 3 mostra um saldo persistido", () => {
  cy.intercept("GET", `${API}/saldo`, { statusCode: 200, body: saldo(1250) }).as("s3PersistedBalance");
  visitWithUser("/dashboard");
  cy.wait("@s3PersistedBalance");
});

When("consulto o saldo após iniciar uma nova sessão", () => {
  cy.clearLocalStorage();
  visitWithUser("/dashboard");
  cy.wait("@s3PersistedBalance");
});

Then("devo ver o mesmo saldo disponível", () => {
  cy.contains("R$ 1.250,00").should("be.visible");
});

Then("registro a evidência da Sprint 3 do caso {string}", (caseId) => {
  cy.screenshot(`release2-sprint3/${caseId}`);
});
