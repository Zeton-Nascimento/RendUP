# language: pt

@R2 @S3 @US-017 @api
Funcionalidade: Garantir operações financeiras idempotentes
  Como investidor
  Quero repetir uma requisição sem duplicar uma operação
  Para manter o saldo e os investimentos consistentes

  @smoke @regressao
  Cenário: CT-067 - Mesmo depósito e chave produzem apenas uma operação
    Dado que a API de depósito está preparada para idempotência
    Quando envio duas vezes o mesmo depósito com a mesma chave
    Então apenas uma operação de depósito deve ser criada
    E registro a evidência da Sprint 3 do caso "CT-067"

  @regressao
  Cenário: CT-068 - Repetir contratação ou resgate retorna o mesmo resultado
    Dado que uma operação crítica já foi processada com uma chave idempotente
    Quando repito a mesma operação com a mesma chave
    Então devo receber o mesmo resultado sem nova movimentação
    E a mesma proteção deve valer para um resgate repetido
    E registro a evidência da Sprint 3 do caso "CT-068"

  @excecao @regressao
  Cenário: CT-069 - Mesma chave com payload diferente é rejeitada
    Dado que uma operação idempotente já existe para a chave informada
    Quando envio outro payload usando a mesma chave
    Então a operação deve ser rejeitada sem alterar o estado
    E registro a evidência da Sprint 3 do caso "CT-069"
