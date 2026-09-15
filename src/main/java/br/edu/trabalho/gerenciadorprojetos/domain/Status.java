package br.edu.trabalho.gerenciadorprojetos.domain;

/**
 * Status manual de uma Etapa ou Tarefa.
 * "Atrasada" não existe aqui de propósito — ver ADR-004-status-derivado.md.
 */
public enum Status {
    NAO_INICIADA("Não iniciada"),
    EM_ANDAMENTO("Em andamento"),
    CONCLUIDA("Concluída");

    private final String rotulo;

    Status(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
