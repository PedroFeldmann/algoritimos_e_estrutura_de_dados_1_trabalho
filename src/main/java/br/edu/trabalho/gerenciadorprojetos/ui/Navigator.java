package br.edu.trabalho.gerenciadorprojetos.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Troca o conteúdo da janela principal entre telas (Dashboard/Projeto/Etapa).
 * Sem framework de navegação — cada tela chama Navigator ao clicar em algo
 * (ADR-003: UI é a camada mais externa, não tem outras dependendo dela).
 */
public class Navigator {

    private final Stage stage;

    public Navigator(Stage stage) {
        this.stage = stage;
    }

    public void mostrar(Parent root, String titulo) {
        Scene cena = stage.getScene();
        if (cena == null) {
            cena = new Scene(root, 900, 620);
            cena.getStylesheets().add(getClass().getResource("app.css").toExternalForm());
            stage.setScene(cena);
        } else {
            cena.setRoot(root);
        }
        stage.setTitle("Gerenciador de Projetos — " + titulo);
    }

    /** Exposto para telas que precisam de uma janela "dona" para diálogos nativos (ex.: DirectoryChooser). */
    public Stage getStage() {
        return stage;
    }
}
