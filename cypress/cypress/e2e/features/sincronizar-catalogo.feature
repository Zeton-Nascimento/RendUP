# language: pt

@R2 @S2 @US-014
Funcionalidade: Sincronizar catálogo com fonte externa mockada
  Como administrador
  Quero sincronizar o catálogo com uma fonte mockada formalizada
  Para atualizar os produtos sem depender de uma integração externa real

  @smoke @regressao
  Cenário: CT-041 - Sincronização mockada conclui e informa o resultado
    Dado que estou autenticado como administrador
    E a tela administrativa possui uma fonte mockada preparada
    Quando sincronizo o catálogo mockado
    Então devo ver a sincronização concluída
    E devo ver a origem e os totais da sincronização
    E registro a evidência do caso "CT-041"

  @excecao
  Cenário: CT-042 - Falha da fonte mockada preserva o estado anterior
    Dado que estou autenticado como administrador
    E a fonte mockada retorna uma falha controlada
    Quando sincronizo o catálogo mockado
    Então devo ver que a sincronização não foi aplicada
    E devo ver uma mensagem de falha da sincronização
    E registro a evidência do caso "CT-042"
