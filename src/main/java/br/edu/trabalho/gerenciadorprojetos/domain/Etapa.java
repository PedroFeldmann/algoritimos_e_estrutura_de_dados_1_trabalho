package br.edu.trabalho.gerenciadorprojetos.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Etapa {

    private String id;
    private String nome;
    private String descricao;
    private LocalDate dataLimite;
    private Status status;
    private List<Tarefa> tarefas;

    /** Construtor vazio + setters existem só para desserialização Jackson (ADR-002/ADR-003: domain não importa Jackson). */
    public Etapa() {
        this.tarefas = new ArrayList<>();
    }

    public Etapa(String nome, String descricao, LocalDate dataLimite) {
        this(UUID.randomUUID().toString(), nome, descricao, dataLimite, Status.NAO_INICIADA, new ArrayList<>());
    }

    public Etapa(String id, String nome, String descricao, LocalDate dataLimite, Status status, List<Tarefa> tarefas) {
        this.id = Objects.requireNonNull(id);
        this.nome = Objects.requireNonNull(nome);
        this.descricao = descricao;
        this.dataLimite = dataLimite;
        this.status = Objects.requireNonNull(status);
        this.tarefas = tarefas != null ? tarefas : new ArrayList<>();
    }

    /** ADR-004: "atrasada" é calculado, nunca persistido como Status. */
    public boolean estaAtrasada() {
        return status != Status.CONCLUIDA
                && dataLimite != null
                && dataLimite.isBefore(LocalDate.now());
    }

    public void adicionarTarefa(Tarefa tarefa) {
        tarefas.add(Objects.requireNonNull(tarefa));
    }

    public void removerTarefa(String tarefaId) {
        tarefas.removeIf(t -> t.getId().equals(tarefaId));
    }

    public long contarTarefasAtrasadas() {
        return tarefas.stream().filter(Tarefa::estaAtrasada).count();
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

    public LocalDate getDataLimite() {
        return dataLimite;
    }

    public void setDataLimite(LocalDate dataLimite) {
        this.dataLimite = dataLimite;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = Objects.requireNonNull(status);
    }

    public List<Tarefa> getTarefas() {
        return tarefas;
    }
}
