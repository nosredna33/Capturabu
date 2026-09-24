# QR Text Capture

O **QR Text Capture** é uma aplicação web genérica para ler o texto de QR Codes presentes nas portas das seções eleitorais a partir de imagens de fotos dos QR-Codes e manter um histórico por usuário. O projeto não possui regras de negócio específicas de eleições ou de qualquer outro domínio.

O fluxo de leitura é manual e controlado pelo usuário: selecionar imagem, selecionar região no Canvas 1, cortar, conferir o Canvas 2, ler o QR Code, conferir o texto e salvar no banco.

## Características principais

- Java 21 e Spring Boot 3.5.6.
- Thymeleaf com fragmentos reutilizáveis.
- Spring Security com login, logout, BCrypt e CSRF.
- Autoinscrição com confirmação por e-mail.
- SQLite 3 acessado exclusivamente por JDBC e SQL explícito.
- Nenhum JPA, Hibernate ou ORM.
- CRUD administrativo de usuários.
- Histórico individual de capturas.
- Leitura no navegador com `jsQR`.
- Decodificação backend alternativa com ZXing.
- Layout responsivo para desktop e celular.
- Compatibilidade com zoom nativo do navegador e do sistema do celular.
- Nenhum botão ou slider de zoom próprio na aplicação.

## Fluxo do leitor

1. Clique em **Selecionar imagem**.
2. A imagem completa aparecerá no Canvas 1.
3. Clique em **Selecionar região**.
4. Arraste manualmente sobre o QR Code.
5. Clique em **Cortar**.
6. Confira o recorte no Canvas 2.
7. Clique em **Ler QR Code**.
8. Confira o texto na área somente leitura.
9. Clique em **Salvar no banco**.
10. Consulte a captura no histórico.

A marcação azul do Canvas 1 é apenas visual. O recorte do Canvas 2 é obtido a partir da imagem original e das dimensões naturais do arquivo. Não são aplicados ajustes de brilho, contraste, cor, nitidez, blur, desentortamento ou suavização. O zoom deve ser feito pelo próprio navegador ou pelo celular; ele não participa do processamento do recorte.

Para aumentar a chance de reconhecimento, selecione o QR Code inteiro e inclua uma margem branca ao redor. Um QR Code parcialmente cortado, desfocado ou com baixa resolução pode não ser reconhecido.

## Requisitos

### Linux

Instale Java 21, Maven, Git e, opcionalmente, SQLite CLI:

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk maven git sqlite3
java -version
mvn -version
```

### Windows

Instale:

1. JDK 21, por exemplo, Eclipse Temurin 21.
2. Maven 3.9 ou superior.
3. Git para Windows, se o projeto for obtido por Git.
4. SQLite CLI opcional, caso queira consultar o banco pelo terminal.

Abra o PowerShell e confirme:

```powershell
java -version
mvn -version
git --version
```

Se o Windows não localizar Java ou Maven, configure `JAVA_HOME` e inclua `%JAVA_HOME%\bin` e a pasta `bin` do Maven no `Path` do sistema.

## Obter o projeto

Se estiver usando um arquivo ZIP, extraia-o para um diretório de trabalho. Se estiver usando Git:

```bash
git clone <URL_DO_REPOSITORIO>
cd qr-text-capture
```

No PowerShell:

```powershell
git clone <URL_DO_REPOSITORIO>
Set-Location qr-text-capture
```

## Compilar e testar

Execute no Linux, macOS ou PowerShell:

```bash
mvn clean test package
```

O teste de contexto inicia a configuração Spring completa. O comando também gera o JAR em `target/qr-text-capture-0.0.1-SNAPSHOT.jar`.

## Executar no Linux

### Modo Maven

```bash
export APP_TEST_USER_ENABLED=true
export SERVER_PORT=8080
mvn spring-boot:run
```

### Modo JAR

```bash
export APP_TEST_USER_ENABLED=true
export SERVER_PORT=8080
java -jar target/qr-text-capture-0.0.1-SNAPSHOT.jar
```

### Execução em uma linha

```bash
APP_TEST_USER_ENABLED=true SERVER_PORT=8080 mvn spring-boot:run
```

Acesse `http://localhost:8080`.

## Executar no Windows PowerShell

### Modo Maven

```powershell
$env:APP_TEST_USER_ENABLED = "true"
$env:SERVER_PORT = "8080"
mvn spring-boot:run
```

### Modo JAR

```powershell
$env:APP_TEST_USER_ENABLED = "true"
$env:SERVER_PORT = "8080"
java -jar target\qr-text-capture-0.0.1-SNAPSHOT.jar
```

Acesse `http://localhost:8080`.

As variáveis definidas com `$env:` valem para a sessão atual do PowerShell. Para removê-las:

```powershell
Remove-Item Env:APP_TEST_USER_ENABLED
Remove-Item Env:SERVER_PORT
```

## Usuário confirmado para testes

Com `APP_TEST_USER_ENABLED=true`, a aplicação cria ou sincroniza uma conta confirmada:

- E-mail: `teste@localhost`
- Senha: `Teste123!`
- Nome: `Usuário de Testes`

Esse usuário existe para desenvolvimento e testes locais. Mantenha a funcionalidade desativada em produção.

Para configurar outra conta no Linux:

```bash
export APP_TEST_USER_ENABLED=true
export APP_TEST_USER_NAME="Usuário Local"
export APP_TEST_USER_EMAIL=teste@example.local
export APP_TEST_USER_PASSWORD='SenhaLocal123!'
mvn spring-boot:run
```

No PowerShell:

```powershell
$env:APP_TEST_USER_ENABLED = "true"
$env:APP_TEST_USER_NAME = "Usuário Local"
$env:APP_TEST_USER_EMAIL = "teste@example.local"
$env:APP_TEST_USER_PASSWORD = "SenhaLocal123!"
mvn spring-boot:run
```

## Banco SQLite

Por padrão, o arquivo é criado em:

```text
./data/qr-text-capture.db
```

Para usar outro caminho no Linux:

```bash
export DB_FILE=/caminho/para/qr-text-capture.db
```

No PowerShell:

```powershell
$env:DB_FILE = "C:\dados\qr-text-capture.db"
```

Se o SQLite CLI estiver instalado, consulte os dados:

```bash
sqlite3 ./data/qr-text-capture.db "select id,name,email,role,enabled from users;"
sqlite3 ./data/qr-text-capture.db "select id,user_id,content,source_name,created_at from qr_captures order by id desc;"
```

No Windows, os mesmos comandos funcionam quando `sqlite3.exe` estiver no `Path`:

```powershell
sqlite3 .\data\qr-text-capture.db "select id,name,email,role,enabled from users;"
```

## Primeiro administrador

O primeiro cadastro recebe o papel `USER`. Depois de confirmar a conta, promova-a diretamente no SQLite.

Linux:

```bash
sqlite3 ./data/qr-text-capture.db "update users set role='ADMIN' where email='seu@email';"
```

Windows PowerShell:

```powershell
sqlite3 .\data\qr-text-capture.db "update users set role='ADMIN' where email='seu@email';"
```

Depois de promover o usuário, faça novo login. O CRUD administrativo estará disponível em `/usuarios`.

## Confirmação por e-mail

O cadastro exige confirmação. Em ambiente local sem SMTP configurado, o link de confirmação é registrado no log da aplicação. Para usar SMTP, configure:

| Variável | Exemplo | Função |
|---|---|---|
| `MAIL_HOST` | `smtp.example.com` | Host SMTP |
| `MAIL_PORT` | `587` | Porta SMTP |
| `MAIL_USERNAME` | `usuario` | Usuário SMTP |
| `MAIL_PASSWORD` | `senha` | Senha SMTP |
| `MAIL_FROM` | `no-reply@example.com` | Remetente |
| `APP_BASE_URL` | `https://app.example.com` | URL dos links |

Linux:

```bash
export MAIL_HOST=smtp.example.com
export MAIL_PORT=587
export MAIL_USERNAME=usuario
export MAIL_PASSWORD='senha'
export MAIL_FROM=no-reply@example.com
export APP_BASE_URL=https://app.example.com
mvn spring-boot:run
```

PowerShell:

```powershell
$env:MAIL_HOST = "smtp.example.com"
$env:MAIL_PORT = "587"
$env:MAIL_USERNAME = "usuario"
$env:MAIL_PASSWORD = "senha"
$env:MAIL_FROM = "no-reply@example.com"
$env:APP_BASE_URL = "https://app.example.com"
mvn spring-boot:run
```

O HTTPS deve ser fornecido por um proxy reverso ou configurado no ambiente de implantação. O código respeita cabeçalhos encaminhados por proxy com `server.forward-headers-strategy=framework`.

## Estrutura do projeto

```text
qr-text-capture/
├── pom.xml
├── README.md
├── ESPECIFICACAO.md
├── PROMPT-REPRODUCAO.md
└── src/
    ├── main/java/dev/qrtext/
    │   ├── QrTextCaptureApplication.java
    │   ├── config/
    │   ├── model/
    │   ├── repository/
    │   ├── service/
    │   └── web/
    ├── main/resources/
    │   ├── application.yml
    │   ├── schema.sql
    │   ├── static/css/app.css
    │   ├── static/js/qr-reader.js
    │   └── templates/
    └── test/java/dev/qrtext/
```

## Rotas principais

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/` | Página inicial |
| `GET` | `/login` | Login |
| `GET`/`POST` | `/cadastrar` | Autoinscrição |
| `GET` | `/confirmar?token=...` | Confirmação |
| `GET` | `/capturas` | Leitor e histórico |
| `POST` | `/capturas/salvar-texto` | Salvar texto reconhecido |
| `POST` | `/capturas/{id}/excluir` | Excluir captura própria |
| `GET` | `/usuarios` | Administração de usuários |
| `GET` | `/csrf-token` | Token CSRF atualizado |

## Solução de problemas

### A porta 8080 está ocupada

Linux:

```bash
ss -ltnp 'sport = :8080'
kill <PID>
```

Windows PowerShell:

```powershell
Get-NetTCPConnection -LocalPort 8080
Stop-Process -Id <PID> -Force
```

Como alternativa, use outra porta:

```bash
SERVER_PORT=8081 mvn spring-boot:run
```

PowerShell:

```powershell
$env:SERVER_PORT = "8081"
mvn spring-boot:run
```

### O QR Code não é reconhecido

Verifique se o recorte contém o QR Code inteiro, se existe margem branca e se a imagem possui resolução suficiente. O Canvas 2 não aplica correções automáticas. Isso é intencional para preservar os pixels originais.

### O login volta para a tela de login

Confirme se a conta está confirmada e habilitada. Em ambiente de teste, ative `APP_TEST_USER_ENABLED=true` e use `teste@localhost` com `Teste123!`. Se a aplicação estiver atrás de proxy, confira a configuração de cabeçalhos encaminhados e faça uma nova tentativa após limpar os cookies da sessão.

### O botão Excluir retorna 403

A versão atual busca um token CSRF atualizado antes de enviar a exclusão. Se estiver usando arquivos antigos do navegador, faça recarga completa da página. Confirme também que o JavaScript `/js/qr-reader.js` está sendo carregado.

## Limitações conhecidas

A captura direta pela câmera do celular não está implementada. A aplicação depende de um arquivo de imagem selecionado pelo usuário. O reconhecimento pode falhar quando o QR Code está incompleto, desfocado, muito pequeno, inclinado ou com reflexos.

## Licença e uso

Defina a licença antes de publicar o projeto em um repositório. O código deste pacote é um ponto de partida para desenvolvimento local e deve receber revisão de segurança, configuração de segredos e política de backup antes de uso em produção.

## Documentos complementares

- [Especificação técnica completa](ESPECIFICACAO.md)
- [Prompt para reprodução por outra IA](PROMPT-REPRODUCAO.md)

## Referências

[1]: https://spring.io/projects/spring-boot "Spring Boot"
[2]: https://spring.io/projects/spring-security "Spring Security"
[3]: https://www.thymeleaf.org/ "Thymeleaf"
[4]: https://github.com/zxing/zxing "ZXing"
[5]: https://github.com/cozmo/jsQR "jsQR"
[6]: https://www.sqlite.org/docs.html "SQLite Documentation"
