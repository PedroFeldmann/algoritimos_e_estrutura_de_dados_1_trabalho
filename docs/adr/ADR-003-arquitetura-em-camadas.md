# ADR-003 — Arquitetura em camadas

## Status
Aceita — 2026-09-15

## Contexto
RNF04 (`docs/PRD.md`) pede código organizado em camadas por motivo didático. O
projeto é pequeno (3 entidades, 1 tela de fluxo principal repetida em 3 níveis),
então a arquitetura precisa ser simples de explicar sem virar excesso de abstração.

## Decisão
Quatro pacotes, com dependência em uma única direção (`ui → service → repository →
domain`; `domain` não depende de nada):

```
br.edu.trabalho.gerenciadorprojetos
├── domain/       Entidades e regras de negócio puras (Projeto, Etapa, Tarefa, Status).
│                 Sem dependência de JavaFX nem de Jackson.
├── repository/   Interface de acesso a dados (ProjetoRepository) + implementação
│                 concreta em JSON (JsonProjetoRepository). Só essa camada conhece
│                 o formato de armazenamento.
├── service/      Regras de aplicação que orquestram domínio + repositório:
│                 exclusão em cascata, cálculo de "próximos prazos", contagem de
│                 atrasados por projeto.
└── ui/           Telas JavaFX (Dashboard, Projeto, Etapa, modal de novo item).
                  Só conversa com `service`, nunca com `repository` diretamente.
```

## Alternativas consideradas
- **Tudo em um pacote só**: mais rápido de escrever, mas mistura lógica de UI com
  regra de negócio — dificulta testar domínio sem inicializar JavaFX, e não atende
  ao requisito didático de camadas.
- **Arquitetura hexagonal completa (ports & adapters) com DI framework**: excesso de
  cerimônia para 3 entidades; adicionaria uma curva de aprendizado (Spring/Guice)
  desnecessária para o escopo do trabalho.

## Consequências
- `domain` pode ser testado com JUnit puro, sem subir JavaFX nem tocar disco.
- Trocar a persistência (ADR-002) ou a UI (ADR-001) no futuro afeta apenas a
  camada correspondente.
- Repositório é injetado manualmente (sem framework de DI) — construído uma vez em
  `Main` e passado para o `service`, que é passado para os controllers de UI.
