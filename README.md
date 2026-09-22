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

Pré-requisitos: JDK 17+ e Maven.

```bash
mvn javafx:run
```

## Como rodar os testes

```bash
mvn test
```

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
