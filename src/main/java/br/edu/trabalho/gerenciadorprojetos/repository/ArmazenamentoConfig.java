package br.edu.trabalho.gerenciadorprojetos.repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

/**
 * Guarda ONDE os arquivos de dados ficam (RF10, docs/PRD.md). O caminho
 * escolhido pelo usuário é lembrado via {@link Preferences} (API do próprio
 * JDK, sem dependência nova) — ver
 * docs/adr/ADR-005-local-de-armazenamento-configuravel.md.
 *
 * <p>Com múltiplos usuários (ADR-006), a pasta contém {@code usuarios.json} e um
 * {@code dados-<login>.json} por usuário. O {@code dados.json} da versão
 * single-user é adotado pelo primeiro usuário cadastrado.
 */
public final class ArmazenamentoConfig {

    private static final String CHAVE_PASTA = "pastaDados";
    private static final String NOME_ARQUIVO = "dados.json";
    private static final String CHAVE_NOTIFICACOES = "notificacoes.";
    /** Arquivos que pertencem ao app e devem acompanhar a pasta ao mudá-la (inclui os .bak). */
    private static final Pattern ARQUIVOS_DO_APP =
            Pattern.compile("usuarios\\.json|dados(-[a-z0-9._-]+)?\\.json(\\.bak)?");

    private ArmazenamentoConfig() {
    }

    /** Pasta atual: a última escolhida pelo usuário, ou o padrão (~/.gerenciador-projetos). */
    public static Path pastaAtual() {
        String salva = preferencias().get(CHAVE_PASTA, null);
        return salva != null ? Path.of(salva) : pastaPadrao();
    }

    public static Path pastaPadrao() {
        return Path.of(System.getProperty("user.home"), ".gerenciador-projetos");
    }

    /** Arquivo da versão single-user; hoje só serve de origem para a migração ao primeiro usuário. */
    public static Path arquivoAtual() {
        return pastaAtual().resolve(NOME_ARQUIVO);
    }

    public static Path arquivoDoUsuario(String login) {
        return arquivoDoUsuario(pastaAtual(), login);
    }

    public static Path arquivoDoUsuario(Path pasta, String login) {
        return pasta.resolve("dados-" + login + ".json");
    }

    /**
     * Troca a pasta de armazenamento. Se a nova pasta ainda não tiver dados do
     * app, os arquivos da pasta antiga (contas e dados de todos os usuários) são
     * movidos para lá; se já tiver (ex.: pasta compartilhada usada antes), ela é
     * mantida como está. Devolve o caminho do {@code dados.json} da nova pasta.
     */
    public static Path escolherNovaPasta(Path novaPasta) {
        migrarArquivos(pastaAtual(), novaPasta);
        preferencias().put(CHAVE_PASTA, novaPasta.toString());
        return novaPasta.resolve(NOME_ARQUIVO);
    }

    /** Visível para teste: move os arquivos do app de {@code de} para {@code para}, sem sobrescrever dados existentes. */
    public static void migrarArquivos(Path de, Path para) {
        try {
            Files.createDirectories(para);
            if (de.equals(para) || !Files.isDirectory(de)) {
                return;
            }
            if (Files.exists(para.resolve("usuarios.json")) || Files.exists(para.resolve(NOME_ARQUIVO))) {
                return;
            }
            List<Path> arquivos;
            try (var stream = Files.list(de)) {
                arquivos = stream
                        .filter(p -> ARQUIVOS_DO_APP.matcher(p.getFileName().toString()).matches())
                        .toList();
            }
            for (Path arquivo : arquivos) {
                Files.move(arquivo, para.resolve(arquivo.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao mover dados para " + para, e);
        }
    }

    /** Preferência por usuário: avisar sobre prazos (padrão: ligado). */
    public static boolean notificacoesAtivas(String login) {
        return preferencias().getBoolean(CHAVE_NOTIFICACOES + login, true);
    }

    public static void definirNotificacoes(String login, boolean ativas) {
        preferencias().putBoolean(CHAVE_NOTIFICACOES + login, ativas);
    }

    private static Preferences preferencias() {
        return Preferences.userNodeForPackage(ArmazenamentoConfig.class);
    }
}
