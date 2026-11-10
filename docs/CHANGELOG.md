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

## 2026-11-10 (3)

- **multiusuário (ADR-006)**: tela de login/cadastro; cada usuário tem seu `dados-<login>.json`
  e não vê os dados do outro. Senhas com PBKDF2 + sal. O primeiro usuário adota o
  `dados.json` existente. "Sair" no Dashboard. Mudar o local dos dados leva todas as contas.
- **notificações (ADR-006)**: banner de prazos no Dashboard e balão na bandeja do sistema
  (ao entrar e a cada 30 min; cada item uma vez por sessão). Opção "Avisar prazos" por usuário.
- **build**: `jpackage` agora com `--add-modules java.desktop,java.prefs`.
- **testes**: 38 no total.

## 2026-11-10 (2)

- **UI, service**: confirmação antes de excluir projeto, etapa ou tarefa (mostra quantos
  itens filhos serão apagados). Antes o Dashboard nem tinha "excluir projeto" (RF01).
- **UI, service**: seletor de status (Não iniciada / Em andamento / Concluída) em cada
  etapa e tarefa; antes só havia o checkbox de concluída.
- **UI, service**: projetos podem ser arquivados/restaurados (`StatusProjeto`). Arquivados
  saem do Dashboard, dos contadores e dos prazos; "Mostrar arquivados" os exibe de volta.
- **UI**: filtro por status (Todas/Atrasadas/Não iniciadas/Em andamento/Concluídas) e
  ordenação (manual ou por prazo) nas telas de Etapas e Tarefas; filtro nos Próximos prazos.
- **UI, service**: reordenar etapas e tarefas manualmente (▲/▼), disponível na ordem manual
  e sem filtro ativo.
- **UI, repository**: exportar/importar todos os projetos para um arquivo JSON. Importar
  atualiza projetos de mesmo id e adiciona os demais; arquivo inválido é recusado sem
  alterar nada.
- **testes**: 24 no total.

## 2026-11-10

- **repository**: gravação segura do `dados.json` (arquivo temporário + troca atômica) com
  backup `.bak` da versão anterior. Arquivo corrompido não derruba mais o app: é preservado
  como `dados.json.corrompido-<timestamp>` e os dados são restaurados do backup, se houver.
  Campos desconhecidos no JSON são ignorados (compatível com versões futuras).
- **service**: nomes/títulos em branco são rejeitados e espaços nas pontas removidos.
- **ui**: erros inesperados agora aparecem num alerta em vez de o app travar em silêncio.
- **testes**: de 4 para 19 testes (persistência, recuperação de arquivo corrompido, cascata,
  validação, ordenação de prazos). **docs**: `docs/exemplo-dados.json` para demonstração.
- Executável em `dist/` regerado.
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
