package br.edu.trabalho.gerenciadorprojetos.service;

import br.edu.trabalho.gerenciadorprojetos.domain.Status;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificadorPrazosTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 10);

    private static PrazoItem item(String nome, LocalDate data, Status status, boolean atrasado) {
        return new PrazoItem(nome, "Projeto", data, status, atrasado);
    }

    @Test
    void avisaAtrasadosEOsQueVencemDentroDaJanela() {
        var atrasado = item("atrasado", HOJE.minusDays(3), Status.NAO_INICIADA, true);
        var hoje = item("hoje", HOJE, Status.EM_ANDAMENTO, false);
        var noLimite = item("limite", HOJE.plusDays(NotificadorPrazos.DIAS_ANTECEDENCIA), Status.NAO_INICIADA, false);
        var longe = item("longe", HOJE.plusDays(NotificadorPrazos.DIAS_ANTECEDENCIA + 1), Status.NAO_INICIADA, false);

        var pendentes = NotificadorPrazos.pendentesDeAviso(List.of(atrasado, hoje, noLimite, longe), HOJE);

        assertEquals(List.of(atrasado, hoje, noLimite), pendentes);
    }

    @Test
    void ignoraConcluidosESemData() {
        var concluido = item("concluído", HOJE, Status.CONCLUIDA, false);
        var semData = item("sem data", null, Status.NAO_INICIADA, false);

        assertTrue(NotificadorPrazos.pendentesDeAviso(List.of(concluido, semData), HOJE).isEmpty());
    }

    @Test
    void resumoDescreveAtrasadosEProximos() {
        var a1 = item("a", HOJE.minusDays(1), Status.NAO_INICIADA, true);
        var a2 = item("b", HOJE.minusDays(2), Status.NAO_INICIADA, true);
        var p = item("c", HOJE, Status.NAO_INICIADA, false);

        assertEquals("2 atrasados · 1 vence nos próximos 2 dias",
                NotificadorPrazos.resumo(List.of(a1, a2, p)));
        assertEquals("1 atrasado", NotificadorPrazos.resumo(List.of(a1)));
        assertEquals("", NotificadorPrazos.resumo(List.of()));
    }

    @Test
    void chaveDistingueItensDeProjetosEDatasDiferentes() {
        var um = new PrazoItem("T", "P1", HOJE, Status.NAO_INICIADA, false);
        var outroProjeto = new PrazoItem("T", "P2", HOJE, Status.NAO_INICIADA, false);
        var outraData = new PrazoItem("T", "P1", HOJE.plusDays(1), Status.NAO_INICIADA, false);

        assertEquals(3, java.util.Set.of(NotificadorPrazos.chave(um),
                NotificadorPrazos.chave(outroProjeto), NotificadorPrazos.chave(outraData)).size());
    }
}
