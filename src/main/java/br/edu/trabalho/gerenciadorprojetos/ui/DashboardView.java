package br.edu.trabalho.gerenciadorprojetos.ui;

import br.edu.trabalho.gerenciadorprojetos.domain.Etapa;
import br.edu.trabalho.gerenciadorprojetos.domain.Projeto;
import br.edu.trabalho.gerenciadorprojetos.domain.Status;
import br.edu.trabalho.gerenciadorprojetos.repository.ArmazenamentoConfig;
import br.edu.trabalho.gerenciadorprojetos.repository.JsonProjetoRepository;
import br.edu.trabalho.gerenciadorprojetos.service.PrazoItem;
import br.edu.trabalho.gerenciadorprojetos.service.ProjetoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Tela 01 dos wireframes: lista de projetos + painel "Próximos prazos" (RF09). */
public class DashboardView {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM");

    private final ProjetoService service;
    private final Navigator navigator;
    private TextField buscaFiltro;

    public DashboardView(ProjetoService service, Navigator navigator) {
        this.service = service;
        this.navigator = navigator;
    }

    public void exibir() {
        navigator.mostrar(construir(), "Dashboard");
    }

    private Parent construir() {
        List<Projeto> projetos = service.listarProjetos();
        List<PrazoItem> prazos = service.listarProximosPrazos();

        VBox raiz = new VBox(22, cabecalho(projetos), painelResumo(projetos, prazos),
                secaoProjetos(projetos), secaoPrazos(prazos));
        raiz.setPadding(new Insets(28, 32, 32, 32));

        ScrollPane scroll = new ScrollPane(raiz);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll-area");
        return scroll;
    }

    private Node cabecalho(List<Projeto> projetos) {
        Label titulo = new Label("Meus projetos");
        titulo.getStyleClass().add("page-title");
        long atrasados = projetos.stream().mapToLong(Projeto::contarEtapasAtrasadas).sum();
        Label subtitulo = new Label(projetos.size() + " projetos · " + atrasados + " itens atrasados");
        subtitulo.getStyleClass().add("page-subtitle");
        VBox titulos = new VBox(2, titulo, subtitulo);

        Button localDados = new Button("📁 Local dos dados");
        localDados.getStyleClass().add("btn-ghost");
        Tooltip.install(localDados, new Tooltip("Pasta atual: " + ArmazenamentoConfig.pastaAtual()));
        localDados.setOnAction(e -> escolherLocalArmazenamento());

        TextField busca = new TextField();
        busca.setPromptText("Buscar projeto...");
        busca.getStyleClass().add("form-field");
        busca.setPrefWidth(200);

        Button novoProjeto = new Button("+ Novo projeto");
        novoProjeto.getStyleClass().add("btn-primary");
        novoProjeto.setOnAction(e -> criarProjeto());

        HBox acoes = new HBox(10, localDados, busca, novoProjeto);
        acoes.setAlignment(Pos.CENTER_RIGHT);

        HBox cabecalho = new HBox(titulos, espacador(), acoes);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        buscaFiltro = busca;
        return cabecalho;
    }

    private Node painelResumo(List<Projeto> projetos, List<PrazoItem> prazos) {
        long atrasados = prazos.stream().filter(PrazoItem::atrasado).count();
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(7);
        long proximos = prazos.stream()
                .filter(p -> !p.atrasado() && p.dataLimite() != null
                        && !p.dataLimite().isBefore(hoje) && !p.dataLimite().isAfter(limite))
                .count();

        HBox linha = new HBox(14,
                statTile("PROJETOS ATIVOS", String.valueOf(projetos.size()), "no total", false),
                statTile("ITENS ATRASADOS", String.valueOf(atrasados), "etapas e tarefas", atrasados > 0),
                statTile("PRÓXIMOS 7 DIAS", String.valueOf(proximos), "etapas e tarefas", false));
        for (Node n : linha.getChildren()) {
            HBox.setHgrow(n, Priority.ALWAYS);
        }
        return linha;
    }

    private Node statTile(String rotulo, String valor, String dica, boolean alerta) {
        Label lblRotulo = new Label(rotulo);
        lblRotulo.getStyleClass().add("stat-tile-label");
        Label lblValor = new Label(valor);
        lblValor.getStyleClass().add("stat-tile-value");
        if (alerta) {
            lblValor.getStyleClass().add("stat-tile-value-warn");
        }
        Label lblDica = new Label(dica);
        lblDica.getStyleClass().add("stat-tile-hint");

        VBox tile = new VBox(4, lblRotulo, lblValor, lblDica);
        tile.getStyleClass().addAll("card", "stat-tile");
        return tile;
    }

    private Node secaoProjetos(List<Projeto> projetos) {
        Label rotulo = new Label("PROJETOS");
        rotulo.getStyleClass().add("section-label");

        FlowPane grade = new FlowPane(14, 14);
        ObservableList<Projeto> lista = FXCollections.observableArrayList(projetos);
        FilteredList<Projeto> filtrados = new FilteredList<>(lista, p -> true);
        if (buscaFiltro != null) {
            buscaFiltro.textProperty().addListener((obs, antigo, novo) -> {
                String termo = novo == null ? "" : novo.trim().toLowerCase();
                filtrados.setPredicate(p -> termo.isEmpty() || p.getNome().toLowerCase().contains(termo));
                redesenharGrade(grade, filtrados);
            });
        }
        redesenharGrade(grade, filtrados);

        VBox secao = new VBox(10, rotulo, grade);
        return secao;
    }

    private void redesenharGrade(FlowPane grade, List<Projeto> projetos) {
        grade.getChildren().clear();
        for (Projeto projeto : projetos) {
            grade.getChildren().add(cartaoProjeto(projeto));
        }
        grade.getChildren().add(cartaoNovoProjeto());
    }

    private Node cartaoProjeto(Projeto projeto) {
        Label nome = new Label(projeto.getNome());
        nome.getStyleClass().add("project-card-name");
        nome.setWrapText(true);
        HBox.setHgrow(nome, Priority.ALWAYS);

        Button editar = new Button("editar");
        editar.getStyleClass().add("btn-ghost");
        editar.setOnMouseClicked(javafx.event.Event::consume);
        editar.setOnAction(e -> editarProjeto(projeto));
        HBox linhaTopo = new HBox(8, nome, editar);
        linhaTopo.setAlignment(Pos.TOP_LEFT);

        Status progresso = projeto.calcularProgresso();
        Label badge = StatusBadge.criar(progresso, false);

        int totalEtapas = projeto.getEtapas().size();
        long concluidas = projeto.getEtapas().stream().filter(e -> e.getStatus() == Status.CONCLUIDA).count();
        double fracao = totalEtapas == 0 ? 0 : (double) concluidas / totalEtapas;
        ProgressBar barra = new ProgressBar(fracao);
        barra.getStyleClass().add("project-progress");
        barra.setMaxWidth(Double.MAX_VALUE);

        long atrasadas = projeto.contarEtapasAtrasadas();
        Label meta = new Label(totalEtapas + " etapas · " + atrasadas + " atrasadas");
        meta.getStyleClass().add("project-card-meta");

        HBox linhaMeta = new HBox(meta);
        linhaMeta.setAlignment(Pos.CENTER_LEFT);
        if (projeto.getDataLimite() != null) {
            Label prazo = new Label(projeto.getDataLimite().format(FORMATO_DATA));
            prazo.getStyleClass().add("project-card-deadline");
            if (projeto.getDataLimite().isBefore(LocalDate.now()) && progresso != Status.CONCLUIDA) {
                prazo.getStyleClass().add("project-card-deadline-late");
            }
            Region esp = new Region();
            HBox.setHgrow(esp, Priority.ALWAYS);
            linhaMeta.getChildren().addAll(esp, prazo);
        }

        VBox card = new VBox(10, linhaTopo, badge, barra, linhaMeta);
        card.getStyleClass().addAll("card", "project-card");
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> new ProjetoView(service, navigator, projeto.getId()).exibir());
        return card;
    }

    private Node cartaoNovoProjeto() {
        Label mais = new Label("+");
        mais.setStyle("-fx-font-size: 20px;");
        Label texto = new Label("Criar novo projeto");
        texto.getStyleClass().add("project-card-add-label");
        VBox card = new VBox(6, mais, texto);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("project-card-add");
        card.setOnMouseClicked(e -> criarProjeto());
        return card;
    }

    private void editarProjeto(Projeto projeto) {
        var valoresAtuais = new ItemFormDialog.Resultado(projeto.getNome(), projeto.getDescricao(), projeto.getDataLimite());
        ItemFormDialog.abrir("Editar projeto", "Nome", valoresAtuais).ifPresent(resultado -> {
            service.editarProjeto(projeto.getId(), resultado.titulo(), resultado.descricao(), resultado.dataLimite());
            exibir();
        });
    }

    private Node secaoPrazos(List<PrazoItem> prazos) {
        Label rotulo = new Label("PRÓXIMOS PRAZOS");
        rotulo.getStyleClass().add("section-label");

        VBox lista = new VBox();
        lista.getStyleClass().addAll("card", "item-list");
        if (prazos.isEmpty()) {
            Label vazio = new Label("Nenhum prazo cadastrado ainda.");
            vazio.getStyleClass().add("empty-hint");
            lista.getChildren().add(vazio);
        } else {
            for (int i = 0; i < prazos.size(); i++) {
                Node linha = linhaPrazo(prazos.get(i));
                if (i == prazos.size() - 1) {
                    linha.getStyleClass().add("last-row");
                }
                lista.getChildren().add(linha);
            }
        }

        return new VBox(10, rotulo, lista);
    }

    private Node linhaPrazo(PrazoItem item) {
        Region faixa = new Region();
        faixa.getStyleClass().addAll("stripe", StatusBadge.stripeStyleClass(item.status(), item.atrasado()));
        faixa.setPrefWidth(3);
        faixa.setMinWidth(3);
        faixa.setMaxHeight(Double.MAX_VALUE);

        Label desc = new Label(item.descricao());
        desc.getStyleClass().add("item-title");
        Label origem = new Label(item.origem());
        origem.getStyleClass().add("item-sub");
        VBox titulos = new VBox(2, desc, origem);

        Label data = new Label(item.dataLimite() != null ? item.dataLimite().format(FORMATO_DATA) : "sem data");
        data.getStyleClass().add(item.atrasado() ? "item-date-late" : "item-date");

        HBox linha = new HBox(12, faixa, titulos, espacador(), StatusBadge.criar(item.status(), item.atrasado()), data);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.getStyleClass().add("item-row");
        return linha;
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

    /** RF10 (docs/PRD.md): deixa o usuário escolher em que pasta os dados ficam salvos. */
    private void escolherLocalArmazenamento() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Escolher pasta para salvar os projetos");
        Path pastaAtual = ArmazenamentoConfig.pastaAtual();
        if (Files.isDirectory(pastaAtual)) {
            chooser.setInitialDirectory(pastaAtual.toFile());
        }

        File escolhida = chooser.showDialog(navigator.getStage());
        if (escolhida == null) {
            return;
        }

        try {
            Path novoArquivo = ArmazenamentoConfig.escolherNovaPasta(escolhida.toPath());
            ProjetoService novoService = new ProjetoService(new JsonProjetoRepository(novoArquivo));
            new DashboardView(novoService, navigator).exibir();
        } catch (RuntimeException ex) {
            Alert alerta = new Alert(Alert.AlertType.ERROR,
                    "Não foi possível usar essa pasta para salvar os dados: " + ex.getMessage());
            alerta.showAndWait();
        }
    }
}
