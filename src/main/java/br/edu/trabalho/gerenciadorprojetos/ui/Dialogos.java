package br.edu.trabalho.gerenciadorprojetos.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

/** Diálogos padronizados (confirmação de exclusão, avisos) em português. */
final class Dialogos {

    private Dialogos() {
    }

    /** Pede confirmação antes de uma ação destrutiva; devolve true só se o usuário confirmar. */
    static boolean confirmarExclusao(String titulo, String mensagem) {
        ButtonType excluir = new ButtonType("Excluir", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensagem, excluir, cancelar);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.getDialogPane().getStylesheets()
                .add(Dialogos.class.getResource("app.css").toExternalForm());
        return alerta.showAndWait().filter(b -> b == excluir).isPresent();
    }

    static void informar(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, mensagem);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    static void erro(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensagem);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
