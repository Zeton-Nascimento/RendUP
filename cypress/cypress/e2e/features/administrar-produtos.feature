# language: pt

@R2 @S2 @US-013
Funcionalidade: Administrar e importar produtos financeiros
  Como administrador
  Quero administrar o catálogo de produtos
  Para manter informações válidas e controlar a disponibilidade dos itens

  @smoke @regressao
  Cenário: CT-039 - Administrador inativa um produto com confirmação
    Dado que estou autenticado como administrador
    E a tela administrativa possui um produto ativo
    Quando confirmo a inativação do produto
    Então o produto deve aparecer como inativo
    E devo ver a mensagem de inativação concluída
    E registro a evidência do caso "CT-039"

  @excecao @regressao
  Cenário: CT-040 - Investidor não acessa a administração de produtos
    Dado que estou autenticado como investidor
    Quando tento acessar a administração de produtos
    Então devo ser redirecionado para o meu perfil
    E registro a evidência do caso "CT-040"
