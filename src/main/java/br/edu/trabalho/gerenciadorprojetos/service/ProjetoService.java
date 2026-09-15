package br.edu.trabalho.gerenciadorprojetos.service;

import br.edu.trabalho.gerenciadorprojetos.domain.Etapa;
import br.edu.trabalho.gerenciadorprojetos.domain.Projeto;
import br.edu.trabalho.gerenciadorprojetos.domain.Tarefa;
import br.edu.trabalho.gerenciadorprojetos.repository.ProjetoRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Orquestra domínio + repositório (ADR-003). A UI só conversa com esta classe,
 * nunca diretamente com ProjetoRepository.
 */
public class ProjetoService {

    private final ProjetoRepository repository;

    public ProjetoService(ProjetoRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public List<Projeto> listarProjetos() {
        return repository.listarTodos();
    }

    public Projeto buscarProjeto(String id) {
        return repository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Projeto não encontrado: " + id));
    }

    public Projeto criarProjeto(String nome, String descricao) {
        Projeto projeto = new Projeto(nome, descricao);
        repository.salvar(projeto);
        return projeto;
    }

    /** Exclusão em cascata é implícita: Etapas/Tarefas vivem dentro do Projeto (docs/PRD.md, 5.2). */
    public void excluirProjeto(String id) {
        repository.excluir(id);
    }

    /** Nome, descrição e dataLimite são editáveis; id e dataCriacao não (docs/PRD.md, 5.1). */
    public void editarProjeto(String id, String nome, String descricao, java.time.LocalDate dataLimite) {
        Projeto projeto = buscarProjeto(id);
        projeto.setNome(nome);
        projeto.setDescricao(descricao);
        projeto.setDataLimite(dataLimite);
        repository.salvar(projeto);
    }

    public Etapa criarEtapa(String projetoId, String nome, String descricao, java.time.LocalDate dataLimite) {
        Projeto projeto = buscarProjeto(projetoId);
        Etapa etapa = new Etapa(nome, descricao, dataLimite);
        projeto.adicionarEtapa(etapa);
        repository.salvar(projeto);
        return etapa;
    }

    public void excluirEtapa(String projetoId, String etapaId) {
        Projeto projeto = buscarProjeto(projetoId);
        projeto.removerEtapa(etapaId);
        repository.salvar(projeto);
    }

    public void editarEtapa(String projetoId, String etapaId, String nome, String descricao, java.time.LocalDate dataLimite) {
        Projeto projeto = buscarProjeto(projetoId);
        Etapa etapa = buscarEtapa(projeto, etapaId);
        etapa.setNome(nome);
        etapa.setDescricao(descricao);
        etapa.setDataLimite(dataLimite);
        repository.salvar(projeto);
    }

    public void alterarStatusEtapa(String projetoId, String etapaId, br.edu.trabalho.gerenciadorprojetos.domain.Status status) {
        Projeto projeto = buscarProjeto(projetoId);
        Etapa etapa = buscarEtapa(projeto, etapaId);
        etapa.setStatus(status);
        repository.salvar(projeto);
    }

    public Tarefa criarTarefa(String projetoId, String etapaId, String titulo, String descricao, java.time.LocalDate dataLimite) {
        Projeto projeto = buscarProjeto(projetoId);
        Etapa etapa = buscarEtapa(projeto, etapaId);
        Tarefa tarefa = new Tarefa(titulo, descricao, dataLimite);
        etapa.adicionarTarefa(tarefa);
        repository.salvar(projeto);
        return tarefa;
    }

    public void excluirTarefa(String projetoId, String etapaId, String tarefaId) {
        Projeto projeto = buscarProjeto(projetoId);
        Etapa etapa = buscarEtapa(projeto, etapaId);
        etapa.removerTarefa(tarefaId);
        repository.salvar(projeto);
    }

    public void editarTarefa(String projetoId, String etapaId, String tarefaId, String titulo, String descricao, java.time.LocalDate dataLimite) {
        Projeto projeto = buscarProjeto(projetoId);
        Etapa etapa = buscarEtapa(projeto, etapaId);
        Tarefa tarefa = buscarTarefa(etapa, tarefaId);
        tarefa.setTitulo(titulo);
        tarefa.setDescricao(descricao);
        tarefa.setDataLimite(dataLimite);
        repository.salvar(projeto);
    }

    private Tarefa buscarTarefa(Etapa etapa, String tarefaId) {
        return etapa.getTarefas().stream()
                .filter(t -> t.getId().equals(tarefaId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada: " + tarefaId));
    }

    public void salvarProjeto(Projeto projeto) {
        repository.salvar(projeto);
    }

    private Etapa buscarEtapa(Projeto projeto, String etapaId) {
        return projeto.getEtapas().stream()
                .filter(e -> e.getId().equals(etapaId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Etapa não encontrada: " + etapaId));
    }

    /**
     * Painel "Próximos prazos" do Dashboard (RF09): agrega Etapas e Tarefas de
     * todos os projetos, ordenadas por data limite (itens sem data ficam por
     * último).
     */
    public List<PrazoItem> listarProximosPrazos() {
        return repository.listarTodos().stream()
                .flatMap(projeto -> projeto.getEtapas().stream().flatMap(etapa -> {
                    var itemEtapa = new PrazoItem(
                            etapa.getNome(),
                            projeto.getNome() + ", etapa",
                            etapa.getDataLimite(),
                            etapa.getStatus(),
                            etapa.estaAtrasada());
                    var itensTarefas = etapa.getTarefas().stream().map(tarefa -> new PrazoItem(
                            tarefa.getTitulo(),
                            projeto.getNome() + ", etapa \"" + etapa.getNome() + "\"",
                            tarefa.getDataLimite(),
                            tarefa.getStatus(),
                            tarefa.estaAtrasada()));
                    return java.util.stream.Stream.concat(java.util.stream.Stream.of(itemEtapa), itensTarefas);
                }))
                .sorted(Comparator.comparing(PrazoItem::dataLimite, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }
}
