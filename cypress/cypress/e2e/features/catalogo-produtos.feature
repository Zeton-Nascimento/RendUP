# language: pt

@R2 @S2 @US-011
Funcionalidade: Explorar e filtrar produtos financeiros
  Como investidor
  Quero explorar e filtrar o catálogo
  Para encontrar produtos compatíveis com meus objetivos

  @smoke @regressao
  Cenário: CT-035 - Filtros e ordenação retornam somente produtos ativos
    Dado que estou autenticado como investidor
    E a API do catálogo retorna produtos ativos
    Quando acesso o catálogo de produtos
    E aplico o filtro de nome "CDB" e risco "BAIXO"
    Então a consulta deve enviar os filtros selecionados
    E devo visualizar apenas produtos ativos do catálogo
    E devo visualizar a paginação do catálogo
    E registro a evidência do caso "CT-035"

  @excecao @regressao
  Cenário: CT-036 - Filtro sem resultados apresenta estado vazio
    Dado que estou autenticado como investidor
    E a API do catálogo não encontra produtos para o filtro
    Quando acesso o catálogo de produtos
    E aplico o filtro de nome "Produto inexistente"
    Então devo ver o estado vazio do catálogo
    E registro a evidência do caso "CT-036"
