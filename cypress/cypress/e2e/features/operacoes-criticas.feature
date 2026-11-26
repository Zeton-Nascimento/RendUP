# language: pt

@R2 @S3 @US-018
Funcionalidade: Confirmar operações críticas antes da execução
  Como investidor
  Quero confirmar contratação e resgate antes da execução
  Para evitar operações acidentais

  @smoke @regressao
  Cenário: CT-049 - Contratação exige confirmação explícita
    Dado que a prévia de contratação está pronta para confirmação
    Então a operação de contratação deve exigir confirmação explícita
    E a operação de resgate também deve exigir confirmação explícita
    E registro a evidência da Sprint 3 do caso "CT-049"

  @excecao @regressao
  Cenário: CT-050 - Cancelar a confirmação não executa a operação
    Dado que a prévia de contratação está pronta para confirmação
    Quando cancelo a confirmação da contratação
    Então a API de contratação não deve ser chamada
    E nenhum investimento deve ser criado
    E registro a evidência da Sprint 3 do caso "CT-050"

  @regressao
  Cenário: CT-051 - Confirmação apresenta retorno e evita repetição acidental
    Dado que a prévia de contratação está pronta para confirmação
    Quando confirmo a operação de contratação
    Então devo ver o retorno de contratação concluída
    E a confirmação não deve ser repetida acidentalmente
    E registro a evidência da Sprint 3 do caso "CT-051"
