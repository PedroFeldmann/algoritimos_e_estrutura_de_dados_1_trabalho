# Regras deste projeto

## Documentar sempre

Este é um trabalho acadêmico avaliado também pela documentação. **Toda mudança de
código que altere escopo, domínio ou comportamento visível precisa deixar rastro
escrito antes de ser considerada concluída.** Não confiar em memória de conversa —
o contexto pode ser resumido/perdido, os arquivos em `docs/` são a fonte da verdade.

Ao implementar algo, sempre atualizar o que for aplicável:

- **`docs/PRD.md`** — se mudou escopo, requisito funcional/não funcional, ou o
  modelo de domínio (novo campo em Projeto/Etapa/Tarefa, nova regra de negócio).
- **`docs/adr/ADR-00N-*.md`** — se foi uma decisão de arquitetura (nova tecnologia,
  novo padrão estrutural, troca de abordagem com trade-offs). Usar o mesmo formato
  dos ADRs existentes (Status / Contexto / Alternativas consideradas / Decisão /
  Consequências). Numerar sequencialmente.
- **`docs/CHANGELOG.md`** — sempre, um item curto por mudança entregue (o quê, e
  por quê em uma linha), com data. Serve de índice rápido do histórico do projeto.
- **`README.md`** — se mudou como buildar/rodar o projeto.

Se uma mudança pedida pelo usuário expande o domínio além do que a PRD descreve
(ex: um novo campo numa entidade), atualizar a PRD como parte da mesma entrega,
não depois. Citar explicitamente para o usuário, ao final da resposta, quais
arquivos de `docs/` foram criados ou atualizados.

## Referências

- Domínio e escopo: [`docs/PRD.md`](docs/PRD.md)
- Decisões de arquitetura: [`docs/adr/`](docs/adr)
- Histórico de mudanças: [`docs/CHANGELOG.md`](docs/CHANGELOG.md)
