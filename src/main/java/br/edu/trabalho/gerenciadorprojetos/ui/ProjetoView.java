package br.edu.trabalho.gerenciadorprojetos.ui;

import br.edu.trabalho.gerenciadorprojetos.domain.Etapa;
import br.edu.trabalho.gerenciadorprojetos.domain.Projeto;
import br.edu.trabalho.gerenciadorprojetos.service.ProjetoService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;

/** Tela 02 dos wireframes: lista de Etapas de um Projeto. */
public class ProjetoView {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ProjetoService service;
    private final Navigator navigator;
    private final String projetoId;

    public ProjetoView(ProjetoService service, Navigator navigator, String projetoId) {
        this.service = service;
        this.navigator = navigator;
        this.projetoId = projetoId;
    }

    public void exibir() {
        Projeto projeto = service.buscarProjeto(projetoId);
        navigator.mostrar(construir(projeto), projeto.getNome());
    }

    private Parent construir(Projeto projeto) {
        Hyperlink voltar = new Hyperlink("Dashboard");
        voltar.setOnAction(e -> new DashboardView(service, navigator).exibir());
        Label separador = new Label(" › ");
        Label atual = new Label(projeto.getNome());
        atual.setStyle("-fx-font-weight: bold;");
        HBox breadcrumb = new HBox(voltar, separador, atual);
        breadcrumb.setAlignment(Pos.CENTER_LEFT);
        breadcrumb.setStyle("-fx-font-size: 12px;");

        Label titulo = new Label("Etapas");
        titulo.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        Button nova = new Button("+ Nova etapa");
        nova.setOnAction(e -> criarEtapa());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox cabecalho = new HBox(titulo, spacer, nova);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        VBox lista = new VBox();
        for (Etapa etapa : projeto.getEtapas()) {
            lista.getChildren().add(linhaEtapa(projeto, etapa));
        }
        if (projeto.getEtapas().isEmpty()) {
            lista.getChildren().add(new Label("Nenhuma etapa cadastrada ainda."));
        }

        VBox raiz = new VBox(14, breadcrumb, cabecalho, lista);
        raiz.setPadding(new Insets(20));
        ScrollPane scroll = new ScrollPane(raiz);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private Node linhaEtapa(Projeto projeto, Etapa etapa) {
        javafx.scene.control.CheckBox concluida = new javafx.scene.control.CheckBox();
        concluida.setSelected(etapa.getStatus() == br.edu.trabalho.gerenciadorprojetos.domain.Status.CONCLUIDA);
        concluida.setOnAction(e -> {
            var novoStatus = concluida.isSelected()
                    ? br.edu.trabalho.gerenciadorprojetos.domain.Status.CONCLUIDA
                    : br.edu.trabalho.gerenciadorprojetos.domain.Status.EM_ANDAMENTO;
            service.alterarStatusEtapa(projeto.getId(), etapa.getId(), novoStatus);
            exibir();
        });

        Label nome = new Label(etapa.getNome());
        nome.setStyle("-fx-font-size: 14px;");
        nome.setOnMouseClicked(e -> new EtapaView(service, navigator, projeto.getId(), etapa.getId()).exibir());
        nome.setCursor(javafx.scene.Cursor.HAND);
        HBox.setHgrow(nome, Priority.ALWAYS);

        Label data = new Label(etapa.getDataLimite() != null ? etapa.getDataLimite().format(FORMATO_DATA) : "sem data");
        data.setStyle("-fx-font-size: 11px; -fx-text-fill: #7c7669;");

        Button editar = new Button("editar");
        editar.setStyle("-fx-font-size: 10px;");
        editar.setOnAction(e -> editarEtapa(projeto, etapa));

        Button excluir = new Button("excluir");
        excluir.getStyleClass().add("link-like");
        excluir.setStyle("-fx-font-size: 10px;");
        excluir.setOnAction(e -> {
            service.excluirEtapa(projeto.getId(), etapa.getId());
            exibir();
        });

        HBox linha = new HBox(12, concluida, nome, data, StatusBadge.criar(etapa.getStatus(), etapa.estaAtrasada()), editar, excluir);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.setPadding(new Insets(10, 0, 10, 0));
        linha.setStyle("-fx-border-color: transparent transparent #c6c0b2 transparent;");
        return linha;
    }

    private void criarEtapa() {
        ItemFormDialog.abrir("Nova etapa").ifPresent(resultado -> {
            service.criarEtapa(projetoId, resultado.titulo(), resultado.descricao(), resultado.dataLimite());
            exibir();
        });
    }

    private void editarEtapa(Projeto projeto, Etapa etapa) {
        var valoresAtuais = new ItemFormDialog.Resultado(etapa.getNome(), etapa.getDescricao(), etapa.getDataLimite());
        ItemFormDialog.abrir("Editar etapa", "Nome", valoresAtuais).ifPresent(resultado -> {
            service.editarEtapa(projeto.getId(), etapa.getId(), resultado.titulo(), resultado.descricao(), resultado.dataLimite());
            exibir();
        });
    }
}
