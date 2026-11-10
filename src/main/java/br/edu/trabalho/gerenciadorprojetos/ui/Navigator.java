package br.edu.trabalho.gerenciadorprojetos.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Troca o conteúdo da janela principal entre telas (Login/Dashboard/Projeto/Etapa).
 * Sem framework de navegação — cada tela chama Navigator ao clicar em algo
 * (ADR-003: UI é a camada mais externa, não tem outras dependendo dela).
 * Também carrega o usuário logado e a ação de sair (ADR-006).
 */
public class Navigator {

    private final Stage stage;
    private Sessao sessao;
    private Runnable aoSair = () -> { };

    public Navigator(Stage stage) {
        this.stage = stage;
    }

    public void mostrar(Parent root, String titulo) {
        Scene cena = stage.getScene();
        if (cena == null) {
            cena = new Scene(root, 960, 640);
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

    Sessao getSessao() {
        return sessao;
    }

    void setSessao(Sessao sessao) {
        this.sessao = sessao;
    }

    void definirAoSair(Runnable aoSair) {
        this.aoSair = aoSair;
    }

    /** Encerra a sessão e volta para a tela de login. */
    void sair() {
        aoSair.run();
    }
}
