# language: pt

@R2 @S3 @US-025
Funcionalidade: Consultar prévia e confirmar resgate
  Como investidor
  Quero consultar os descontos antes de resgatar
  Para confirmar apenas operações possíveis

  @smoke @regressao
  Cenário: CT-055 - Prévia parcial apresenta bruto, impostos, descontos e líquido
    Dado que existe um investimento disponível para resgate parcial
    Quando consulto a prévia de um resgate parcial
    Então devo ver os valores bruto, imposto, desconto, líquido e remanescente
    E registro a evidência da Sprint 3 do caso "CT-055"

  @smoke @regressao
  Cenário: CT-056 - Resgate total confirmado encerra investimento e credita líquido
    Dado que existe um investimento disponível para resgate total
    Quando consulto a prévia de um resgate total
    E confirmo o resgate
    Então devo ver o resgate confirmado e o novo saldo
    E registro a evidência da Sprint 3 do caso "CT-056"

  @excecao @regressao
  Cenário: CT-057 - Resgate inválido não altera investimento nem saldo
    Dado que o resgate da Sprint 3 está bloqueado por uma regra inválida
    Quando consulto a prévia de um resgate inválido
    Então devo ver que o resgate está impedido
    E devo ver que carência, bloqueio, valor acima do disponível e inexistência impedem o resgate
    E a API de resgate não deve ser chamada
    E registro a evidência da Sprint 3 do caso "CT-057"
