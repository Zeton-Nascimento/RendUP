# language: pt

@R2 @S3 @US-022
Funcionalidade: Recuperar e compartilhar uma simulação
  Como investidor
  Quero salvar, recuperar e compartilhar um resumo
  Para continuar a análise sem expor dados sensíveis

  @excecao @regressao
  Cenário: CT-064 - Simulação inexistente, inválida ou de outro usuário não é recuperada
    Dado que a API de simulações rejeita um identificador inválido
    Quando tento recuperar a simulação inexistente
    Então devo ver que a simulação não foi encontrada
    E nenhum dado de outro usuário deve ser exibido
    E registro a evidência da Sprint 3 do caso "CT-064"

  @regressao
  Cenário: CT-065 - Compartilhamento contém resumo seguro sem credenciais ou PII desnecessária
    Dado que existe uma simulação salva para compartilhar
    Quando compartilho o resumo da simulação
    Então devo ver a confirmação de resumo copiado
    E o resumo não deve conter credenciais, token ou PII desnecessária
    E registro a evidência da Sprint 3 do caso "CT-065"

  @smoke @regressao
  Cenário: CT-066 - Histórico recupera os mesmos parâmetros e resultados
    Dado que existe uma simulação salva no histórico
    Quando abro a simulação salva
    Então devo ver os parâmetros e os resultados recuperados
    E registro a evidência da Sprint 3 do caso "CT-066"
