# language: pt

@R2 @S2 @US-015
Funcionalidade: Validar entradas e controlar uso abusivo
  Como usuário da plataforma
  Quero que entradas inválidas e excesso de chamadas sejam controlados
  Para preservar a integridade e a disponibilidade do sistema

  @excecao @smoke @regressao
  Cenário: CT-033 - Cadastro com entrada inválida não é enviado
    Dado que estou na tela de cadastro
    Quando tento enviar o cadastro com campos obrigatórios vazios
    Então devo ver a validação de campo obrigatório
    E a API de cadastro não deve ser chamada
    E registro a evidência do caso "CT-033"

  @excecao @api
  Cenário: CT-034 - Excesso de chamadas de sincronização é rejeitado
    Dado que estou autenticado como administrador
    E a tela administrativa de produtos está preparada para limitar chamadas
    Quando solicito a sincronização do catálogo duas vezes
    Então a segunda solicitação deve ser rejeitada por excesso de chamadas
    E registro a evidência do caso "CT-034"
