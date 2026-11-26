# language: pt

@R2 @S3 @US-026
Funcionalidade: Consolidar regras financeiras e tributárias
  Como administrador
  Quero documentar regras aprovadas por evento financeiro
  Para manter cálculos e decisões consistentes e auditáveis

  @smoke @regressao
  Cenário: CT-058 - Administrador registra regra aprovada com todos os eventos
    Dado que a tela de regras financeiras está pronta para edição
    Quando preencho uma regra aprovada com os cinco eventos obrigatórios
    E salvo a regra financeira
    Então devo ver que a regra foi salva e está pronta para aplicação
    E registro a evidência da Sprint 3 do caso "CT-058"

  @excecao @regressao
  Cenário: CT-059 - Regra ausente, pendente ou conflitante bloqueia processamento
    Dado que a validação da regra financeira está pendente
    Quando verifico o documento de regras financeiras
    Então devo ver que o processamento está bloqueado
    E devo ver a pendência sinalizada
    E devo ver que regra ausente, pendente e conflitante bloqueiam o processamento
    E registro a evidência da Sprint 3 do caso "CT-059"

  @excecao @regressao
  Cenário: CT-060 - Evento ou fórmula incompleta não é persistido
    Dado que a tela de regras financeiras está pronta para edição
    Quando tento salvar uma regra com fórmula incompleta
    Então a API de regras financeiras não deve ser chamada
    E devo ver a validação do preenchimento obrigatório
    E registro a evidência da Sprint 3 do caso "CT-060"
