package br.edu.trabalho.gerenciadorprojetos.repository;

import br.edu.trabalho.gerenciadorprojetos.domain.Etapa;
import br.edu.trabalho.gerenciadorprojetos.domain.Projeto;
import br.edu.trabalho.gerenciadorprojetos.domain.Status;
import br.edu.trabalho.gerenciadorprojetos.domain.Tarefa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF08 / critério de aceite: dados sobrevivem ao fechar e reabrir o app. */
class JsonProjetoRepositoryTest {

    @TempDir
    Path pasta;

    @Test
    void dadosGravadosSaoRecarregadosPorOutraInstancia() {
        Path arquivo = pasta.resolve("dados.json");
        JsonProjetoRepository repo = new JsonProjetoRepository(arquivo);

        Projeto projeto = new Projeto("TCC", "Trabalho final");
        projeto.setDataLimite(LocalDate.of(2030, 1, 31));
        Etapa etapa = new Etapa("Pesquisa", "", LocalDate.of(2030, 1, 10));
        Tarefa tarefa = new Tarefa("Ler artigos", "5 artigos", LocalDate.of(2030, 1, 5));
        tarefa.setStatus(Status.EM_ANDAMENTO);
        etapa.adicionarTarefa(tarefa);
        projeto.adicionarEtapa(etapa);
        repo.salvar(projeto);

        JsonProjetoRepository reaberto = new JsonProjetoRepository(arquivo);
        Projeto lido = reaberto.buscarPorId(projeto.getId()).orElseThrow();
        assertEquals("TCC", lido.getNome());
        assertEquals(LocalDate.of(2030, 1, 31), lido.getDataLimite());
        Tarefa tarefaLida = lido.getEtapas().get(0).getTarefas().get(0);
        assertEquals("Ler artigos", tarefaLida.getTitulo());
        assertEquals(Status.EM_ANDAMENTO, tarefaLida.getStatus());
    }

    @Test
    void excluirRemoveTambemDoArquivo() {
        Path arquivo = pasta.resolve("dados.json");
        JsonProjetoRepository repo = new JsonProjetoRepository(arquivo);
        Projeto projeto = new Projeto("Temporário", "");
        repo.salvar(projeto);
        repo.excluir(projeto.getId());

        assertTrue(new JsonProjetoRepository(arquivo).listarTodos().isEmpty());
    }

    @Test
    void salvarDeNovoProjetoExistenteNaoDuplica() {
        JsonProjetoRepository repo = new JsonProjetoRepository(pasta.resolve("dados.json"));
        Projeto projeto = new Projeto("A", "");
        repo.salvar(projeto);
        projeto.setNome("B");
        repo.salvar(projeto);

        assertEquals(1, repo.listarTodos().size());
        assertEquals("B", repo.listarTodos().get(0).getNome());
    }

    @Test
    void arquivoCorrompidoNaoDerrubaOAppEFicaPreservado() throws IOException {
        Path arquivo = pasta.resolve("dados.json");
        Files.writeString(arquivo, "{ isto não é json válido");

        JsonProjetoRepository repo = new JsonProjetoRepository(arquivo);

        assertTrue(repo.listarTodos().isEmpty());
        try (var arquivos = Files.list(pasta)) {
            assertTrue(arquivos.anyMatch(p -> p.getFileName().toString().contains(".corrompido-")),
                    "o arquivo problemático deve ser preservado, não apagado");
        }
    }

    @Test
    void arquivoCorrompidoERestauradoDoBackup() throws IOException {
        Path arquivo = pasta.resolve("dados.json");
        JsonProjetoRepository repo = new JsonProjetoRepository(arquivo);
        repo.salvar(new Projeto("Primeiro", ""));
        repo.salvar(new Projeto("Segundo", "")); // gera o .bak com o estado anterior

        Files.writeString(arquivo, "lixo");
        JsonProjetoRepository reaberto = new JsonProjetoRepository(arquivo);

        assertEquals(1, reaberto.listarTodos().size());
        assertEquals("Primeiro", reaberto.listarTodos().get(0).getNome());
    }

    @Test
    void arquivoVazioEquivaleASemDados() throws IOException {
        Path arquivo = pasta.resolve("dados.json");
        Files.writeString(arquivo, "");
        assertTrue(new JsonProjetoRepository(arquivo).listarTodos().isEmpty());
    }

    @Test
    void camposDesconhecidosNoJsonSaoIgnorados() throws IOException {
        Path arquivo = pasta.resolve("dados.json");
        Files.writeString(arquivo,
                "[{\"id\":\"1\",\"nome\":\"X\",\"status\":\"ATIVO\",\"campoNovo\":42,\"etapas\":[]}]");
        assertEquals("X", new JsonProjetoRepository(arquivo).listarTodos().get(0).getNome());
    }
}
