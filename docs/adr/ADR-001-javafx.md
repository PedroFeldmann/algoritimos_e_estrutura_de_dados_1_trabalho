# ADR-001 — Interface gráfica em JavaFX

## Status
Aceita — 2026-09-15

## Contexto
O projeto precisa de uma interface desktop para o usuário navegar entre Dashboard,
Projeto, Etapa e formulários de criação (ver `docs/PRD.md`, seção 4.1). É um trabalho
acadêmico de Algoritmos e Estrutura de Dados: a interface não é o foco de avaliação,
mas precisa existir e ser utilizável.

## Alternativas consideradas
- **CLI (linha de comando)**: mais simples de implementar, mas não atende ao pedido de
  telas/wireframes e é pior experiência para navegar entre projeto → etapa → tarefa.
- **Swing**: nativo do JDK, sem dependências externas, porém API mais verbosa e visual
  datado; exigiria mais código boilerplate para o mesmo resultado.
- **Web (Spring Boot + HTML)**: mais robusto para múltiplos usuários, mas adiciona
  complexidade (servidor HTTP, camada REST) desnecessária para um app local
  single-user.
- **JavaFX**: API declarativa (FXML) ou programática, CSS para estilo, curva de
  aprendizado razoável, e é a tecnologia GUI moderna recomendada para novos projetos
  Java desktop.

## Decisão
Usar **JavaFX** para a interface gráfica, sem FXML no início (telas construídas via
código Java) para reduzir a quantidade de arquivos/configuração no MVP. FXML pode ser
adotado depois se as telas crescerem em complexidade.

Para evitar a complexidade do Java Platform Module System (JPMS) com JavaFX — fonte
comum de erros como `module not found` em projetos didáticos — o projeto **não** usa
`module-info.java`. O build roda em classpath simples via `javafx-maven-plugin`.

## Consequências
- Dependência externa `org.openjfx` precisa estar no `pom.xml` (JavaFX não é mais
  parte do JDK desde o Java 11).
- Rodar o projeto requer `mvn javafx:run` (ou configurar a IDE com os módulos
  JavaFX no VM options) em vez de um simples `java -jar`.
- Sem JPMS, perde-se isolamento forte de módulos, mas ganha-se simplicidade de setup
  — aceitável para o escopo do trabalho.
