# Gerenciador de Projetos

Aplicação desktop em Java/JavaFX para gerenciar múltiplos projetos, suas etapas
e as tarefas de cada etapa, com datas limite e cálculo automático de atraso.

Trabalho de Algoritmos e Estrutura de Dados 1.

## Documentação

- [`docs/PRD.md`](docs/PRD.md) — escopo, domínio, requisitos e critérios de aceite.
- [`docs/adr/`](docs/adr) — decisões de arquitetura (ADR-001 a ADR-005).
- Wireframes das telas: publicados como artifact durante o desenvolvimento
  (ver histórico da conversa/relatório do trabalho).

## Como rodar

**Sem instalar nada:** baixe [`dist/GerenciadorDeProjetos.zip`](dist/GerenciadorDeProjetos.zip),
extraia a pasta inteira e execute `GerenciadorDeProjetos.exe` (Windows). Já
vem com o runtime Java embutido — não precisa ter Java instalado.

**A partir do código-fonte:** pré-requisitos JDK 17+ e Maven.

```bash
mvn javafx:run
```

## Como rodar os testes

```bash
mvn test
```

## Como gerar um executável standalone (Windows)

O repositório já traz um executável pronto em `dist/GerenciadorDeProjetos.zip`
(ver seção "Como rodar" acima). Esta seção é só para regerá-lo depois de
alterar o código.

Gera uma pasta com `GerenciadorDeProjetos.exe` e um runtime Java embutido —
não precisa ter Java instalado na máquina que for rodar. Requer JDK 17+ com
`jpackage` (vem junto do JDK desde a versão 14).

```bash
mvn dependency:copy-dependencies -DoutputDirectory=target/libs -DincludeScope=runtime
mvn package -DskipTests
cp target/gerenciador-projetos-1.0-SNAPSHOT.jar target/libs/

jpackage --type app-image ^
  --input target/libs ^
  --dest target/dist ^
  --name GerenciadorDeProjetos ^
  --main-jar gerenciador-projetos-1.0-SNAPSHOT.jar ^
  --main-class br.edu.trabalho.gerenciadorprojetos.ui.Launcher ^
  --app-version 1.0
```

O executável fica em `target/dist/GerenciadorDeProjetos/GerenciadorDeProjetos.exe`
(pasta inteira, não só o `.exe`, precisa ser copiada/distribuída). O ponto de
entrada é `ui.Launcher`, não `ui.Main` — ver comentário na classe: o runtime do
JavaFX recusa iniciar quando a classe com `main` é a própria `Application`
rodando fora do module-path (este projeto não usa module-info.java, ADR-001).

## Estrutura do código

```
src/main/java/br/edu/trabalho/gerenciadorprojetos/
├── domain/       Entidades e regras de negócio (Projeto, Etapa, Tarefa, Status)
├── repository/   Persistência em JSON (ADR-002)
├── service/      Regras de aplicação (ProjetoService)
└── ui/           Telas JavaFX (ADR-001)
```

Ver `docs/adr/ADR-003-arquitetura-em-camadas.md` para a justificativa dessa divisão.

Por padrão os dados ficam salvos em `~/.gerenciador-projetos/dados.json`, mas
o botão "📁 Local dos dados" no Dashboard permite escolher outra pasta (a
escolha é lembrada entre execuções — ver
`docs/adr/ADR-005-local-de-armazenamento-configuravel.md`).
