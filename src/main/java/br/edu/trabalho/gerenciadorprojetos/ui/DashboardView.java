package br.edu.trabalho.gerenciadorprojetos.ui;

import br.edu.trabalho.gerenciadorprojetos.domain.Projeto;
import br.edu.trabalho.gerenciadorprojetos.service.PrazoItem;
import br.edu.trabalho.gerenciadorprojetos.service.ProjetoService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;

/** Tela 01 dos wireframes: lista de projetos + painel "Próximos prazos" (RF09). */
public class DashboardView {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM");

    private final ProjetoService service;
    private final Navigator navigator;

    public DashboardView(ProjetoService service, Navigator navigator) {
        this.service = service;
        this.navigator = navigator;
    }

    public void exibir() {
        navigator.mostrar(construir(), "Dashboard");
    }

    private Parent construir() {
        Button novoProjeto = new Button("+ Novo projeto");
        novoProjeto.setOnAction(e -> criarProjeto());

        Label titulo = new Label("Meus projetos");
        titulo.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        HBox cabecalho = new HBox(titulo, espacador(), novoProjeto);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        FlowPane grade = new FlowPane(12, 12);
        for (Projeto projeto : service.listarProjetos()) {
            grade.getChildren().add(cartaoProjeto(projeto));
        }

        VBox prazos = painelPrazos();

        VBox raiz = new VBox(16, cabecalho, grade, prazos);
        raiz.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(raiz);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private Node cartaoProjeto(Projeto projeto) {
        Label nome = new Label(projeto.getNome());
        nome.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        HBox.setHgrow(nome, Priority.ALWAYS);

        Button editar = new Button("editar");
        editar.setStyle("-fx-font-size: 10px;");
        editar.setOnAction(e -> {
            e.consume();
            editarProjeto(projeto);
        });
        HBox linhaTopo = new HBox(nome, editar);
        linhaTopo.setAlignment(Pos.CENTER_LEFT);

        String linhaData = projeto.getDataLimite() != null
                ? " · prazo " + projeto.getDataLimite().format(FORMATO_DATA)
                : "";
        Label meta = new Label(projeto.getEtapas().size() + " etapas · "
                + projeto.contarEtapasAtrasadas() + " atrasadas" + linhaData);
        meta.setStyle("-fx-text-fill: #7c7669; -fx-font-size: 11.5px;");

        VBox card = new VBox(8, linhaTopo, StatusBadge.criar(projeto.calcularProgresso(), false), meta);
        card.setPadding(new Insets(14));
        card.setPrefWidth(220);
        card.setStyle("-fx-border-color: #c6c0b2; -fx-border-radius: 5; -fx-background-radius: 5;"
                + " -fx-background-color: white;");
        card.setCursor(javafx.scene.Cursor.HAND);
        nome.setOnMouseClicked(e -> new ProjetoView(service, navigator, projeto.getId()).exibir());
        meta.setOnMouseClicked(e -> new ProjetoView(service, navigator, projeto.getId()).exibir());
        return card;
    }

    private void editarProjeto(Projeto projeto) {
        var valoresAtuais = new ItemFormDialog.Resultado(projeto.getNome(), projeto.getDescricao(), projeto.getDataLimite());
        ItemFormDialog.abrir("Editar projeto", "Nome", valoresAtuais).ifPresent(resultado -> {
            service.editarProjeto(projeto.getId(), resultado.titulo(), resultado.descricao(), resultado.dataLimite());
            exibir();
        });
    }

    private VBox painelPrazos() {
        Label titulo = new Label("PRÓXIMOS PRAZOS");
        titulo.setStyle("-fx-font-size: 11px; -fx-text-fill: #7c7669; -fx-font-weight: bold;");

        VBox lista = new VBox(6);
        for (PrazoItem item : service.listarProximosPrazos()) {
            HBox linha = new HBox(10);
            linha.setAlignment(Pos.CENTER_LEFT);
            Label desc = new Label(item.descricao() + "  —  " + item.origem());
            desc.setStyle("-fx-font-size: 12.5px;");
            Label data = new Label(item.dataLimite() != null ? item.dataLimite().format(FORMATO_DATA) : "sem data");
            data.setStyle("-fx-font-size: 11px; -fx-text-fill: #7c7669;");
            linha.getChildren().addAll(desc, espacador(), data, StatusBadge.criar(item.status(), item.atrasado()));
            lista.getChildren().add(linha);
        }
        if (service.listarProximosPrazos().isEmpty()) {
            lista.getChildren().add(new Label("Nenhum prazo cadastrado ainda."));
        }

        VBox painel = new VBox(10, titulo, lista);
        painel.setPadding(new Insets(14));
        painel.setStyle("-fx-border-color: #938c7a; -fx-border-style: dashed; -fx-border-radius: 5;");
        return painel;
    }

    private Region espacador() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    private void criarProjeto() {
        ItemFormDialog.abrir("Novo projeto").ifPresent(resultado -> {
            service.criarProjeto(resultado.titulo(), resultado.descricao());
            exibir();
        });
    }
}
