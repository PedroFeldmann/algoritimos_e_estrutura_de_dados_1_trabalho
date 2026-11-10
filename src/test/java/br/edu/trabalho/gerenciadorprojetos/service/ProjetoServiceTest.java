package br.edu.trabalho.gerenciadorprojetos.service;

import br.edu.trabalho.gerenciadorprojetos.domain.Etapa;
import br.edu.trabalho.gerenciadorprojetos.domain.Projeto;
import br.edu.trabalho.gerenciadorprojetos.domain.Status;
import br.edu.trabalho.gerenciadorprojetos.domain.StatusProjeto;
import br.edu.trabalho.gerenciadorprojetos.repository.JsonProjetoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjetoServiceTest {

    @TempDir
    Path pasta;

    ProjetoService service;

    @BeforeEach
    void preparar() {
        service = new ProjetoService(new JsonProjetoRepository(pasta.resolve("dados.json")));
    }

    @Test
    void criaProjetoEtapaETarefaEmCadeia() {
        Projeto p = service.criarProjeto("Site", "Reformulação");
        Etapa e = service.criarEtapa(p.getId(), "Design", "", LocalDate.now().plusDays(10));
        service.criarTarefa(p.getId(), e.getId(), "Wireframes", "", LocalDate.now().plusDays(3));

        Projeto lido = service.buscarProjeto(p.getId());
        assertEquals(1, lido.getEtapas().size());
        assertEquals("Wireframes", lido.getEtapas().get(0).getTarefas().get(0).getTitulo());
    }

    @Test
    void excluirEtapaRemoveSuasTarefasEmCascata() {
        Projeto p = service.criarProjeto("P", "");
        Etapa e = service.criarEtapa(p.getId(), "E", "", null);
        service.criarTarefa(p.getId(), e.getId(), "T", "", null);

        service.excluirEtapa(p.getId(), e.getId());

        assertTrue(service.buscarProjeto(p.getId()).getEtapas().isEmpty());
    }

    @Test
    void excluirProjetoRemoveTudo() {
        Projeto p = service.criarProjeto("P", "");
        Etapa e = service.criarEtapa(p.getId(), "E", "", null);
        service.criarTarefa(p.getId(), e.getId(), "T", "", null);

        service.excluirProjeto(p.getId());

        assertTrue(service.listarProjetos().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> service.buscarProjeto(p.getId()));
    }

    @Test
    void editarAtualizaCamposEPreservaIdEDataCriacao() {
        Projeto p = service.criarProjeto("Velho", "d");
        LocalDate criacao = p.getDataCriacao();
        service.editarProjeto(p.getId(), "  Novo  ", "d2", LocalDate.of(2031, 5, 1));

        Projeto lido = service.buscarProjeto(p.getId());
        assertEquals("Novo", lido.getNome());
        assertEquals(LocalDate.of(2031, 5, 1), lido.getDataLimite());
        assertEquals(criacao, lido.getDataCriacao());
    }

    @Test
    void rejeitaNomesEmBranco() {
        assertThrows(IllegalArgumentException.class, () -> service.criarProjeto("   ", ""));
        Projeto p = service.criarProjeto("P", "");
        assertThrows(IllegalArgumentException.class, () -> service.criarEtapa(p.getId(), "", "", null));
        Etapa e = service.criarEtapa(p.getId(), "E", "", null);
        assertThrows(IllegalArgumentException.class,
                () -> service.criarTarefa(p.getId(), e.getId(), null, "", null));
        assertTrue(service.listarProjetos().get(0).getEtapas().get(0).getTarefas().isEmpty());
    }

    @Test
    void alterarStatusDaEtapaPersiste() {
        Projeto p = service.criarProjeto("P", "");
        Etapa e = service.criarEtapa(p.getId(), "E", "", null);
        service.alterarStatusEtapa(p.getId(), e.getId(), Status.CONCLUIDA);

        assertEquals(Status.CONCLUIDA, service.buscarProjeto(p.getId()).getEtapas().get(0).getStatus());
        assertEquals(Status.CONCLUIDA, service.buscarProjeto(p.getId()).calcularProgresso());
    }

    @Test
    void proximosPrazosVemOrdenadosComSemDataPorUltimo() {
        Projeto p = service.criarProjeto("P", "");
        Etapa e = service.criarEtapa(p.getId(), "Sem data", "", null);
        service.criarTarefa(p.getId(), e.getId(), "Tarde", "", LocalDate.now().plusDays(20));
        service.criarTarefa(p.getId(), e.getId(), "Cedo", "", LocalDate.now().plusDays(2));
        service.criarTarefa(p.getId(), e.getId(), "Atrasada", "", LocalDate.now().minusDays(4));

        List<PrazoItem> prazos = service.listarProximosPrazos();

        assertEquals(List.of("Atrasada", "Cedo", "Tarde", "Sem data"),
                prazos.stream().map(PrazoItem::descricao).toList());
        assertTrue(prazos.get(0).atrasado());
        assertFalse(prazos.get(1).atrasado());
    }

    @Test
    void contaEtapasAtrasadasPorProjeto() {
        Projeto p = service.criarProjeto("P", "");
        service.criarEtapa(p.getId(), "Velha", "", LocalDate.now().minusDays(1));
        service.criarEtapa(p.getId(), "Futura", "", LocalDate.now().plusDays(1));

        assertEquals(1, service.buscarProjeto(p.getId()).contarEtapasAtrasadas());
    }

    @Test
    void alterarStatusDaTarefaPersiste() {
        Projeto p = service.criarProjeto("P", "");
        Etapa e = service.criarEtapa(p.getId(), "E", "", null);
        var t = service.criarTarefa(p.getId(), e.getId(), "T", "", null);
        service.alterarStatusTarefa(p.getId(), e.getId(), t.getId(), Status.EM_ANDAMENTO);

        assertEquals(Status.EM_ANDAMENTO,
                service.buscarProjeto(p.getId()).getEtapas().get(0).getTarefas().get(0).getStatus());
    }

    @Test
    void arquivarEscondeOProjetoDosPrazosESeRestauraVolta() {
        Projeto p = service.criarProjeto("P", "");
        Etapa e = service.criarEtapa(p.getId(), "E", "", LocalDate.now().plusDays(1));
        service.criarTarefa(p.getId(), e.getId(), "T", "", LocalDate.now().plusDays(1));
        assertEquals(2, service.listarProximosPrazos().size());

        service.alterarStatusProjeto(p.getId(), StatusProjeto.ARQUIVADO);
        assertTrue(service.listarProximosPrazos().isEmpty());
        assertEquals(1, service.listarProjetos().size(), "arquivar não apaga nada");

        service.alterarStatusProjeto(p.getId(), StatusProjeto.ATIVO);
        assertEquals(2, service.listarProximosPrazos().size());
    }

    @Test
    void moverEtapaETarefaTrocaPosicaoEPersiste() {
        Projeto p = service.criarProjeto("P", "");
        Etapa a = service.criarEtapa(p.getId(), "A", "", null);
        service.criarEtapa(p.getId(), "B", "", null);
        service.criarEtapa(p.getId(), "C", "", null);

        service.moverEtapa(p.getId(), a.getId(), 1);
        assertEquals(List.of("B", "A", "C"),
                service.buscarProjeto(p.getId()).getEtapas().stream().map(Etapa::getNome).toList());

        service.moverEtapa(p.getId(), a.getId(), 5);   // fora dos limites: ignora
        service.moverEtapa(p.getId(), a.getId(), -1);
        assertEquals(List.of("A", "B", "C"),
                service.buscarProjeto(p.getId()).getEtapas().stream().map(Etapa::getNome).toList());

        var t1 = service.criarTarefa(p.getId(), a.getId(), "t1", "", null);
        service.criarTarefa(p.getId(), a.getId(), "t2", "", null);
        service.moverTarefa(p.getId(), a.getId(), t1.getId(), 1);
        assertEquals("t2", service.buscarProjeto(p.getId()).getEtapas().get(0).getTarefas().get(0).getTitulo());
    }

    @Test
    void exportarEImportarEntreArmazenamentosDiferentes() {
        Projeto p = service.criarProjeto("Levar comigo", "");
        Etapa e = service.criarEtapa(p.getId(), "E", "", null);
        service.criarTarefa(p.getId(), e.getId(), "T", "", null);
        Path export = pasta.resolve("backup.json");
        service.exportar(export);

        ProjetoService outro = new ProjetoService(new JsonProjetoRepository(pasta.resolve("outro.json")));
        assertEquals(1, outro.importar(export));
        assertEquals("T", outro.listarProjetos().get(0).getEtapas().get(0).getTarefas().get(0).getTitulo());

        // importar de novo atualiza em vez de duplicar
        assertEquals(1, outro.importar(export));
        assertEquals(1, outro.listarProjetos().size());
    }

    @Test
    void importarArquivoInvalidoLancaErroSemMexerNosDados() throws java.io.IOException {
        service.criarProjeto("Existente", "");
        Path lixo = pasta.resolve("lixo.json");
        java.nio.file.Files.writeString(lixo, "isto não é json");
        Path incompleto = pasta.resolve("incompleto.json");
        java.nio.file.Files.writeString(incompleto, "[{\"descricao\":\"sem id\"}]");

        assertThrows(IllegalArgumentException.class, () -> service.importar(lixo));
        assertThrows(IllegalArgumentException.class, () -> service.importar(incompleto));
        assertEquals(1, service.listarProjetos().size());
    }
}
