# Changelog

Histórico de mudanças de escopo/domínio/comportamento. Ver `CLAUDE.md` na raiz
para a regra de quando registrar uma entrada aqui.

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
