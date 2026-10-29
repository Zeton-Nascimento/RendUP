# Frontend — RendUP

Frontend da plataforma RendUP, desenvolvido com Angular.

## Requisitos

- Node.js instalado;
- npm instalado;
- Backend do RendUP em execução em `http://localhost:8080` para utilizar as funcionalidades que acessam a API.

## Instalação

No diretório raiz do projeto, execute:

```bash
cd frontend
npm install
```

O comando `npm install` instala as dependências do projeto e deve ser executado novamente quando o arquivo `package.json` for alterado.

## Executar em modo de desenvolvimento

Ainda dentro do diretório `frontend`, execute:

```bash
npm start
```

Depois, acesse [http://localhost:4200] no navegador.

Para executar o projeto completo, consulte as instruções do [backend](../backend/README.md) e mantenha o backend e o frontend em terminais separados.

## Comandos disponíveis

| Comando | Descrição |
| --- | --- |
| `npm start` | Inicia o servidor de desenvolvimento na porta `4200`. |
| `npm run build` | Gera a versão compilada da aplicação na pasta `dist/`. |
| `npm test` | Executa os testes unitários. |
| `npm run test:coverage` | Executa os testes e gera o relatório de cobertura. |

## Endereço da API

Em ambiente local, o frontend utiliza a API no endereço:

```text
http://localhost:8080/api
```

Esse endereço está configurado nos arquivos de ambiente em `src/environments/`.
