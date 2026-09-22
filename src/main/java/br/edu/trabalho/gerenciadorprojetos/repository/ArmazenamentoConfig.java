package br.edu.trabalho.gerenciadorprojetos.repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.prefs.Preferences;

/**
 * Guarda ONDE o arquivo {@code dados.json} deve ficar (RF10, docs/PRD.md). O
 * caminho escolhido pelo usuário é lembrado via {@link Preferences} (API do
 * próprio JDK, sem dependência nova) — ver
 * docs/adr/ADR-005-local-de-armazenamento-configuravel.md.
 */
public final class ArmazenamentoConfig {

    private static final String CHAVE_PASTA = "pastaDados";
    private static final String NOME_ARQUIVO = "dados.json";

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

    public static Path arquivoAtual() {
        return pastaAtual().resolve(NOME_ARQUIVO);
    }

    /**
     * Troca a pasta de armazenamento e devolve o novo caminho do arquivo de
     * dados. Se a pasta escolhida já tiver um {@code dados.json} (ex.: pasta
     * compartilhada usada antes), ele é mantido como está; caso contrário, os
     * dados da pasta antiga são movidos para lá, para não perder nada que já
     * foi cadastrado.
     */
    public static Path escolherNovaPasta(Path novaPasta) {
        Path arquivoAntigo = arquivoAtual();
        Path arquivoNovo = novaPasta.resolve(NOME_ARQUIVO);
        try {
            Files.createDirectories(novaPasta);
            if (!Files.exists(arquivoNovo) && Files.exists(arquivoAntigo)) {
                Files.move(arquivoAntigo, arquivoNovo, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao mover dados para " + novaPasta, e);
        }
        preferencias().put(CHAVE_PASTA, novaPasta.toString());
        return arquivoNovo;
    }

    private static Preferences preferencias() {
        return Preferences.userNodeForPackage(ArmazenamentoConfig.class);
    }
}
