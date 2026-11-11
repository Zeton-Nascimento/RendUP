# Testes da Release 2 e Sprint 2

Esta pasta reúne os testes automatizados do RendUP. Ela mantém os testes criados na Sprint 1 e também contém os novos testes da Release 2, feitos com Cypress e Cucumber.

Os testes apenas verificam o comportamento do sistema. Eles não alteram os arquivos da aplicação dentro de `src/`.

## Testes mantidos da Sprint 1

Os testes da Sprint 1 continuam disponíveis para verificar cadastro, login, controle de acesso por perfil, consulta e atualização do perfil, troca de senha, inativação de usuários, mensagens de erro e estados vazios.

Também existem testes de API para situações como acesso sem token, cadastro, consulta do próprio perfil, falta de permissão, atualização de dados, e-mail duplicado e troca de senha.

O teste de inativação administrativa é executado quando o e-mail e a senha do administrador são informados no terminal.

Os principais arquivos da Sprint 1 são:

`cypress/e2e/sprint1-ui.cy.js`

`cypress/api/sprint1-api.cy.js`

`cypress/api/admin-inativacao.cy.js`

## Testes da Sprint 2

Os testes da Sprint 2 cobrem as seguintes histórias:

US-015: validar entradas e controlar uso abusivo, no arquivo `validacao-abuso.feature`.

US-011: explorar e filtrar produtos financeiros, no arquivo `catalogo-produtos.feature`.

US-012: detalhar e comparar produtos financeiros, no arquivo `detalhe-comparacao.feature`.

US-013: administrar e importar produtos financeiros, no arquivo `administrar-produtos.feature`.

US-014: sincronizar o catálogo com uma fonte externa mockada, no arquivo `sincronizar-catalogo.feature`.

Os arquivos de teste mantêm os identificadores CT-033 até CT-042 para facilitar a rastreabilidade com o plano de testes.

## Arquivos importantes

`cypress.cucumber.config.cjs` configura a execução dos arquivos `.feature`.

`cypress/e2e/features/` contém os cenários escritos em Gherkin.

`cypress/support/step_definitions/sprint2.steps.js` contém os passos usados pelos cenários Cucumber, os dados mockados e o registro das screenshots.

`cypress/api/sprint2-api.cy.js` contém os testes que fazem chamadas reais ao backend.

`cypress.api.config.cjs` configura os testes de API.

`cypress/evidence/` guarda os relatórios gerados e o registro da regressão.

## Como os testes funcionam

Os testes de interface usam `cy.intercept()` para simular as respostas da API. Isso permite testar a tela sem depender de todos os dados do backend.

Os testes de API usam o backend real e precisam que o Spring Boot e o PostgreSQL estejam funcionando.

Por isso, os testes de interface e os testes de API são complementares. Os testes de interface verificam a experiência do usuário, enquanto os testes de API verificam autenticação, autorização, validações, códigos HTTP e regras do backend.

## Requisitos

Antes de executar os testes, instale o Node.js e o npm e instale as dependências dentro desta pasta:

```bash
npm install
```

Para os testes de interface, o frontend deve estar disponível em `http://localhost:4200`.

Para os testes de API, o backend deve estar disponível em `http://localhost:8080` e o PostgreSQL deve estar iniciado.

## Executando no Linux

Em um terminal, inicie o backend:

```bash
cd backend-novo-sprint1
./mvnw spring-boot:run
```

Em outro terminal, inicie o frontend:

```bash
cd frontend-novo-sprint1
npm install
npm start
```

Em um terceiro terminal, entre nesta pasta e execute os testes:

```bash
cd cypress
npm install
```

Para executar os cenários Cucumber da Sprint 2:

```bash
npm run test:cucumber
```

Para executar os testes de API da Sprint 2:

```bash
npm run test:api:sprint2
```

Para executar os testes da Sprint 1:

```bash
npm run test:e2e
npm run test:e2e:open
npm run test:api
npm run test:api:open
```

Para executar somente os cenários de smoke:

```bash
npm run test:smoke
```

Para executar a regressão da Release 2:

```bash
npm run test:regressao
```

Para abrir o Cypress e executar os testes de forma interativa:

```bash
npm run test:cucumber:open
```

Para executar apenas uma funcionalidade:

```bash
npx cypress run --config-file cypress.cucumber.config.cjs --spec "cypress/e2e/features/catalogo-produtos.feature"
```

## Executando no Windows PowerShell

```powershell
cd .\cypress
npm install
npm run test:cucumber
npm run test:api:sprint2
```

Para executar os testes da Sprint 1:

```powershell
npm run test:e2e
npm run test:e2e:open
npm run test:api
npm run test:api:open
```

Para executar os testes administrativos da API, informe as credenciais somente no terminal atual:

```powershell
$env:RENDUP_ADMIN_EMAIL = "admin@investimentos.com"
$env:RENDUP_ADMIN_PASSWORD = "HDzcVpSOYsG+aZAE"
npm run test:api:sprint2
```

Sem essas variáveis, os testes que precisam de administrador aparecem como pendentes.

Para executar o teste opcional de inativação administrativa da Sprint 1, use as mesmas variáveis e rode:

```powershell
npm run test:api
```

## Tags usadas nos testes Cucumber

`@R2` e `@S2` identificam os cenários da Release 2 e da Sprint 2.

`@US-011` até `@US-015` relacionam cada cenário à sua história de usuário.

`@smoke` identifica os cenários mais importantes para uma verificação rápida.

`@regressao` identifica os cenários que devem ser repetidos para confirmar que as alterações não quebraram os fluxos existentes.

`@excecao` identifica cenários de erro, validação, limite de uso ou acesso negado.

`@api` identifica cenários relacionados à proteção ou limitação da API.

Para executar uma tag específica:

```bash
npx cypress run --config-file cypress.cucumber.config.cjs --env tags="@smoke"
npx cypress run --config-file cypress.cucumber.config.cjs --env tags="@regressao"
```

## Onde ficam as evidências

Depois da execução dos testes Cucumber, os vídeos ficam em:

`cypress/cypress/videos/`

As screenshots ficam em:

`cypress/cypress/screenshots/`

Os relatórios Cucumber ficam em:

`cypress/evidence/`

O registro manual da regressão fica em:

`cypress/evidence/regressao-release2-sprint2.txt`

Se quiser manter as evidências de execuções anteriores, use `trashAssetsBeforeRuns=false` no comando do Cypress.

## Observações

Os testes de interface usam mocks.

Os testes de API precisam do backend e do banco de dados iniciados.

