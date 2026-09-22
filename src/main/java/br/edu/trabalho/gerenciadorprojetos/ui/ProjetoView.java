package br.edu.trabalho.gerenciadorprojetos.ui;

import br.edu.trabalho.gerenciadorprojetos.domain.Etapa;
import br.edu.trabalho.gerenciadorprojetos.domain.Projeto;
import br.edu.trabalho.gerenciadorprojetos.domain.Status;
import br.edu.trabalho.gerenciadorprojetos.service.ProjetoService;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

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
        Button voltar = new Button("← Dashboard");
        voltar.getStyleClass().add("breadcrumb-back");
        voltar.setOnAction(e -> new DashboardView(service, navigator).exibir());

        Label separador = new Label("›");
        separador.getStyleClass().add("breadcrumb-sep");
        Label atual = new Label(projeto.getNome());
        atual.getStyleClass().add("breadcrumb-current");
        HBox breadcrumb = new HBox(8, voltar, separador, atual);
        breadcrumb.setAlignment(Pos.CENTER_LEFT);

        Label titulo = new Label("Etapas");
        titulo.getStyleClass().add("page-title");
        Button nova = new Button("+ Nova etapa");
        nova.getStyleClass().add("btn-secondary");
        nova.setOnAction(e -> criarEtapa());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox cabecalho = new HBox(titulo, spacer, nova);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        VBox lista = new VBox();
        lista.getStyleClass().addAll("card", "item-list");
        List<Etapa> etapas = projeto.getEtapas();
        if (etapas.isEmpty()) {
            Label vazio = new Label("Nenhuma etapa cadastrada ainda.");
            vazio.getStyleClass().add("empty-hint");
            lista.getChildren().add(vazio);
        } else {
            for (int i = 0; i < etapas.size(); i++) {
                Node linha = linhaEtapa(projeto, etapas.get(i));
                if (i == etapas.size() - 1) {
                    linha.getStyleClass().add("last-row");
                }
                lista.getChildren().add(linha);
            }
        }

        VBox raiz = new VBox(20, breadcrumb, cabecalho, lista);
        raiz.setPadding(new Insets(28, 32, 32, 32));
        ScrollPane scroll = new ScrollPane(raiz);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-area");
        return scroll;
    }

    private Node linhaEtapa(Projeto projeto, Etapa etapa) {
        CheckBox concluida = new CheckBox();
        concluida.getStyleClass().add("task-checkbox");
        concluida.setSelected(etapa.getStatus() == Status.CONCLUIDA);
        concluida.setOnAction(e -> {
            Status novoStatus = concluida.isSelected() ? Status.CONCLUIDA : Status.EM_ANDAMENTO;
            service.alterarStatusEtapa(projeto.getId(), etapa.getId(), novoStatus);
            exibir();
        });

        Label nome = new Label(etapa.getNome());
        nome.getStyleClass().add("item-title");
        if (etapa.getStatus() == Status.CONCLUIDA) {
            nome.getStyleClass().add("item-title-done");
        }
        nome.setOnMouseClicked(e -> new EtapaView(service, navigator, projeto.getId(), etapa.getId()).exibir());
        nome.setCursor(Cursor.HAND);

        Label data = new Label(etapa.getDataLimite() != null ? etapa.getDataLimite().format(FORMATO_DATA) : "sem data");
        data.getStyleClass().add(etapa.estaAtrasada() ? "item-date-late" : "item-date");

        Button editar = new Button("editar");
        editar.getStyleClass().add("btn-ghost");
        editar.setOnMouseClicked(Event::consume);
        editar.setOnAction(e -> editarEtapa(projeto, etapa));

        Button excluir = new Button("excluir");
        excluir.getStyleClass().addAll("btn-ghost", "btn-ghost-danger");
        excluir.setOnAction(e -> {
            service.excluirEtapa(projeto.getId(), etapa.getId());
            exibir();
        });

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        HBox linha = new HBox(12, concluida, nome, espacador, data,
                StatusBadge.criar(etapa.getStatus(), etapa.estaAtrasada()), editar, excluir);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.getStyleClass().add("item-row");
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
