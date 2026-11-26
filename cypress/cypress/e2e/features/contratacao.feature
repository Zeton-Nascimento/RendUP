# language: pt

@R2 @S3 @US-024
Funcionalidade: Visualizar prévia e contratar investimento
  Como investidor
  Quero revisar uma aplicação antes de confirmá-la
  Para contratar somente produtos compatíveis e com saldo disponível

  @smoke @regressao
  Cenário: CT-052 - Investidor compatível contrata com saldo suficiente
    Dado que a página de contratação tem saldo suficiente e produto compatível
    Quando consulto a prévia da contratação
    E confirmo a contratação
    Então devo ver o investimento contratado com sucesso
    E registro a evidência da Sprint 3 do caso "CT-052"

  @excecao @regressao
  Cenário: CT-053 - Saldo insuficiente não persiste contratação
    Dado que a página de contratação possui saldo insuficiente
    Quando consulto a prévia da contratação
    Então devo ver que a contratação está impedida
    E a API de contratação não deve ser chamada
    E registro a evidência da Sprint 3 do caso "CT-053"

  @excecao @regressao
  Cenário: CT-054 - Produto incompatível ou sem consentimento é rejeitado
    Dado que a página de contratação possui produto incompatível
    Quando consulto a prévia da contratação
    Então devo ver que a contratação está impedida
    E devo ver mensagens para produto incompatível, inativo e sem consentimento
    E a API de contratação não deve ser chamada
    E registro a evidência da Sprint 3 do caso "CT-054"
