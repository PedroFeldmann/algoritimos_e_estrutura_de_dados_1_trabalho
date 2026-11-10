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
        // Renderização por software: evita tela branca/bugs visuais em máquinas cujo
        // driver de vídeo não funciona bem com a aceleração (Direct3D/OpenGL) do JavaFX.
        // Quem quiser a GPU pode rodar com -Dprism.order=d3d,sw.
        if (System.getProperty("prism.order") == null) {
            System.setProperty("prism.order", "sw");
        }
        Main.main(args);
    }
}
