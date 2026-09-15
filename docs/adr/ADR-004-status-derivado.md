# ADR-004 — Status "Atrasada" calculado, não armazenado

## Status
Aceita — 2026-09-15

## Contexto
`docs/PRD.md` (seção 5.2) define que Etapas e Tarefas são consideradas atrasadas
quando `status != Concluida` e `dataLimite < hoje`. É preciso decidir se esse valor
é gravado no JSON como um status normal ou calculado em tempo de execução.

## Alternativas consideradas
- **Gravar "Atrasada" como valor de status no arquivo**: exigiria um job/checagem
  para atualizar todos os itens toda vez que o app abre (ou um agendador rodando em
  background), e o dado salvo poderia ficar desatualizado entre uma abertura e
  outra do app.
- **Calcular em tempo real a partir de `dataLimite` e `status`**: sempre correto no
  momento da leitura, sem necessidade de sincronização.

## Decisão
O enum `Status` persistido só tem três valores: `NAO_INICIADA`, `EM_ANDAMENTO`,
`CONCLUIDA`. "Atrasada" **não** é um valor do enum — é derivado por um método
(`Etapa#estaAtrasada()` / `Tarefa#estaAtrasada()`) que compara `dataLimite` com
`LocalDate.now()` sempre que a UI precisa exibir o badge de status.

A UI (camada `ui`, ver ADR-003) decide qual badge mostrar combinando `status` +
`estaAtrasada()` — o domínio nunca grava "atrasado" como estado.

## Consequências
- Não existe risco de dado desatualizado: o status visual está sempre correto no
  instante em que a tela é desenhada.
- O formulário de criação/edição (tela 04 dos wireframes) não oferece "Atrasada"
  como opção de status — reforça no código a regra já definida na PRD.
- Testes de domínio podem fixar uma `dataLimite` no passado e verificar
  `estaAtrasada() == true` sem precisar mockar tempo/scheduler.
