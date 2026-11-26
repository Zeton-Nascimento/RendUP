# language: pt

@R2 @S3 @US-023
Funcionalidade: Depositar e consultar saldo disponível
  Como investidor
  Quero depositar e consultar meu saldo
  Para usar o valor disponível nas operações financeiras

  @smoke @regressao
  Cenário: CT-070 - Depósito válido atualiza saldo e movimentação
    Dado que o dashboard da Sprint 3 está pronto para depósito
    Quando confirmo um depósito válido de "250"
    Então devo ver o saldo atualizado e a movimentação registrada
    E registro a evidência da Sprint 3 do caso "CT-070"

  @excecao @regressao
  Cenário: CT-071 - Depósito inválido não altera saldo
    Dado que o dashboard da Sprint 3 está pronto para depósito
    Quando tento confirmar um depósito inválido
    Então o depósito não deve chamar a API
    E o saldo não deve ser alterado
    E registro a evidência da Sprint 3 do caso "CT-071"

  @regressao
  Cenário: CT-072 - Saldo permanece disponível após novo login
    Dado que o dashboard da Sprint 3 mostra um saldo persistido
    Quando consulto o saldo após iniciar uma nova sessão
    Então devo ver o mesmo saldo disponível
    E registro a evidência da Sprint 3 do caso "CT-072"
