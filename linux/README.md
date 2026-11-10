# Versão para Linux

Esta pasta tem tudo o que é específico do Linux. O código-fonte é o mesmo do restante do projeto.

## Opção 1 — Pacote portátil (precisa de Java 17+)

`GerenciadorDeProjetos-linux.tar.gz` já está pronto neste repositório. Ele traz os `.jar` do app e
do JavaFX para Linux (x64) e um script de execução.

```bash
sudo apt install openjdk-21-jre      # se ainda não tiver Java 17 ou superior
tar -xzf GerenciadorDeProjetos-linux.tar.gz
./GerenciadorDeProjetos/executar.sh
```

A pasta `GerenciadorDeProjetos/` deste diretório é o conteúdo desse pacote já descompactado.

- Os `.jar` do JavaFX incluídos são os de **Linux x64**. Em ARM (ex.: Raspberry Pi) use a opção 2,
  que baixa a versão certa da sua máquina.
- Precisa de ambiente gráfico (X11 ou Wayland).

## Opção 2 — Versão autossuficiente (Java embutido)

O `jpackage` não gera para outro sistema, então isto precisa ser feito num Linux com JDK 17+ e Maven:

```bash
sh linux/gerar-executavel.sh
```

Gera `linux/GerenciadorDeProjetos-linux-standalone.tar.gz`. Quem for usar não precisa ter Java:
extrai e roda `GerenciadorDeProjetos/bin/GerenciadorDeProjetos`.

## Dados e notificações no Linux

- Os dados ficam em `~/.gerenciador-projetos/`, igual ao Windows.
- O banner de prazos no Dashboard funciona sempre. O balão de notificação depende de o ambiente
  gráfico oferecer bandeja do sistema (alguns, como o GNOME puro, não oferecem); sem ela,
  só o banner avisa.

## Observação

Esta versão foi montada num computador Windows e **não foi executada em Linux** por quem a gerou.
Se algo falhar, abra um terminal, rode `./executar.sh` e leia a mensagem de erro.
