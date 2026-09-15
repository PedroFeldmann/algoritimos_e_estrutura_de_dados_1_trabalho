package br.edu.trabalho.gerenciadorprojetos.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Projeto {

    private String id;
    private String nome;
    private String descricao;
    private LocalDate dataCriacao;
    private LocalDate dataLimite;
    private StatusProjeto status;
    private List<Etapa> etapas;

    /** Construtor vazio + setters existem só para desserialização Jackson (ADR-002/ADR-003: domain não importa Jackson). */
    public Projeto() {
        this.etapas = new ArrayList<>();
    }

    public Projeto(String nome, String descricao) {
        this(UUID.randomUUID().toString(), nome, descricao, LocalDate.now(), StatusProjeto.ATIVO, new ArrayList<>());
    }

    public Projeto(String id, String nome, String descricao, LocalDate dataCriacao, StatusProjeto status, List<Etapa> etapas) {
        this.id = Objects.requireNonNull(id);
        this.nome = Objects.requireNonNull(nome);
        this.descricao = descricao;
        this.dataCriacao = dataCriacao;
        this.status = Objects.requireNonNull(status);
        this.etapas = etapas != null ? etapas : new ArrayList<>();
    }

    public void adicionarEtapa(Etapa etapa) {
        etapas.add(Objects.requireNonNull(etapa));
    }

    public void removerEtapa(String etapaId) {
        etapas.removeIf(e -> e.getId().equals(etapaId));
    }

    /**
     * Progresso exibido no Dashboard (badge do cartão de projeto). Derivado das
     * Etapas, não é um campo persistido — mesmo princípio do ADR-004 aplicado a
     * um nível acima.
     */
    public Status calcularProgresso() {
        if (etapas.isEmpty()) {
            return Status.NAO_INICIADA;
        }
        boolean todasConcluidas = etapas.stream().allMatch(e -> e.getStatus() == Status.CONCLUIDA);
        if (todasConcluidas) {
            return Status.CONCLUIDA;
        }
        boolean nenhumaIniciada = etapas.stream().allMatch(e -> e.getStatus() == Status.NAO_INICIADA);
        return nenhumaIniciada ? Status.NAO_INICIADA : Status.EM_ANDAMENTO;
    }

    public long contarEtapasAtrasadas() {
        return etapas.stream().filter(Etapa::estaAtrasada).count();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = Objects.requireNonNull(nome);
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDate getDataLimite() {
        return dataLimite;
    }

    public void setDataLimite(LocalDate dataLimite) {
        this.dataLimite = dataLimite;
    }

    public StatusProjeto getStatus() {
        return status;
    }

    public void setStatus(StatusProjeto status) {
        this.status = Objects.requireNonNull(status);
    }

    public List<Etapa> getEtapas() {
        return etapas;
    }
}
