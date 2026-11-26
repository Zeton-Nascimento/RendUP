# language: pt

@R2 @S3 @US-020
Funcionalidade: Informar origem e metodologia dos resultados
  Como investidor
  Quero entender de onde vêm os resultados da simulação
  Para interpretar os valores com transparência

  @smoke @regressao
  Cenário: CT-043 - Resultado apresenta origem, data de referência e metodologia
    Dado que a tela de simulação da Sprint 3 está pronta com dois produtos
    Quando executo uma simulação da Sprint 3
    Então devo ver a origem, a data de referência e a metodologia
    E registro a evidência da Sprint 3 do caso "CT-043"

  @smoke @regressao
  Cenário: CT-044 - Resultado apresenta limitações e aviso de estimativa
    Dado que a simulação da Sprint 3 possui limitações documentadas
    Quando executo uma simulação da Sprint 3
    Então devo ver as limitações e o aviso de estimativa
    E registro a evidência da Sprint 3 do caso "CT-044"

  @excecao @regressao
  Cenário: CT-045 - Metadados incompletos impedem resultado definitivo
    Dado que a simulação da Sprint 3 recebe metadados incompletos
    Quando executo uma simulação da Sprint 3
    Então o resultado deve permanecer não definitivo
    E registro a evidência da Sprint 3 do caso "CT-045"
