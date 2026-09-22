package br.edu.trabalho.gerenciadorprojetos.ui;

import br.edu.trabalho.gerenciadorprojetos.domain.Etapa;
import br.edu.trabalho.gerenciadorprojetos.domain.Projeto;
import br.edu.trabalho.gerenciadorprojetos.domain.Status;
import br.edu.trabalho.gerenciadorprojetos.domain.Tarefa;
import br.edu.trabalho.gerenciadorprojetos.service.ProjetoService;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

/** Tela 03 dos wireframes: lista de Tarefas de uma Etapa. */
public class EtapaView {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ProjetoService service;
    private final Navigator navigator;
    private final String projetoId;
    private final String etapaId;

    public EtapaView(ProjetoService service, Navigator navigator, String projetoId, String etapaId) {
        this.service = service;
        this.navigator = navigator;
        this.projetoId = projetoId;
        this.etapaId = etapaId;
    }

    public void exibir() {
        Projeto projeto = service.buscarProjeto(projetoId);
        Etapa etapa = projeto.getEtapas().stream().filter(e -> e.getId().equals(etapaId)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Etapa não encontrada: " + etapaId));
        navigator.mostrar(construir(projeto, etapa), etapa.getNome());
    }

    private Parent construir(Projeto projeto, Etapa etapa) {
        Button dashboard = new Button("← Dashboard");
        dashboard.getStyleClass().add("breadcrumb-back");
        dashboard.setOnAction(e -> new DashboardView(service, navigator).exibir());

        Hyperlink doProjeto = new Hyperlink(projeto.getNome());
        doProjeto.getStyleClass().add("breadcrumb-link");
        doProjeto.setOnAction(e -> new ProjetoView(service, navigator, projeto.getId()).exibir());

        Label atual = new Label(etapa.getNome());
        atual.getStyleClass().add("breadcrumb-current");

        Label sep1 = new Label("›");
        sep1.getStyleClass().add("breadcrumb-sep");
        Label sep2 = new Label("›");
        sep2.getStyleClass().add("breadcrumb-sep");

        HBox breadcrumb = new HBox(8, dashboard, sep1, doProjeto, sep2, atual);
        breadcrumb.setAlignment(Pos.CENTER_LEFT);

        Label titulo = new Label("Tarefas");
        titulo.getStyleClass().add("page-title");
        Button nova = new Button("+ Nova tarefa");
        nova.getStyleClass().add("btn-secondary");
        nova.setOnAction(e -> criarTarefa());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox cabecalho = new HBox(titulo, spacer, nova);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        VBox lista = new VBox();
        lista.getStyleClass().addAll("card", "item-list");
        List<Tarefa> tarefas = etapa.getTarefas();
        if (tarefas.isEmpty()) {
            Label vazio = new Label("Nenhuma tarefa cadastrada ainda.");
            vazio.getStyleClass().add("empty-hint");
            lista.getChildren().add(vazio);
        } else {
            for (int i = 0; i < tarefas.size(); i++) {
                Node linha = linhaTarefa(projeto, etapa, tarefas.get(i));
                if (i == tarefas.size() - 1) {
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

    private Node linhaTarefa(Projeto projeto, Etapa etapa, Tarefa tarefa) {
        CheckBox concluida = new CheckBox();
        concluida.getStyleClass().add("task-checkbox");
        concluida.setSelected(tarefa.getStatus() == Status.CONCLUIDA);
        concluida.setOnAction(e -> {
            tarefa.setStatus(concluida.isSelected() ? Status.CONCLUIDA : Status.EM_ANDAMENTO);
            service.salvarProjeto(projeto);
            exibir();
        });

        Label titulo = new Label(tarefa.getTitulo());
        titulo.getStyleClass().add("item-title");
        if (tarefa.getStatus() == Status.CONCLUIDA) {
            titulo.getStyleClass().add("item-title-done");
        }

        Label data = new Label(tarefa.getDataLimite() != null ? tarefa.getDataLimite().format(FORMATO_DATA) : "sem data");
        data.getStyleClass().add(tarefa.estaAtrasada() ? "item-date-late" : "item-date");

        Button editar = new Button("editar");
        editar.getStyleClass().add("btn-ghost");
        editar.setOnMouseClicked(Event::consume);
        editar.setOnAction(e -> editarTarefa(projeto, etapa, tarefa));

        Button excluir = new Button("excluir");
        excluir.getStyleClass().addAll("btn-ghost", "btn-ghost-danger");
        excluir.setOnAction(e -> {
            service.excluirTarefa(projeto.getId(), etapa.getId(), tarefa.getId());
            exibir();
        });

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);

        HBox linha = new HBox(12, concluida, titulo, espacador, data,
                StatusBadge.criar(tarefa.getStatus(), tarefa.estaAtrasada()), editar, excluir);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.getStyleClass().add("item-row");
        return linha;
    }

    private void criarTarefa() {
        ItemFormDialog.abrir("Nova tarefa").ifPresent(resultado -> {
            service.criarTarefa(projetoId, etapaId, resultado.titulo(), resultado.descricao(), resultado.dataLimite());
            exibir();
        });
    }

    private void editarTarefa(Projeto projeto, Etapa etapa, Tarefa tarefa) {
        var valoresAtuais = new ItemFormDialog.Resultado(tarefa.getTitulo(), tarefa.getDescricao(), tarefa.getDataLimite());
        ItemFormDialog.abrir("Editar tarefa", "Título", valoresAtuais).ifPresent(resultado -> {
            service.editarTarefa(projeto.getId(), etapa.getId(), tarefa.getId(), resultado.titulo(), resultado.descricao(), resultado.dataLimite());
            exibir();
        });
    }
}
