package br.edu.trabalho.gerenciadorprojetos.domain;

/**
 * Estado de ciclo de vida do Projeto (docs/PRD.md, seção 5.1) — diferente do
 * progresso exibido no Dashboard, que é derivado das Etapas (ver Projeto#calcularProgresso).
 */
public enum StatusProjeto {
    ATIVO,
    ARQUIVADO
}
