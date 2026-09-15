package br.edu.trabalho.gerenciadorprojetos.ui;

import br.edu.trabalho.gerenciadorprojetos.domain.Etapa;
import br.edu.trabalho.gerenciadorprojetos.domain.Projeto;
import br.edu.trabalho.gerenciadorprojetos.domain.Status;
import br.edu.trabalho.gerenciadorprojetos.domain.Tarefa;
import br.edu.trabalho.gerenciadorprojetos.service.ProjetoService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;

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
        Hyperlink dashboard = new Hyperlink("Dashboard");
        dashboard.setOnAction(e -> new DashboardView(service, navigator).exibir());
        Hyperlink doProjeto = new Hyperlink(projeto.getNome());
        doProjeto.setOnAction(e -> new ProjetoView(service, navigator, projeto.getId()).exibir());
        Label atual = new Label(etapa.getNome());
        atual.setStyle("-fx-font-weight: bold;");
        HBox breadcrumb = new HBox(dashboard, new Label(" › "), doProjeto, new Label(" › "), atual);
        breadcrumb.setAlignment(Pos.CENTER_LEFT);
        breadcrumb.setStyle("-fx-font-size: 12px;");

        Label titulo = new Label("Tarefas");
        titulo.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        Button nova = new Button("+ Nova tarefa");
        nova.setOnAction(e -> criarTarefa());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox cabecalho = new HBox(titulo, spacer, nova);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        VBox lista = new VBox();
        for (Tarefa tarefa : etapa.getTarefas()) {
            lista.getChildren().add(linhaTarefa(projeto, etapa, tarefa));
        }
        if (etapa.getTarefas().isEmpty()) {
            lista.getChildren().add(new Label("Nenhuma tarefa cadastrada ainda."));
        }

        VBox raiz = new VBox(14, breadcrumb, cabecalho, lista);
        raiz.setPadding(new Insets(20));
        ScrollPane scroll = new ScrollPane(raiz);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private Node linhaTarefa(Projeto projeto, Etapa etapa, Tarefa tarefa) {
        CheckBox concluida = new CheckBox();
        concluida.setSelected(tarefa.getStatus() == Status.CONCLUIDA);
        concluida.setOnAction(e -> {
            tarefa.setStatus(concluida.isSelected() ? Status.CONCLUIDA : Status.EM_ANDAMENTO);
            service.salvarProjeto(projeto);
            exibir();
        });

        Label titulo = new Label(tarefa.getTitulo());
        titulo.setStyle("-fx-font-size: 14px;");
        HBox.setHgrow(titulo, Priority.ALWAYS);

        Label data = new Label(tarefa.getDataLimite() != null ? tarefa.getDataLimite().format(FORMATO_DATA) : "sem data");
        data.setStyle("-fx-font-size: 11px; -fx-text-fill: #7c7669;");

        Button editar = new Button("editar");
        editar.setStyle("-fx-font-size: 10px;");
        editar.setOnAction(e -> editarTarefa(projeto, etapa, tarefa));

        Button excluir = new Button("excluir");
        excluir.setStyle("-fx-font-size: 10px;");
        excluir.setOnAction(e -> {
            service.excluirTarefa(projeto.getId(), etapa.getId(), tarefa.getId());
            exibir();
        });

        HBox linha = new HBox(12, concluida, titulo, data, StatusBadge.criar(tarefa.getStatus(), tarefa.estaAtrasada()), editar, excluir);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.setPadding(new Insets(10, 0, 10, 0));
        linha.setStyle("-fx-border-color: transparent transparent #c6c0b2 transparent;");
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
