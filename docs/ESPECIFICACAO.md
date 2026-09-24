# Especificação técnica — QR Text Capture

## 1. Objetivo

O **QR Text Capture** é uma aplicação web para selecionar uma imagem, marcar manualmente a região de um QR Code, gerar um recorte para conferência, reconhecer o conteúdo textual e salvar o resultado em um histórico por usuário. O projeto é genérico e não contém regras de negócio relacionadas a eleições, boletins, seções eleitorais ou qualquer outro domínio específico.

O sistema deve preservar os pixels da imagem original no recorte usado para leitura. A interface pode redimensionar visualmente a imagem para caber na tela, mas essa representação serve apenas para orientar a seleção. O Canvas 2 é criado com as coordenadas e dimensões da imagem original.

## 2. Requisitos tecnológicos

A implementação utiliza Java 21 e Spring Boot 3.5.6. O acesso ao SQLite é feito com `JdbcTemplate` e SQL explícito. O projeto não usa JPA, Hibernate, ORM ou camada de entidades persistentes.

A camada de apresentação usa Thymeleaf, CSS responsivo e JavaScript no navegador. A leitura do recorte na interface usa a biblioteca `jsQR` carregada por CDN. O backend também contém ZXing para o endpoint de decodificação tradicional baseado em upload multipart.

As principais dependências são:

| Componente | Versão ou tecnologia | Finalidade |
|---|---|---|
| Java | 21 | Plataforma de execução |
| Spring Boot | 3.5.6 | Framework da aplicação |
| Spring Web | fornecido pelo Boot | Controllers e HTTP |
| Thymeleaf | fornecido pelo Boot | Templates server-side |
| Spring Security | fornecido pelo Boot | Autenticação, autorização e CSRF |
| Spring JDBC | fornecido pelo Boot | Acesso SQL sem ORM |
| SQLite JDBC | 3.50.3.0 | Persistência local |
| ZXing | 3.5.3 | Decodificação backend |
| jsQR | 1.4.0 via CDN | Decodificação no navegador |
| Spring Mail | fornecido pelo Boot | Confirmação por e-mail |
| Maven | wrapper externo ou instalação local | Build e testes |

## 3. Fluxo funcional principal

O fluxo principal da captura possui seis etapas explícitas.

1. O usuário seleciona um arquivo de imagem no controle de upload.
2. A imagem é carregada no Canvas 1. A região de seleção não é preenchida automaticamente.
3. O usuário clica em **Selecionar região** e arrasta manualmente um retângulo sobre o QR Code.
4. O usuário clica em **Cortar**. O Canvas 2 recebe o recorte da imagem original.
5. O usuário verifica o recorte e clica em **Ler QR Code**. O texto reconhecido aparece em uma `textarea` somente leitura.
6. O usuário confere o texto e clica em **Salvar no banco**. O histórico é recarregado com a nova captura.

O Canvas 1 pode apresentar uma marcação azul translúcida para orientar a seleção. Essa marcação nunca é utilizada como fonte do recorte. Antes da extração, o sistema usa a imagem original carregada pelo navegador e converte as coordenadas da seleção para `naturalWidth` e `naturalHeight`.

O Canvas 2 é dimensionado para `sw` por `sh`, calculados na resolução original. O contexto usa `imageSmoothingEnabled=false` durante o corte. Não são aplicados brilho, contraste, correção de cor, desentortamento, nitidez, blur, inversão ou qualquer ajuste de imagem. A leitura usa `inversionAttempts: 'dontInvert'`.

O zoom usado pelo usuário deve ser o zoom nativo do navegador ou do sistema operacional do celular. A aplicação não possui botão, slider ou lógica própria de zoom.

## 4. Autenticação e contas

A autenticação é baseada na tabela `users` e usa BCrypt para armazenar senhas. O login exige uma conta habilitada e confirmada. A autoinscrição cria uma conta desabilitada até que o usuário confirme o endereço de e-mail.

O fluxo de cadastro é:

1. O usuário informa nome, e-mail e senha.
2. O sistema grava o hash BCrypt e um token de confirmação.
3. O sistema tenta enviar um e-mail usando Spring Mail.
4. Em ambiente local sem SMTP funcional, o link de confirmação é registrado no log da aplicação.
5. O usuário acessa o link `/confirmar?token=...`.
6. O token é consumido e a conta passa a ficar habilitada.

Existe um inicializador opcional para desenvolvimento. Quando `APP_TEST_USER_ENABLED=true`, o sistema cria ou sincroniza uma conta confirmada com os valores configurados. Os valores padrão são `teste@localhost` e `Teste123!`. Esse recurso deve permanecer desativado em ambientes reais.

A autorização administrativa utiliza o campo `role`. O papel `ADMIN` acessa o CRUD de usuários em `/usuarios`. Usuários comuns acessam suas próprias capturas em `/capturas`.

## 5. CSRF e exclusão

Os formulários Thymeleaf incluem o token CSRF. A exclusão de uma captura ocorre por `POST` em `/capturas/{id}/excluir`. O backend sempre restringe a exclusão ao par `id da captura` e `id do usuário autenticado`.

Para evitar falhas quando a página contém um token antigo após a renovação da sessão, o formulário de exclusão é interceptado pelo JavaScript. Antes do envio, o navegador chama `GET /csrf-token`, atualiza o campo oculto com o token corrente e então envia o formulário. A proteção CSRF permanece habilitada.

## 6. Modelo de dados

A inicialização SQL cria duas tabelas.

### 6.1 Tabela `users`

| Coluna | Tipo | Regra |
|---|---|---|
| `id` | INTEGER | Chave primária autoincremental |
| `name` | TEXT | Obrigatório |
| `email` | TEXT | Obrigatório e único |
| `password_hash` | TEXT | Hash BCrypt |
| `role` | TEXT | `USER` por padrão; pode ser `ADMIN` |
| `enabled` | INTEGER | `0` até confirmação; `1` quando habilitado |
| `confirmation_token` | TEXT | Token temporário de confirmação |
| `created_at` | TEXT | Data de criação |

### 6.2 Tabela `qr_captures`

| Coluna | Tipo | Regra |
|---|---|---|
| `id` | INTEGER | Chave primária autoincremental |
| `user_id` | INTEGER | Referência para `users.id` |
| `content` | TEXT | Texto reconhecido ou informado |
| `source_name` | TEXT | Nome do arquivo ou origem informada |
| `created_at` | TEXT | Data de criação |

A chave estrangeira usa `ON DELETE CASCADE`. Os repositórios executam consultas parametrizadas para evitar concatenação de valores recebidos pelo usuário.

## 7. Rotas principais

| Método | Rota | Proteção | Função |
|---|---|---|---|
| `GET` | `/` | Pública | Página inicial |
| `GET` | `/login` | Pública | Formulário de login |
| `POST` | `/login` | Pública | Autenticação Spring Security |
| `GET` | `/cadastrar` | Pública | Formulário de autoinscrição |
| `POST` | `/cadastrar` | Pública | Criação de conta |
| `GET` | `/confirmar?token=...` | Pública | Confirmação de e-mail |
| `GET` | `/capturas` | Autenticada | Leitor e histórico |
| `POST` | `/capturas/ler` | Autenticada | Decodificação backend por upload |
| `POST` | `/capturas/salvar-texto` | Autenticada | Salvar texto reconhecido no histórico |
| `POST` | `/capturas/{id}/excluir` | Autenticada | Excluir captura do próprio usuário |
| `GET` | `/csrf-token` | Autenticada | Obter token CSRF corrente |
| `GET` | `/usuarios` | ADMIN | CRUD de usuários |
| `POST` | `/usuarios/{id}/editar` | ADMIN | Editar usuário |
| `POST` | `/usuarios/{id}/excluir` | ADMIN | Excluir usuário |

## 8. Estrutura do código

```text
src/main/java/dev/qrtext/
├── QrTextCaptureApplication.java
├── config/
│   ├── SecurityConfig.java
│   └── TestUserInitializer.java
├── model/
│   └── Models.java
├── repository/
│   ├── CaptureRepository.java
│   └── UserRepository.java
├── service/
│   ├── AuthService.java
│   └── QrService.java
└── web/
    └── WebControllers.java

src/main/resources/
├── application.yml
├── schema.sql
├── static/
│   ├── css/app.css
│   └── js/qr-reader.js
└── templates/
    ├── fragments.html
    ├── home.html
    ├── login.html
    ├── register.html
    ├── confirm.html
    ├── captures.html
    └── users.html
```

Os fragmentos Thymeleaf concentram componentes reutilizáveis, como cabeçalho e mensagens. Os repositórios contêm SQL explícito. Os serviços concentram autenticação, confirmação e decodificação. Os controllers conectam as telas aos serviços.

## 9. Configuração

As propriedades mais importantes são:

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_FILE` | `./data/qr-text-capture.db` | Caminho do banco SQLite |
| `APP_BASE_URL` | `http://localhost:8080` | URL usada nos links de confirmação |
| `MAIL_HOST` | `localhost` | Host SMTP |
| `MAIL_PORT` | `25` | Porta SMTP |
| `MAIL_USERNAME` | vazio | Usuário SMTP |
| `MAIL_PASSWORD` | vazio | Senha SMTP |
| `MAIL_FROM` | `no-reply@localhost` | Remetente |
| `APP_TEST_USER_ENABLED` | `false` | Ativa usuário confirmado de teste |
| `APP_TEST_USER_NAME` | `Usuário de Testes` | Nome da conta de teste |
| `APP_TEST_USER_EMAIL` | `teste@localhost` | E-mail da conta de teste |
| `APP_TEST_USER_PASSWORD` | `Teste123!` | Senha da conta de teste |
| `SERVER_PORT` | `8080` | Porta HTTP da aplicação |

Quando a aplicação é executada atrás de um proxy HTTPS, `server.forward-headers-strategy=framework` permite respeitar os cabeçalhos encaminhados pelo proxy.

## 10. Testes e critérios de aceite

O teste automatizado de contexto deve iniciar o Spring Boot com sucesso. O build mínimo de validação é:

```bash
mvn clean test package
```

Os critérios funcionais de aceite são:

- A imagem selecionada aparece no Canvas 1.
- Nenhuma área é selecionada automaticamente.
- O botão **Selecionar região** habilita a seleção manual.
- O retângulo azul é apenas uma marcação visual.
- O botão **Cortar** gera o Canvas 2 sem overlay.
- O Canvas 2 conserva a resolução e os pixels originais do recorte.
- O botão **Ler QR Code** mostra o texto em uma área somente leitura.
- O botão **Salvar no banco** cria o histórico e o atualiza.
- O botão **Excluir** remove apenas uma captura pertencente ao usuário autenticado.
- A exclusão não retorna 403 por token CSRF antigo.
- A aplicação funciona com layout responsivo sem botão de zoom próprio.

## 11. Limitações atuais

A aplicação trabalha com imagens selecionadas pelo usuário. A captura direta pela câmera do celular não foi implementada. O reconhecimento depende de uma seleção que contenha o QR Code inteiro e, preferencialmente, uma margem branca ao redor. Imagens inclinadas, com baixa resolução, reflexos ou o QR Code parcialmente cortado podem não ser reconhecidas sem que isso represente um erro de HTTPS ou de autenticação.

O SMTP é opcional para desenvolvimento. Em produção, deve ser configurado um servidor SMTP real e devem ser alteradas as credenciais padrão do usuário de teste.

## 12. Evoluções recomendadas

Como próximos passos, recomenda-se adicionar testes unitários dos repositórios e serviços, testes de integração dos fluxos CSRF, uma política de tamanho e tipo de imagem mais estrita, logs estruturados, migrações versionadas do SQLite e uma estratégia de backup do arquivo de banco.

## Referências

[1]: https://spring.io/projects/spring-boot "Spring Boot"
[2]: https://spring.io/projects/spring-security "Spring Security"
[3]: https://www.thymeleaf.org/ "Thymeleaf"
[4]: https://github.com/zxing/zxing "ZXing"
[5]: https://github.com/cozmo/jsQR "jsQR"
[6]: https://www.sqlite.org/docs.html "SQLite Documentation"
