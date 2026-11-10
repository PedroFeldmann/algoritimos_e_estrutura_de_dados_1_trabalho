package br.edu.trabalho.gerenciadorprojetos.ui;

import br.edu.trabalho.gerenciadorprojetos.domain.Etapa;
import br.edu.trabalho.gerenciadorprojetos.domain.Projeto;
import br.edu.trabalho.gerenciadorprojetos.domain.Status;
import br.edu.trabalho.gerenciadorprojetos.domain.StatusProjeto;
import br.edu.trabalho.gerenciadorprojetos.repository.ArmazenamentoConfig;
import br.edu.trabalho.gerenciadorprojetos.service.NotificadorPrazos;
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
import javafx.stage.FileChooser;
import javafx.scene.control.CheckBox;

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
    private boolean mostrarArquivados;
    private final FiltroLista filtroPrazos = new FiltroLista();

    public DashboardView(ProjetoService service, Navigator navigator) {
        this.service = service;
        this.navigator = navigator;
    }

    public void exibir() {
        navigator.mostrar(construir(), "Dashboard");
    }

    private Parent construir() {
        List<Projeto> todos = service.listarProjetos();
        List<Projeto> projetos = todos.stream().filter(p -> p.getStatus() == StatusProjeto.ATIVO).toList();
        List<PrazoItem> prazos = service.listarProximosPrazos();
        List<Projeto> exibidos = mostrarArquivados ? todos : projetos;

        VBox raiz = new VBox(22, cabecalho(projetos), bannerPrazos(prazos), painelResumo(projetos, prazos),
                secaoProjetos(exibidos, todos.size() - projetos.size()), secaoPrazos(prazos));
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

        Button exportar = new Button("⬆ Exportar");
        exportar.getStyleClass().add("btn-ghost");
        Tooltip.install(exportar, new Tooltip("Salvar uma cópia de todos os projetos num arquivo"));
        exportar.setOnAction(e -> exportarDados());

        Button importar = new Button("⬇ Importar");
        importar.getStyleClass().add("btn-ghost");
        Tooltip.install(importar, new Tooltip("Carregar projetos de um arquivo exportado"));
        importar.setOnAction(e -> importarDados());

        Sessao sessao = navigator.getSessao();
        CheckBox avisos = new CheckBox("🔔 Avisar prazos");
        avisos.setSelected(sessao != null && ArmazenamentoConfig.notificacoesAtivas(sessao.login()));
        avisos.setDisable(sessao == null);
        Tooltip.install(avisos, new Tooltip("Notificação do sistema para itens atrasados ou que vencem em até "
                + NotificadorPrazos.DIAS_ANTECEDENCIA + " dias"));
        avisos.setOnAction(e -> ArmazenamentoConfig.definirNotificacoes(sessao.login(), avisos.isSelected()));

        // FlowPane: em janelas estreitas (ou com fontes mais largas, como no Linux)
        // as ações descem para a linha de baixo em vez de ficarem cortadas ("Ex…").
        FlowPane acoes = new FlowPane(10, 8, busca, novoProjeto, exportar, importar, localDados, avisos);
        acoes.setAlignment(Pos.CENTER_LEFT);

        buscaFiltro = busca;
        HBox linhaTitulo = new HBox(10, titulos, espacador());
        linhaTitulo.setAlignment(Pos.CENTER_LEFT);
        if (sessao != null) {
            Label usuario = new Label(sessao.nome());
            usuario.getStyleClass().add("page-subtitle");
            Button sair = new Button("Sair");
            sair.getStyleClass().add("btn-ghost");
            sair.setOnAction(e -> navigator.sair());
            linhaTitulo.getChildren().addAll(usuario, sair);
        }
        return new VBox(12, linhaTitulo, acoes);
    }

    /** Faixa de alerta no topo: itens atrasados ou vencendo em breve (independe da notificação do sistema). */
    private Node bannerPrazos(List<PrazoItem> prazos) {
        List<PrazoItem> pendentes = NotificadorPrazos.pendentesDeAviso(prazos, LocalDate.now());
        if (pendentes.isEmpty()) {
            Region vazio = new Region();
            vazio.setManaged(false);
            vazio.setVisible(false);
            return vazio;
        }
        Label texto = new Label("⚠ " + NotificadorPrazos.resumo(pendentes));
        texto.getStyleClass().add("alert-banner");
        texto.setMaxWidth(Double.MAX_VALUE);
        return texto;
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

    private Node secaoProjetos(List<Projeto> projetos, int qtdArquivados) {
        Label rotulo = new Label("PROJETOS");
        rotulo.getStyleClass().add("section-label");

        HBox cabecalhoSecao = new HBox(16, rotulo);
        cabecalhoSecao.setAlignment(Pos.CENTER_LEFT);
        if (qtdArquivados > 0 || mostrarArquivados) {
            CheckBox arquivados = new CheckBox("Mostrar arquivados (" + qtdArquivados + ")");
            arquivados.setSelected(mostrarArquivados);
            arquivados.setOnAction(e -> {
                mostrarArquivados = arquivados.isSelected();
                exibir();
            });
            cabecalhoSecao.getChildren().add(arquivados);
        }

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

        VBox secao = new VBox(10, cabecalhoSecao, grade);
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

        boolean arquivado = projeto.getStatus() == StatusProjeto.ARQUIVADO;
        Button arquivar = new Button(arquivado ? "restaurar" : "arquivar");
        arquivar.getStyleClass().add("btn-ghost");
        arquivar.setOnMouseClicked(javafx.event.Event::consume);
        arquivar.setOnAction(e -> {
            service.alterarStatusProjeto(projeto.getId(),
                    arquivado ? StatusProjeto.ATIVO : StatusProjeto.ARQUIVADO);
            exibir();
        });
        Button excluir = new Button("excluir");
        excluir.getStyleClass().addAll("btn-ghost", "btn-ghost-danger");
        excluir.setOnMouseClicked(javafx.event.Event::consume);
        excluir.setOnAction(e -> excluirProjeto(projeto));
        HBox linhaAcoes = new HBox(8, arquivar, excluir);
        linhaAcoes.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(10, linhaTopo, badge, barra, linhaMeta, linhaAcoes);
        card.getStyleClass().addAll("card", "project-card");
        if (arquivado) {
            card.setOpacity(0.6);
        }
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

    private void excluirProjeto(Projeto projeto) {
        int etapas = projeto.getEtapas().size();
        long tarefas = projeto.getEtapas().stream().mapToLong(e -> e.getTarefas().size()).sum();
        String mensagem = "Excluir o projeto \"" + projeto.getNome() + "\""
                + (etapas > 0 ? " com " + etapas + " etapas e " + tarefas + " tarefas" : "")
                + "?\nEsta ação não pode ser desfeita. (Para só esconder, use \"arquivar\".)";
        if (Dialogos.confirmarExclusao("Excluir projeto", mensagem)) {
            service.excluirProjeto(projeto.getId());
            exibir();
        }
    }

    private void exportarDados() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar projetos");
        chooser.setInitialFileName("projetos-" + LocalDate.now() + ".json");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo JSON", "*.json"));
        File destino = chooser.showSaveDialog(navigator.getStage());
        if (destino == null) {
            return;
        }
        try {
            service.exportar(destino.toPath());
            Dialogos.informar("Projetos exportados para:\n" + destino);
        } catch (RuntimeException ex) {
            Dialogos.erro("Não foi possível exportar: " + ex.getMessage());
        }
    }

    private void importarDados() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Importar projetos");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo JSON", "*.json"));
        File origem = chooser.showOpenDialog(navigator.getStage());
        if (origem == null) {
            return;
        }
        try {
            int qtd = service.importar(origem.toPath());
            exibir();
            Dialogos.informar(qtd + " projeto(s) importado(s). Projetos com o mesmo id foram atualizados.");
        } catch (RuntimeException ex) {
            Dialogos.erro("Não foi possível importar: " + ex.getMessage());
        }
    }

    private void editarProjeto(Projeto projeto) {
        var valoresAtuais = new ItemFormDialog.Resultado(projeto.getNome(), projeto.getDescricao(), projeto.getDataLimite());
        ItemFormDialog.abrir("Editar projeto", "Nome", valoresAtuais).ifPresent(resultado -> {
            service.editarProjeto(projeto.getId(), resultado.titulo(), resultado.descricao(), resultado.dataLimite());
            exibir();
        });
    }

    private Node secaoPrazos(List<PrazoItem> todosPrazos) {
        Label rotulo = new Label("PRÓXIMOS PRAZOS");
        rotulo.getStyleClass().add("section-label");
        List<PrazoItem> prazos = filtroPrazos.aplicar(todosPrazos, PrazoItem::status, PrazoItem::atrasado,
                PrazoItem::dataLimite);
        HBox cabecalhoSecao = new HBox(16, rotulo, filtroPrazos.barra(false, this::exibir));
        cabecalhoSecao.setAlignment(Pos.CENTER_LEFT);

        VBox lista = new VBox();
        lista.getStyleClass().addAll("card", "item-list");
        if (prazos.isEmpty()) {
            Label vazio = new Label(todosPrazos.isEmpty()
                    ? "Nenhum prazo cadastrado ainda."
                    : "Nenhum prazo com esse filtro.");
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

        return new VBox(10, cabecalhoSecao, lista);
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
            ArmazenamentoConfig.escolherNovaPasta(escolhida.toPath());
            Dialogos.informar("Local alterado para:\n" + escolhida
                    + "\n\nEntre novamente para carregar os dados dessa pasta.");
            navigator.sair();
        } catch (RuntimeException ex) {
            Alert alerta = new Alert(Alert.AlertType.ERROR,
                    "Não foi possível usar essa pasta para salvar os dados: " + ex.getMessage());
            alerta.showAndWait();
        }
    }
}
