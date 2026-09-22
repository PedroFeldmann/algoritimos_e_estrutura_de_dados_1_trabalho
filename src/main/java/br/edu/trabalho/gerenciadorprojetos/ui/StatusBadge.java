package br.edu.trabalho.gerenciadorprojetos.ui;

import br.edu.trabalho.gerenciadorprojetos.domain.Status;
import javafx.scene.control.Label;

/** Badge de status usado nas três listas (Dashboard, Projeto, Etapa) — ver docs wireframes, legenda. */
final class StatusBadge {

    private StatusBadge() {
    }

    static Label criar(Status status, boolean atrasado) {
        Label label = new Label(atrasado ? "Atrasada" : status.getRotulo());
        label.getStyleClass().addAll("pill", "pill-" + variante(status, atrasado));
        return label;
    }

    /** Nome da classe CSS de faixa colorida (".stripe-xxx") equivalente ao status, para listas com indicador lateral. */
    static String stripeStyleClass(Status status, boolean atrasado) {
        return "stripe-" + variante(status, atrasado);
    }

    private static String variante(Status status, boolean atrasado) {
        if (atrasado) {
            return "danger";
        }
        return switch (status) {
            case NAO_INICIADA -> "neutral";
            case EM_ANDAMENTO -> "warning";
            case CONCLUIDA -> "success";
        };
    }
}
