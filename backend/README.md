# Backend — RendUP

Backend da plataforma RendUP, desenvolvido com Spring Boot, Java, Maven e PostgreSQL.

## Requisitos

- Java 18 ou superior;
- PostgreSQL instalado;
- Maven instalado.

O backend utiliza a porta `8080` e espera encontrar o PostgreSQL na porta `5432`.

## Criar o banco de dados

Por padrão, a aplicação utiliza as seguintes configurações:

| Configuração | Valor padrão |
| --- | --- |
| Banco | `rendup` |
| Usuário | `postgres` |
| Senha | `postgres` |
| URL | `jdbc:postgresql://localhost:5432/rendup` |

### Linux ou macOS

Com o PostgreSQL em execução, dentro do terminal entre no console do PostgreSQL:

```bash
sudo -u postgres psql
```

Crie o banco de dados:

```sql
CREATE DATABASE rendup;
\q
```

Se o banco já existir, não é necessário criá-lo novamente.

### Windows

Abra o **SQL Shell (psql)** ou um terminal com o comando `psql` disponível e execute:

```bash
psql -U postgres -c "CREATE DATABASE rendup;"
```

Informe a senha do usuário `postgres` quando solicitado.

### Usar credenciais diferentes

Se o usuário ou a senha do PostgreSQL forem diferentes dos valores padrão, configure as variáveis de ambiente antes de iniciar o backend.

No Linux ou macOS:

```bash
export SPRING_DATASOURCE_USERNAME=seu_usuario
export SPRING_DATASOURCE_PASSWORD=sua_senha
```

No PowerShell(Windows):

```powershell
$env:SPRING_DATASOURCE_USERNAME = "seu_usuario"
$env:SPRING_DATASOURCE_PASSWORD = "sua_senha"
```

Também é possível alterar a URL do banco, caso ele esteja em outro endereço:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/rendup
```

As tabelas são criadas e atualizadas automaticamente pelo Hibernate quando a aplicação é iniciada (`ddl-auto: update`).

## Executar o backend

No diretório raiz do projeto, execute:

```bash
cd backend
./mvnw spring-boot:run
```

No Windows, use:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Se o Maven estiver instalado, também é possível executar:

```bash
mvn spring-boot:run
```

O backend ficará disponível em [http://localhost:8080].

## Executar o projeto completo

Mantenha dois terminais abertos.

No primeiro terminal, inicie o backend:

```bash
cd backend
./mvnw spring-boot:run
```

No segundo terminal, inicie o frontend:

```bash
cd frontend
npm start
```

Depois, acesse [http://localhost:4200].

## Testes e build

Executar os testes:

```bash
./mvnw test
```

Gerar o arquivo `.jar`:

```bash
./mvnw clean package
```

Executar o `.jar` gerado:

```bash
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

## Configurações adicionais

- Porta do backend: `8080`;
- Origem permitida pelo CORS: `http://localhost:4200`;
- Expiração padrão do token JWT: `1200` segundos;
- Arquivo de configuração: `src/main/resources/application.yml`.
