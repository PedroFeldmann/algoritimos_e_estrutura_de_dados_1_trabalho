# ADR-002 — Persistência em arquivo JSON local

## Status
Aceita — 2026-09-15

## Contexto
O sistema precisa manter Projetos, Etapas e Tarefas entre execuções (RF08, RNF02/RNF03
em `docs/PRD.md`). É um app local, single-user, sem servidor.

## Alternativas consideradas
- **Banco relacional embarcado (H2/SQLite)**: mais robusto para consultas e
  concorrência, mas exige driver JDBC, schema/migrations e SQL — complexidade extra
  não justificada para um app local de uso individual.
- **Serialização binária Java (`Serializable`)**: simples de implementar, porém gera
  arquivos ilegíveis/não portáveis entre versões da aplicação, dificultando debug e
  inspeção manual durante o desenvolvimento/avaliação do trabalho.
- **Arquivo JSON local**: legível, fácil de inspecionar e versionar em capturas de
  tela do trabalho, e mapeia diretamente para as classes de domínio via uma
  biblioteca de serialização.

## Decisão
Persistir os dados em um único arquivo `dados.json` no diretório de configuração do
usuário (`~/.gerenciador-projetos/dados.json`), usando **Jackson**
(`jackson-databind` + `jackson-datatype-jsr310` para tipos `LocalDate`) para
serializar/desserializar a árvore de objetos `Projeto → Etapa → Tarefa`.

A leitura/escrita é feita de forma simples e síncrona (carrega tudo em memória ao
abrir o app, regrava o arquivo inteiro a cada alteração relevante). Não há
otimização de escrita incremental — aceitável dado o volume de dados esperado
(uso individual, poucas dezenas de projetos).

## Consequências
- Não há suporte a múltiplos processos escrevendo no mesmo arquivo simultaneamente
  (fora de escopo — uso é local e single-user).
- Se o arquivo for corrompido manualmente, a aplicação pode falhar ao carregar;
  não há migração de schema entre versões (aceitável para o MVP acadêmico).
- A camada de persistência fica isolada atrás de uma interface de repositório
  (ver `ADR-003-arquitetura-em-camadas.md`), então trocar JSON por outro mecanismo
  no futuro não afeta domínio nem UI.
