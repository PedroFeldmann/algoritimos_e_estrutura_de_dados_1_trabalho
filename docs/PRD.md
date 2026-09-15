# PRD — Gerenciador de Projetos

## 1. Visão geral

Aplicação desktop em Java (JavaFX) para gerenciar múltiplos projetos, cada um dividido em
etapas, e dentro de cada etapa, tarefas com data limite. O usuário deve conseguir navegar
para dentro de um projeto específico, ver suas etapas e tarefas, e acompanhar prazos.

## 2. Problema

Uma pessoa que conduz vários projetos ao mesmo tempo (ex: trabalhos de faculdade, projetos
pessoais) não tem uma visão organizada de quais etapas/tarefas pertencem a qual projeto,
nem dos prazos de cada uma. Planilhas ou anotações soltas não escalam bem com múltiplos
projetos simultâneos.

## 3. Objetivo

Fornecer uma aplicação simples, local, que permita:
- Cadastrar e listar múltiplos projetos.
- Entrar em um projeto específico e gerenciar suas etapas.
- Dentro de cada etapa, gerenciar tarefas.
- Definir e visualizar datas limite (deadlines) em etapas e tarefas.
- Ver rapidamente o que está atrasado, próximo do prazo ou concluído.

## 4. Escopo

### 4.1 Dentro do escopo (MVP)
- CRUD de Projetos (criar, listar, editar, arquivar/excluir).
- CRUD de Etapas dentro de um Projeto.
- CRUD de Tarefas dentro de uma Etapa.
- Data limite (deadline) em Etapas e em Tarefas.
- Status de cada item (ex: Não iniciado, Em andamento, Concluído, Atrasado).
- Cálculo automático de "atrasado" comparando deadline com a data atual.
- Navegação: Dashboard (todos os projetos) → Projeto (etapas) → Etapa (tarefas).
- Persistência local em arquivo JSON (sem servidor, sem login).
- Visão simples de prazos próximos/atrasados (lista ordenada por data).

### 4.2 Fora do escopo (não será feito no MVP)
- Multiusuário, login, permissões.
- Sincronização em nuvem / colaboração em tempo real.
- Notificações do sistema operacional.
- Anexos de arquivos às tarefas.
- Relatórios avançados, gráficos, dashboards analíticos.
- Aplicativo mobile ou versão web.
- Subtarefas (tarefas dentro de tarefas) — apenas 3 níveis: Projeto → Etapa → Tarefa.

## 5. Domínio (entidades e regras)

### 5.1 Entidades

**Projeto**
- id, nome, descrição, dataCriacao, dataLimite (opcional), status (Ativo/Arquivado)
- possui 0..N Etapas
- nome, descrição e dataLimite são editáveis pelo usuário a partir do Dashboard
  (dataCriacao e id não são editáveis). *Adicionado em 2026-09-15 — ver
  `docs/CHANGELOG.md`; o campo `dataLimite` não fazia parte do escopo original.*

**Etapa**
- id, nome, descrição, dataLimite, status (NaoIniciada/EmAndamento/Concluida/Atrasada)
- pertence a 1 Projeto
- possui 0..N Tarefas

**Tarefa**
- id, titulo, descrição, dataLimite, status (NaoIniciada/EmAndamento/Concluida/Atrasada)
- pertence a 1 Etapa

### 5.2 Regras de domínio
- Uma Tarefa/Etapa é considerada **Atrasada** se `status != Concluida` e `dataLimite < hoje`.
  Esse status é derivado, não editado manualmente pelo usuário.
- Excluir um Projeto exclui em cascata suas Etapas e Tarefas.
- Excluir uma Etapa exclui em cascata suas Tarefas.
- A dataLimite de uma Etapa é informativa; o sistema **não** força que as tarefas
  da etapa tenham deadline anterior ao da etapa (MVP simples, sem essa validação).
- Status "Concluída" de uma Etapa não é automático — o usuário marca manualmente
  (mesmo que existam tarefas em aberto), mantendo o modelo simples.

## 6. Requisitos funcionais

| ID | Requisito |
|----|-----------|
| RF01 | O sistema deve permitir criar, editar, listar e excluir Projetos. |
| RF02 | O sistema deve permitir criar, editar (nome, descrição, data limite e status), listar e excluir Etapas dentro de um Projeto. |
| RF03 | O sistema deve permitir criar, editar (título, descrição, data limite e status), listar e excluir Tarefas dentro de uma Etapa. |
| RF04 | O sistema deve permitir definir data limite em Etapas e Tarefas. |
| RF05 | O sistema deve calcular automaticamente o status "Atrasado" com base na data atual. |
| RF06 | O sistema deve exibir uma tela de Dashboard listando todos os projetos com indicador de pendências/atrasos. |
| RF07 | O sistema deve permitir navegar do Dashboard para um Projeto específico, e deste para suas Etapas e Tarefas. |
| RF08 | O sistema deve persistir os dados localmente em arquivo, mantendo-os entre execuções. |
| RF09 | O sistema deve exibir uma lista de "Próximos prazos" agregando etapas/tarefas ordenadas por data limite. |

## 7. Requisitos não funcionais

| ID | Requisito |
|----|-----------|
| RNF01 | Aplicação desktop Java, interface gráfica em JavaFX. |
| RNF02 | Persistência em arquivo(s) JSON local, sem dependência de banco de dados externo. |
| RNF03 | Deve rodar localmente sem necessidade de internet. |
| RNF04 | Código organizado em camadas (domínio, persistência, UI) para fins didáticos. |
| RNF05 | Projeto documentado (PRD, ADRs, README) por ser trabalho acadêmico. |

## 8. Personas

**Usuário único (self-service)**: estudante ou profissional que conduz múltiplos projetos
e quer visualizar prazos e progresso sem depender de planilhas. Não há distinção de perfis
de acesso — é uso individual e local.

## 9. Fluxo principal (happy path)

1. Usuário abre o app → vê Dashboard com lista de projetos existentes.
2. Usuário cria um novo Projeto.
3. Usuário entra no Projeto → tela de Etapas (vazia inicialmente).
4. Usuário cria uma Etapa com data limite.
5. Usuário entra na Etapa → tela de Tarefas.
6. Usuário cria Tarefas com data limite dentro da Etapa.
7. Usuário volta ao Dashboard e vê indicadores de prazos e atrasos atualizados.

## 10. Critérios de aceite do MVP

- É possível criar um Projeto, uma Etapa dentro dele e uma Tarefa dentro da Etapa,
  fechar o aplicativo e reabrir, e todos os dados continuam lá.
- Uma Tarefa com data limite no passado e status diferente de "Concluída" aparece
  visualmente marcada como Atrasada.
- O Dashboard mostra corretamente a contagem de itens atrasados por projeto.
