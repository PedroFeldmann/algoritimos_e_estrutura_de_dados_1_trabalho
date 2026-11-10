# ADR-006 — Multiusuário local e notificações de prazo

- **Status:** aceito (2026-11-10)
- **Substitui parcialmente:** PRD 4.2 ("Fora do escopo": multiusuário/login e notificações)

## Contexto

O app era single-user e sem avisos. Pediu-se multiusuário e notificações. O app
continua local e sem servidor (RNF02/RNF03), então "multiusuário" significa
**várias contas no mesmo computador**, cada uma com seus próprios projetos — não
colaboração em tempo real nem dados compartilhados entre contas/máquinas.

## Decisão

**Contas locais**
- `usuarios.json` na pasta de dados (a mesma do RF10) guarda login, nome, sal e hash.
- Senha: PBKDF2-HMAC-SHA256, 120 000 iterações, sal aleatório de 16 bytes — tudo do
  próprio JDK, sem dependência nova. A senha nunca é gravada; a comparação é em
  tempo constante e login inexistente/senha errada dão a mesma mensagem.
- Cada usuário tem o seu `dados-<login>.json`. O login é restrito a `[a-z0-9._-]{3,30}`,
  o que também impede caminhos como `../`.
- O primeiro usuário criado adota o `dados.json` da versão single-user, para ninguém
  perder dados. Mudar a pasta de dados leva `usuarios.json` e todos os `dados-*.json`.
- A tela de login abre antes do Dashboard; "Sair" volta a ela. O usuário logado fica no `Navigator`.

**Notificações**
- `NotificadorPrazos` (service, sem UI) decide o que avisar: itens não concluídos,
  com data, atrasados ou vencendo em até 2 dias.
- `AgendadorNotificacoes` verifica ao entrar e a cada 30 min, e avisa cada item uma
  vez por sessão, via balão da bandeja do sistema (`java.awt.SystemTray`).
- O Dashboard mostra um banner com o mesmo resumo, independente da bandeja.
- A opção "Avisar prazos" é por usuário (`Preferences`), ligada por padrão.

## Consequências

- Não é um controle de acesso forte: quem tem acesso à pasta do usuário do SO lê os
  JSON. O objetivo é separar contas no uso diário, não proteger contra o dono da máquina.
- Não há recuperação de senha nem troca de senha (fora desta versão).
- Se o sistema não tiver bandeja, só o banner avisa. O executável precisa dos módulos
  `java.desktop` e `java.prefs` (ver README, comando do `jpackage`).
- Não há compartilhamento de projetos entre usuários.
