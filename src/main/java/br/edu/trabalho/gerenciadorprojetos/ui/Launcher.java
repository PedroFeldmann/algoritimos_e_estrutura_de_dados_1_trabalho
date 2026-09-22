package br.edu.trabalho.gerenciadorprojetos.ui;

/**
 * Ponto de entrada usado só pelo executável empacotado (jpackage). O runtime
 * do JavaFX recusa iniciar quando a classe com {@code main} é ela mesma uma
 * {@code Application} rodando fora do module-path (ADR-001: este projeto não
 * usa module-info.java) — por isso o "main" real fica numa classe separada
 * que apenas repassa para {@link Main}.
 */
public final class Launcher {

    public static void main(String[] args) {
        Main.main(args);
    }
}
