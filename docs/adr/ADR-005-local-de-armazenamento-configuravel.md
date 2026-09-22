# ADR-005 — Local de armazenamento configurável pelo usuário

## Status
Aceita — 2026-09-21

## Contexto
ADR-002 fixou o arquivo de dados em `~/.gerenciador-projetos/dados.json`, sem
opção de troca. O usuário pediu um botão para escolher onde os projetos são
salvos (RF10, `docs/PRD.md`) — por exemplo, para salvar numa pasta sincronizada
(OneDrive/Drive) ou num pendrive, sem precisar mexer em código.

Isso não muda a decisão de ADR-002 (continua sendo um único arquivo JSON local,
sem servidor); só torna a localização desse arquivo configurável.

## Alternativas consideradas
- **Arquivo de configuração próprio** (ex.: `config.properties` num local fixo
  apontando para a pasta escolhida): funciona, mas exige código de leitura/
  escrita de mais um formato de arquivo só para guardar um caminho.
- **Variável de ambiente / argumento de linha de comando**: não atende ao
  pedido de um *botão* na interface; exigiria reiniciar o app manualmente com
  outro parâmetro.
- **`java.util.prefs.Preferences`**: API padrão do JDK (nenhuma dependência
  nova) para guardar pequenas preferências do usuário por aplicação, já
  persistida pelo próprio SO (registro do Windows / arquivo em
  `~/.java` no Linux/Mac). Ideal para guardar só o caminho da pasta escolhida.

## Decisão
Guardar o caminho da pasta escolhida via `Preferences`
(`ArmazenamentoConfig`, pacote `repository` — só essa camada conhece onde e
como o dado é persistido, ADR-003). Fluxo:

1. Dashboard tem um botão "📁 Local dos dados" que abre um `DirectoryChooser`
   nativo do JavaFX.
2. Ao escolher uma pasta, `ArmazenamentoConfig.escolherNovaPasta(...)`:
   - se a pasta escolhida já tiver um `dados.json` (ex.: pasta usada antes),
     mantém esse arquivo como está;
   - senão, move o `dados.json` da pasta atual para a nova, para não perder
     projetos já cadastrados;
   - grava o novo caminho na `Preferences`.
3. O Dashboard cria um novo `ProjetoService`/`JsonProjetoRepository` apontando
   para o novo arquivo e recarrega a tela — não precisa reiniciar o app.
4. Em execuções futuras, `Main` lê `ArmazenamentoConfig.arquivoAtual()` (que
   consulta a `Preferences`) em vez do caminho fixo.

Sem a pasta escolhida, o comportamento é o mesmo de antes (ADR-002): usa
`~/.gerenciador-projetos/dados.json`.

## Consequências
- Usuários que já tinham dados no caminho padrão continuam funcionando sem
  qualquer migração manual — a troca só acontece se o usuário clicar no botão.
- Se duas pastas diferentes acabarem com um `dados.json` cada (ex.: o usuário
  trocou de pasta e depois copiou o arquivo antigo manualmente para lá), o
  sistema não tenta fundir os dois — usa o que já existir na pasta escolhida.
  Aceitável para o escopo do trabalho (uso individual, sem colaboração).
- `ArmazenamentoConfig` fica isolado dentro de `repository`, então `service` e
  `ui` continuam sem saber que existe um arquivo JSON ou uma `Preferences` —
  só o Dashboard aciona a troca e recebe de volta o novo `ProjetoService`.
