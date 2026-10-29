# RendUP — testes Cypress da Sprint 1 (pacote independente)

Este diretório contém somente artefatos de QA: configurações próprias do Cypress, specs e instruções. **Não altera `src/`, `package.json`, `package-lock.json`, o backend nem qualquer arquivo existente do projeto-base-gerencia.** Execute a suíte a partir deste diretório, em paralelo com a aplicação já iniciada.

## O que está coberto

- **Interface com API simulada:** 15 cenários para cadastro, login, controle de rota por perfil, consulta e atualização de perfil, troca de senha, inativação, mensagens, erro com nova tentativa e estado vazio.
- **API real:** 7 cenários básicos para 401 sem token, cadastro, resposta sem segredos, consulta do próprio perfil, 403 para investidor, persistência de atualização, e-mail duplicado e troca de senha.
- **Inativação pela API real (opcional):** 1 cenário adicional executado somente quando `RENDUP_ADMIN_EMAIL` e `RENDUP_ADMIN_PASSWORD` estiverem configuradas no ambiente local. O cenário cria um investidor sintético, inativa esse usuário pelo endpoint administrativo e confirma que um novo login é recusado.

A suíte de interface usa `cy.intercept()` e não prova sozinha a segurança real do backend. A suíte de API exige backend e PostgreSQL de teste. Os resultados começam como **Não executado** até a execução real.

## Requisitos

- Node.js e npm compatíveis com o projeto.
- Frontend iniciado em `http://localhost:4200` para a suíte de interface.
- Backend iniciado em `http://localhost:8080` e PostgreSQL de teste para a suíte de API.
- Cypress instalado pela execução de `npm install` dentro deste diretório independente.

## Executar no Windows PowerShell

Terminal 1 — backend e banco local/de teste:

```powershell
cd .\backend-novo-sprint1
.\mvnw.cmd spring-boot:run
```

Terminal 2 — frontend:

```powershell
cd .\frontend-novo-sprint1
npm install
npm start
```

Terminal 3 — suíte de UI (rode a partir da pasta `qa-cypress-rendup`):

```powershell
npm install
npm run test:e2e
```

Modo interativo:

```powershell
npm run test:e2e:open
```

Suíte contra a API real (somente banco local/de teste):

```powershell
npm run test:api
```

Para incluir o cenário de inativação administrativa real, defina no terminal as variáveis `RENDUP_ADMIN_EMAIL` e `RENDUP_ADMIN_PASSWORD` com as credenciais de um administrador **do ambiente de teste** e então execute `npm run test:api`. Não grave credenciais em arquivos, não as envie ao Git e não use produção. Sem as duas variáveis, o spec administrativo é excluído automaticamente.

## Evidências

- A suíte UI grava vídeos em `cypress/videos/` e screenshots de falha em `cypress/screenshots/`.
- A suíte API não grava vídeo; screenshots de falha podem ser geradas pelo Cypress.
- Não anexe tokens, senhas, CPF completo, dados bancários nem dados reais em tickets ou no plano de qualidade.
- Os testes API criam contas sintéticas que permanecem no banco de teste. Use uma base descartável/local e remova as contas de teste conforme o procedimento da equipe.

## Limites e rastreabilidade

Os testes usam os seletores já presentes no código (IDs de login/cadastro, labels, títulos e classes). Nenhum `data-cy` precisou ser adicionado à aplicação. A lista de US e seus títulos é a do quadro Sprint 1 enviado pelo QA. Alguns arquivos `docs/issues` e o plano PDF anterior atribuem outros significados a US-008, US-009 e US-010; essa rastreabilidade deve ser reconciliada por PO/SM antes do fechamento da release.
