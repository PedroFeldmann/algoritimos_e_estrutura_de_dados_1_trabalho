package br.edu.trabalho.gerenciadorprojetos.service;

import br.edu.trabalho.gerenciadorprojetos.domain.Status;

import java.time.LocalDate;

/**
 * Linha do painel "Próximos prazos" do Dashboard (RF09) — agrega Etapas e
 * Tarefas de todos os projetos num único formato de exibição.
 */
public record PrazoItem(
        String descricao,
        String origem,
        LocalDate dataLimite,
        Status status,
        boolean atrasado
) {
}
