package br.edu.trabalho.gerenciadorprojetos.ui;

import br.edu.trabalho.gerenciadorprojetos.domain.Status;
import javafx.scene.control.Label;

/** Badge de status usado nas três listas (Dashboard, Projeto, Etapa) — ver docs wireframes, legenda. */
final class StatusBadge {

    private StatusBadge() {
    }

    static Label criar(Status status, boolean atrasado) {
        Label label = new Label(atrasado ? "Atrasada" : status.getRotulo());
        label.setStyle(estilo(atrasado ? "late" : conforme(status)));
        return label;
    }

    private static String conforme(Status status) {
        return switch (status) {
            case NAO_INICIADA -> "todo";
            case EM_ANDAMENTO -> "doing";
            case CONCLUIDA -> "done";
        };
    }

    private static String estilo(String tipo) {
        String cor;
        String fundo;
        switch (tipo) {
            case "doing" -> { cor = "#a9660a"; fundo = "#f7e9d5"; }
            case "done" -> { cor = "#3f6b4f"; fundo = "#e2ebe3"; }
            case "late" -> { cor = "#a4342a"; fundo = "#f6e0dd"; }
            default -> { cor = "#7c7669"; fundo = "transparent"; }
        }
        return "-fx-text-fill: " + cor + "; -fx-background-color: " + fundo
                + "; -fx-padding: 2 8 2 8; -fx-background-radius: 12; -fx-font-size: 11px;";
    }
}
