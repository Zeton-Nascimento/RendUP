# language: pt

@R2 @S3 @US-019
Funcionalidade: Simular investimentos e comparar cenários
  Como investidor
  Quero simular aportes em vários produtos
  Para comparar cenários antes de investir

  @smoke @regressao
  Cenário: CT-061 - Simulação compara produtos com aporte inicial e mensal
    Dado que a tela de simulação da Sprint 3 está pronta com dois produtos
    Quando executo uma simulação com aporte inicial e mensal
    Então devo ver o ranking e a comparação dos cenários
    E registro a evidência da Sprint 3 do caso "CT-061"

  @excecao @regressao
  Cenário: CT-062 - Simulação sem produto ou com parâmetro inválido é rejeitada
    Dado que a tela de simulação da Sprint 3 está pronta sem produto selecionado
    Quando tento executar uma simulação inválida
    Então devo ver a validação da simulação
    E a API de simulação não deve ser chamada
    E registro a evidência da Sprint 3 do caso "CT-062"

  @regressao
  Cenário: CT-063 - Perfil do investidor altera ranking e explicação
    Dado que a tela de simulação da Sprint 3 está pronta com dois produtos
    Quando executo a simulação para os perfis conservador e arrojado
    Então devo ver ranking personalizado para cada perfil
    E registro a evidência da Sprint 3 do caso "CT-063"
