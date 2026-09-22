# Changelog

Histórico de mudanças de escopo/domínio/comportamento. Ver `CLAUDE.md` na raiz
para a regra de quando registrar uma entrada aqui.

## 2026-09-22

- **Build, UI**: adicionada `ui.Launcher` (classe `main` sem estender
  `Application`) e documentado no README como gerar um executável standalone
  do Windows via `jpackage` (`mvn dependency:copy-dependencies` + `jpackage
  --type app-image`). Sem `Launcher`, o `jpackage`/`java -jar` recusam iniciar
  com "JavaFX runtime components are missing" porque o projeto não usa
  module-info.java (ADR-001) e a classe `main` original (`ui.Main`) é ela
  mesma uma `Application`.

## 2026-09-21

- **PRD, ADR, repository, UI**: novo botão "📁 Local dos dados" no Dashboard
  para o usuário escolher em qual pasta o `dados.json` é salvo (RF10). Nova
  classe `ArmazenamentoConfig` (pacote `repository`) guarda a pasta escolhida
  via `java.util.prefs.Preferences` e migra o arquivo existente para lá. Ver
  `docs/adr/ADR-005-local-de-armazenamento-configuravel.md`. Sem a escolha,
  comportamento continua igual ao de ADR-002 (`~/.gerenciador-projetos`).

## 2026-09-16

- **UI**: redesenho visual das 3 telas de navegação (Dashboard, Etapas, Tarefas),
  aprovado antes via wireframe (artifact). Sem mudança de domínio/regras de
  negócio — apenas apresentação:
  - Novo stylesheet `src/main/resources/.../ui/app.css` (cores, cards, pills de
    status, botões) carregado pelo `Navigator`, substituindo os `-fx-style`
    inline que existiam em cada tela.
  - Dashboard ganhou tiles de resumo (projetos ativos, itens atrasados,
    próximos 7 dias — todos calculados a partir de dados já existentes, nenhum
    campo novo), busca por nome de projeto (filtro em memória, só na UI) e
    barra de progresso por projeto (fração de etapas concluídas).
  - Etapas/Tarefas ganharam breadcrumb com botão "← Dashboard" e lista em
    cartão único no lugar de linhas soltas.
  - `StatusBadge` passou a gerar pills via style class (CSS) em vez de
    `-fx-style` montado em Java.

## 2026-09-15 (2)

- **UI, service**: Etapa e Tarefa agora têm edição (nome/título, descrição, data
  limite) via link "editar" nas telas 02 e 03, reaproveitando `ItemFormDialog`
  (mesmo padrão já usado para editar Projeto). Ver `docs/PRD.md` RF02/RF03.
- **UI, service**: adicionado checkbox de conclusão na linha de cada Etapa (tela
  02) — antes só era possível concluir Tarefas, não havia forma de marcar uma
  Etapa como concluída. `ProjetoService.alterarStatusEtapa(...)`.

## 2026-09-15

- **PRD, domínio, UI**: Projeto ganhou campo `dataLimite` (opcional) e passou a
  ser editável (nome, descrição, data limite) a partir do Dashboard. Antes só
  existia criação; a PRD original (seção 5.1) não previa deadline em Projeto,
  só em Etapa/Tarefa — ampliado a pedido do usuário. Ver `docs/PRD.md` seção 5.1
  e RF01.
- Scaffold inicial do projeto: PRD, ADR-001 a ADR-004, estrutura em camadas
  (domain/repository/service/ui), telas 01–04 dos wireframes implementadas com
  dados reais, persistência em JSON, teste de domínio para o status "Atrasada".
