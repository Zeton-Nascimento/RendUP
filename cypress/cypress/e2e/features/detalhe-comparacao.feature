# language: pt

@R2 @S2 @US-012
Funcionalidade: Detalhar e comparar produtos financeiros
  Como investidor
  Quero consultar detalhes e comparar alternativas
  Para tomar uma decisão de investimento mais informada

  @smoke @regressao
  Cenário: CT-037 - Detalhe e comparação exibem os principais critérios
    Dado que estou autenticado como investidor
    E a API do catálogo retorna dois produtos ativos
    E a API retorna o detalhe do produto e seus relacionados
    Quando acesso o catálogo de produtos
    E seleciono dois produtos para comparar
    Então devo ver a tabela de comparação com risco, liquidez e rentabilidade
    Quando acesso os detalhes do produto "produto-cdb-001"
    Então devo ver risco, liquidez, carência, vencimento, taxas e tributação
    E devo ver produtos relacionados
    E registro a evidência do caso "CT-037"

  @excecao
  Cenário: CT-038 - Produto inexistente apresenta estado de erro
    Dado que estou autenticado como investidor
    E a API do detalhe rejeita o produto inexistente
    Quando acesso os detalhes do produto "produto-inexistente"
    Então devo ver o erro de produto não encontrado
    E não devo ver uma opção de contratação
    E registro a evidência do caso "CT-038"
