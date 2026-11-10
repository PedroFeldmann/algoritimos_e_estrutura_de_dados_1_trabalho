package br.edu.trabalho.gerenciadorprojetos.ui;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Ponto de entrada usado só pelo executável empacotado (jpackage). O runtime
 * do JavaFX recusa iniciar quando a classe com {@code main} é ela mesma uma
 * {@code Application} rodando fora do module-path (ADR-001: este projeto não
 * usa module-info.java) — por isso o "main" real fica numa classe separada
 * que apenas repassa para {@link Main}.
 *
 * <p>Também grava a saída de diagnóstico em {@code ~/.gerenciador-projetos/log.txt}
 * (o executável não tem console), incluindo qual pipeline gráfico o JavaFX
 * escolheu ({@code prism.verbose}), para investigar problemas de vídeo em
 * outras máquinas.
 */
public final class Launcher {

    public static void main(String[] args) {
        if (System.getProperty("prism.verbose") == null) {
            System.setProperty("prism.verbose", "true");
        }
        redirecionarParaLog();
        System.out.println("=== " + LocalDateTime.now() + " | Java " + System.getProperty("java.version")
                + " | " + System.getProperty("os.name") + " " + System.getProperty("os.version")
                + " | prism.order=" + System.getProperty("prism.order")
                + " | glass.win.uiScale=" + System.getProperty("glass.win.uiScale"));
        Main.main(args);
    }

    private static void redirecionarParaLog() {
        try {
            Path pasta = Path.of(System.getProperty("user.home"), ".gerenciador-projetos");
            Files.createDirectories(pasta);
            OutputStream arquivo = new FileOutputStream(pasta.resolve("log.txt").toFile(), false);
            System.setOut(new PrintStream(new Duplo(System.out, arquivo), true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(new Duplo(System.err, arquivo), true, StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException e) {
            // Sem log, o app ainda deve abrir normalmente.
        }
    }

    /** Escreve em dois destinos (console original + arquivo de log). */
    private static final class Duplo extends OutputStream {
        private final OutputStream a;
        private final OutputStream b;

        Duplo(OutputStream a, OutputStream b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public synchronized void write(int c) throws IOException {
            a.write(c);
            b.write(c);
        }

        @Override
        public synchronized void write(byte[] buf, int off, int len) throws IOException {
            a.write(buf, off, len);
            b.write(buf, off, len);
        }

        @Override
        public synchronized void flush() throws IOException {
            a.flush();
            b.flush();
        }
    }
}
