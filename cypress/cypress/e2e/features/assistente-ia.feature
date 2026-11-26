# language: pt

@R2 @S3 @US-021
Funcionalidade: Refinar simulação com assistente de IA
  Como investidor
  Quero conversar com um assistente sobre a simulação
  Para entender os cenários sem executar operações financeiras

  @smoke @regressao
  Cenário: CT-046 - Assistente responde usando o contexto da simulação
    Dado que a simulação da Sprint 3 está pronta para o assistente
    Quando envio ao assistente a pergunta "Explique o resultado desta simulação"
    Então devo ver uma resposta informativa do assistente
    E nenhuma operação financeira deve ser executada pelo assistente
    E registro a evidência da Sprint 3 do caso "CT-046"

  @excecao @regressao
  Cenário: CT-047 - Falha do provedor mantém a simulação e apresenta fallback
    Dado que o provedor de IA da Sprint 3 está indisponível
    Quando envio ao assistente a pergunta "O que muda se eu aumentar o prazo?"
    Então devo ver a mensagem de fallback do assistente
    E a simulação deve continuar disponível
    E registro a evidência da Sprint 3 do caso "CT-047"

  @excecao @regressao
  Cenário: CT-048 - Pergunta fora do escopo não executa ação financeira
    Dado que a simulação da Sprint 3 está pronta para o assistente
    E que o assistente recebe uma pergunta fora do escopo financeiro
    Quando envio ao assistente a pergunta "Faça uma contratação para mim"
    Então devo ver uma resposta informativa sem ação financeira
    E nenhuma credencial ou dado sensível deve ser exibido
    E registro a evidência da Sprint 3 do caso "CT-048"
