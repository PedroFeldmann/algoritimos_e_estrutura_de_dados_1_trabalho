package br.edu.trabalho.gerenciadorprojetos.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Nível-folha do domínio (Projeto -> Etapa -> Tarefa). Sem subtarefas — fora de
 * escopo conforme docs/PRD.md, seção 4.2.
 */
public class Tarefa {

    private String id;
    private String titulo;
    private String descricao;
    private LocalDate dataLimite;
    private Status status;

    /** Construtor vazio + setters existem só para desserialização Jackson (ADR-002/ADR-003: domain não importa Jackson). */
    public Tarefa() {
    }

    public Tarefa(String titulo, String descricao, LocalDate dataLimite) {
        this(UUID.randomUUID().toString(), titulo, descricao, dataLimite, Status.NAO_INICIADA);
    }

    public Tarefa(String id, String titulo, String descricao, LocalDate dataLimite, Status status) {
        this.id = Objects.requireNonNull(id);
        this.titulo = Objects.requireNonNull(titulo);
        this.descricao = descricao;
        this.dataLimite = dataLimite;
        this.status = Objects.requireNonNull(status);
    }

    /** ADR-004: "atrasada" é calculado, nunca persistido como Status. */
    public boolean estaAtrasada() {
        return status != Status.CONCLUIDA
                && dataLimite != null
                && dataLimite.isBefore(LocalDate.now());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = Objects.requireNonNull(titulo);
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
}
