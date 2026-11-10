package br.edu.trabalho.gerenciadorprojetos.ui;

import br.edu.trabalho.gerenciadorprojetos.repository.ArmazenamentoConfig;
import br.edu.trabalho.gerenciadorprojetos.repository.JsonProjetoRepository;
import br.edu.trabalho.gerenciadorprojetos.repository.ProjetoRepository;
import br.edu.trabalho.gerenciadorprojetos.service.ProjetoService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Ponto de entrada. Monta a cadeia de dependências manualmente (ADR-003: sem
 * framework de injeção de dependência): primeiro a tela de login (ADR-006) e,
 * depois de entrar, o Dashboard com os dados do usuário.
 */
public class Main extends Application {

    private AgendadorNotificacoes agendador;

    @Override
    public void start(Stage stage) {
        // Falhas inesperadas (ex.: disco cheio ao gravar) viram um aviso em vez de
        // o app simplesmente parar de responder.
        Thread.setDefaultUncaughtExceptionHandler((t, ex) -> avisar(ex));

        Navigator navigator = new Navigator(stage);
        navigator.definirAoSair(() -> sair(navigator));
        abrirLogin(navigator);
        // Tamanho explícito: alguns gerenciadores de janela do Linux ignoram o
        // tamanho da Scene e abriam a janela minúscula.
        stage.setMinWidth(800);
        stage.setMinHeight(560);
        stage.setWidth(980);
        stage.setHeight(680);
        stage.centerOnScreen();
        stage.show();
        System.out.println("Janela: " + (int) stage.getWidth() + "x" + (int) stage.getHeight());
    }

    @Override
    public void stop() {
        if (agendador != null) {
            agendador.parar();
        }
        NotificacaoSistema.remover();
    }

    private void abrirLogin(Navigator navigator) {
        new LoginView(navigator, sessao -> entrar(navigator, sessao)).exibir();
    }

    private void entrar(Navigator navigator, Sessao sessao) {
        navigator.setSessao(sessao);
        ProjetoRepository repository = new JsonProjetoRepository(ArmazenamentoConfig.arquivoDoUsuario(sessao.login()));
        ProjetoService service = new ProjetoService(repository);

        agendador = new AgendadorNotificacoes(service, sessao.login());
        new DashboardView(service, navigator).exibir();
        agendador.iniciar();
    }

    private void sair(Navigator navigator) {
        if (agendador != null) {
            agendador.parar();
            agendador = null;
        }
        navigator.setSessao(null);
        abrirLogin(navigator);
    }

    private static void avisar(Throwable ex) {
        ex.printStackTrace();
        Runnable mostrar = () -> new Alert(Alert.AlertType.ERROR,
                "Ocorreu um erro: " + ex.getMessage()).showAndWait();
        if (Platform.isFxApplicationThread()) {
            mostrar.run();
        } else {
            Platform.runLater(mostrar);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
