package br.edu.trabalho.gerenciadorprojetos.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Cobre a regra do ADR-004: "atrasada" é calculado a partir de dataLimite + status. */
class TarefaTest {

    @Test
    void tarefaComPrazoNoPassadoENaoConcluidaEstaAtrasada() {
        Tarefa tarefa = new Tarefa("Escrever introdução", "", LocalDate.now().minusDays(1));
        assertTrue(tarefa.estaAtrasada());
    }

    @Test
    void tarefaConcluidaNuncaEstaAtrasadaMesmoComPrazoVencido() {
        Tarefa tarefa = new Tarefa("Escrever introdução", "", LocalDate.now().minusDays(1));
        tarefa.setStatus(Status.CONCLUIDA);
        assertFalse(tarefa.estaAtrasada());
    }

    @Test
    void tarefaComPrazoFuturoNaoEstaAtrasada() {
        Tarefa tarefa = new Tarefa("Escrever introdução", "", LocalDate.now().plusDays(5));
        assertFalse(tarefa.estaAtrasada());
    }

    @Test
    void tarefaSemDataLimiteNuncaEstaAtrasada() {
        Tarefa tarefa = new Tarefa("Escrever introdução", "", null);
        assertFalse(tarefa.estaAtrasada());
    }
}
