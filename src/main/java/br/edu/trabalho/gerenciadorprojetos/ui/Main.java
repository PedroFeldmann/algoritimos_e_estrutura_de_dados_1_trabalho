package br.edu.trabalho.gerenciadorprojetos.ui;

import br.edu.trabalho.gerenciadorprojetos.repository.ArmazenamentoConfig;
import br.edu.trabalho.gerenciadorprojetos.repository.JsonProjetoRepository;
import br.edu.trabalho.gerenciadorprojetos.repository.ProjetoRepository;
import br.edu.trabalho.gerenciadorprojetos.service.ProjetoService;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Ponto de entrada. Monta a cadeia de dependências manualmente (ADR-003: sem
 * framework de injeção de dependência) e abre o Dashboard.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        ProjetoRepository repository = new JsonProjetoRepository(ArmazenamentoConfig.arquivoAtual());
        ProjetoService service = new ProjetoService(repository);
        Navigator navigator = new Navigator(stage);

        new DashboardView(service, navigator).exibir();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
