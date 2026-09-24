# Prompt para reprodução por outra IA

Você é uma IA responsável por implementar uma aplicação web completa chamada **QR Text Capture**.

## Objetivo

Crie uma aplicação genérica para ler textos de QR Codes presentes em imagens. Não inclua regras de negócio, terminologia ou funcionalidades relacionadas a eleições, boletins, urnas, seções eleitorais ou apuração. O sistema deve permitir que um usuário selecione manualmente uma região da imagem, visualize o recorte, leia o QR Code, confira o texto e salve o resultado em um histórico.

## Stack obrigatória

Use:

- Java 21;
- Spring Boot 3.5.x;
- Spring MVC;
- Thymeleaf;
- Spring Security;
- SQLite 3;
- `JdbcTemplate` e SQL explícito;
- nenhum JPA, Hibernate ou ORM;
- Spring Mail opcional para confirmação de e-mail;
- BCrypt para senhas;
- ZXing 3.5.x no backend;
- `jsQR` no navegador para a leitura do recorte do fluxo principal;
- Maven;
- HTML, CSS responsivo e JavaScript sem framework obrigatório.

## Requisitos funcionais

Implemente:

1. Autoinscrição com nome, e-mail e senha.
2. Armazenamento da senha somente como hash BCrypt.
3. Confirmação por e-mail com token de uso único.
4. Bloqueio do login enquanto a conta não estiver confirmada.
5. Login e logout com Spring Security.
6. CRUD administrativo de usuários.
7. Histórico de capturas por usuário.
8. Exclusão de uma captura somente pelo usuário proprietário.
9. Leitura de QR Code a partir de imagem.
10. Persistência do texto reconhecido, nome da origem e data/hora.
11. Usuário de teste opcional, ativado por variável de ambiente.
12. Layout Thymeleaf com fragmentos reutilizáveis para cabeçalho e alertas.

## Fluxo visual obrigatório do QR Code

A tela `/capturas` deve seguir esta sequência, sem atalhos automáticos:

1. O usuário seleciona uma imagem.
2. A imagem completa aparece no Canvas 1.
3. Nenhum retângulo de seleção deve ser criado automaticamente.
4. O usuário clica em **Selecionar região**.
5. O usuário arrasta manualmente um retângulo no Canvas 1.
6. A marcação azul pode aparecer somente como guia visual.
7. O usuário clica em **Cortar**.
8. O recorte aparece no Canvas 2.
9. O usuário confere o Canvas 2.
10. O usuário clica em **Ler QR Code**.
11. O texto reconhecido aparece em uma `textarea` somente leitura.
12. O usuário clica em **Salvar no banco**.
13. O histórico é atualizado sem exigir recarregamento manual adicional.

## Regra crítica de preservação da imagem

O Canvas 2 deve ser gerado diretamente da imagem original carregada no navegador. A imagem usada como origem não pode ser o Canvas 1 quando ele contiver a marcação azul.

O Canvas 1 pode ser visualmente reduzido para caber na tela. Para obter o recorte, converta a seleção usando a razão entre as dimensões naturais da imagem e as dimensões exibidas do Canvas 1. Use `naturalWidth` e `naturalHeight` da imagem original.

O Canvas 2 deve:

- receber largura e altura originais do recorte;
- usar `drawImage` a partir da imagem original;
- usar `imageSmoothingEnabled=false` durante o recorte;
- não aplicar redimensionamento adicional;
- não aplicar correção de cor;
- não aplicar brilho ou contraste;
- não aplicar blur;
- não aplicar nitidez;
- não aplicar desentortamento;
- não aplicar inversão;
- não receber overlay azul;
- ser usado diretamente pelo `jsQR`.

O zoom deve ser exclusivamente o zoom nativo do navegador ou do celular. Não crie botão, slider, `zoomRange`, `setZoom` ou lógica própria de zoom na aplicação.

## Responsividade

Inclua `<meta name="viewport" content="width=device-width, initial-scale=1">`. Em telas estreitas, empilhe as colunas do leitor, permita rolagem horizontal do Canvas quando necessário e mantenha os botões utilizáveis por toque. O CSS não deve forçar o Canvas 2 a uma largura que altere seu bitmap; use um contêiner com rolagem.

## Segurança e CSRF

Mantenha CSRF habilitado. Inclua o token nos formulários Thymeleaf. Para o botão de exclusão, implemente:

- formulário `POST` em `/capturas/{id}/excluir`;
- validação do proprietário no backend;
- endpoint autenticado `GET /csrf-token` que retorne o nome do parâmetro e o token atual;
- interceptação do formulário de exclusão no JavaScript;
- atualização do token antes do envio;
- envio normal do formulário depois da renovação.

A solução deve evitar HTTP 403 quando a página estiver aberta há algum tempo ou quando a sessão tiver sido renovada.

## Modelo SQLite

Crie `schema.sql` com:

```sql
CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  email TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  role TEXT NOT NULL DEFAULT 'USER',
  enabled INTEGER NOT NULL DEFAULT 0,
  confirmation_token TEXT,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS qr_captures (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  content TEXT NOT NULL,
  source_name TEXT,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

## Configuração

Use `application.yml` com suporte a:

- `DB_FILE`, padrão `./data/qr-text-capture.db`;
- `APP_BASE_URL`, padrão `http://localhost:8080`;
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`;
- `APP_TEST_USER_ENABLED`;
- `APP_TEST_USER_NAME`;
- `APP_TEST_USER_EMAIL`;
- `APP_TEST_USER_PASSWORD`;
- `SERVER_PORT`.

Ative `server.forward-headers-strategy=framework` para execução atrás de proxy HTTPS.

## Usuário de teste

Quando `APP_TEST_USER_ENABLED=true`, crie ou sincronize uma conta confirmada. Valores padrão:

- e-mail: `teste@localhost`;
- senha: `Teste123!`;
- nome: `Usuário de Testes`.

A operação deve ser idempotente e não deve enviar e-mail de confirmação. Documente claramente que esse recurso é apenas para desenvolvimento.

## Estrutura sugerida

Use classes separadas para:

- aplicação principal;
- configuração de segurança;
- inicializador do usuário de teste;
- modelos;
- `UserRepository`;
- `CaptureRepository`;
- `AuthService`;
- `QrService`;
- controller web.

Use templates separados para home, login, cadastro, confirmação, capturas, usuários e fragmentos. Use `static/js/qr-reader.js` e `static/css/app.css`.

## Validação obrigatória

Execute:

```bash
mvn clean test package
```

Depois inicie:

```bash
APP_TEST_USER_ENABLED=true SERVER_PORT=8080 java -jar target/qr-text-capture-0.0.1-SNAPSHOT.jar
```

Valide:

- HTTP 200 na página inicial;
- login com o usuário de teste;
- upload de uma imagem QR real;
- Canvas 1 visível após seleção;
- seleção manual sem retângulo automático;
- Canvas 2 criado somente após clicar em **Cortar**;
- reconhecimento por **Ler QR Code**;
- texto em textarea somente leitura;
- salvamento no histórico;
- exclusão com retorno HTTP 302 e remoção no SQLite;
- exclusão sem HTTP 403 por token CSRF antigo;
- layout em uma coluna em viewport estreito;
- ausência de qualquer botão ou código próprio de zoom.

## Entregáveis

Entregue:

1. O projeto Maven compilável.
2. O `README.md` com instruções completas para Linux e Windows.
3. A especificação técnica do projeto.
4. Teste automatizado de contexto Spring.
5. Instruções sobre SMTP, usuário de teste, banco SQLite e promoção do primeiro administrador.

Ao final, informe os comandos executados, os testes aprovados, as limitações conhecidas e os arquivos criados.
