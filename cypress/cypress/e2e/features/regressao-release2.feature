# language: pt

@R2 @S2 @regressao
Funcionalidade: Regressão crítica da Release 2
  Como equipe de QA
  Quero repetir os fluxos críticos da Release 2
  Para garantir que as entregas principais continuem funcionando em conjunto

  @smoke
  Cenário: CT-REG-001 - Investidor consulta o catálogo e compara produtos
    Dado que estou autenticado como investidor
    E a API do catálogo retorna dois produtos ativos
    Quando acesso o catálogo de produtos
    E seleciono dois produtos para comparar
    Então devo ver a tabela de comparação com risco, liquidez e rentabilidade
    E registro a evidência do caso "CT-REG-001"

  @smoke
  Cenário: CT-REG-002 - Administrador sincroniza o catálogo mockado
    Dado que estou autenticado como administrador
    E a tela administrativa possui uma fonte mockada preparada
    Quando sincronizo o catálogo mockado
    Então devo ver a sincronização concluída
    E registro a evidência do caso "CT-REG-002"

  Cenário: CT-REG-003 - Cadastro inválido permanece bloqueado
    Dado que estou na tela de cadastro
    Quando tento enviar o cadastro com campos obrigatórios vazios
    Então devo ver a validação de campo obrigatório
    E a API de cadastro não deve ser chamada
    E registro a evidência do caso "CT-REG-003"
