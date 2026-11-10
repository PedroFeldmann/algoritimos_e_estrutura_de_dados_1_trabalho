package br.edu.trabalho.gerenciadorprojetos.repository;

import br.edu.trabalho.gerenciadorprojetos.domain.Projeto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementação em arquivo JSON local — ver ADR-002-persistencia-json.md.
 * Carrega tudo em memória na construção e regrava o arquivo inteiro a cada
 * alteração; adequado ao volume de dados de um app single-user (ADR-002).
 */
public class JsonProjetoRepository implements ProjetoRepository {

    private final Path arquivo;
    private final ObjectMapper mapper;
    private List<Projeto> cache;

    public JsonProjetoRepository() {
        this(Path.of(System.getProperty("user.home"), ".gerenciador-projetos", "dados.json"));
    }

    public JsonProjetoRepository(Path arquivo) {
        this.arquivo = arquivo;
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(SerializationFeature.INDENT_OUTPUT);
        this.cache = carregarDoDisco();
    }

    @Override
    public List<Projeto> listarTodos() {
        return List.copyOf(cache);
    }

    @Override
    public Optional<Projeto> buscarPorId(String id) {
        return cache.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    @Override
    public void salvar(Projeto projeto) {
        cache.removeIf(p -> p.getId().equals(projeto.getId()));
        cache.add(projeto);
        persistir();
    }

    @Override
    public void excluir(String id) {
        cache.removeIf(p -> p.getId().equals(id));
        persistir();
    }

    @Override
    public void exportarPara(Path destino) {
        try {
            mapper.writeValue(destino.toFile(), cache);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao exportar para " + destino, e);
        }
    }

    @Override
    public int importarDe(Path origem) {
        List<Projeto> lidos;
        try {
            lidos = ler(origem);
        } catch (IOException | RuntimeException e) {
            throw new IllegalArgumentException("O arquivo não é um export válido do Gerenciador de Projetos.", e);
        }
        for (Projeto p : lidos) {
            if (p.getId() == null || p.getNome() == null || p.getStatus() == null) {
                throw new IllegalArgumentException("O arquivo contém projetos incompletos (sem id, nome ou status).");
            }
        }
        for (Projeto p : lidos) {
            cache.removeIf(existente -> existente.getId().equals(p.getId()));
            cache.add(p);
        }
        persistir();
        return lidos.size();
    }

    private Path backup() {
        return arquivo.resolveSibling(arquivo.getFileName() + ".bak");
    }

    /**
     * Lê o arquivo de dados. Se estiver ilegível (corrompido), preserva-o com o
     * sufixo ".corrompido-<timestamp>" e tenta restaurar do backup (.bak) em vez
     * de derrubar o app ou sobrescrever o arquivo problemático.
     */
    private List<Projeto> carregarDoDisco() {
        if (!Files.exists(arquivo)) {
            return new ArrayList<>();
        }
        try {
            return ler(arquivo);
        } catch (IOException e) {
            System.err.println("Arquivo de dados ilegível (" + arquivo + "): " + e.getMessage());
            preservarCorrompido();
            if (Files.exists(backup())) {
                try {
                    List<Projeto> restaurado = ler(backup());
                    System.err.println("Dados restaurados do backup " + backup());
                    return restaurado;
                } catch (IOException ignorado) {
                    System.err.println("Backup também ilegível: " + ignorado.getMessage());
                }
            }
            return new ArrayList<>();
        }
    }

    private List<Projeto> ler(Path origem) throws IOException {
        if (Files.size(origem) == 0) {
            return new ArrayList<>();
        }
        Projeto[] projetos = mapper.readValue(origem.toFile(), Projeto[].class);
        List<Projeto> lista = new ArrayList<>();
        for (Projeto p : projetos) {
            lista.add(p);
        }
        return lista;
    }

    private void preservarCorrompido() {
        try {
            Path destino = arquivo.resolveSibling(
                    arquivo.getFileName() + ".corrompido-" + System.currentTimeMillis());
            Files.move(arquivo, destino, StandardCopyOption.REPLACE_EXISTING);
            System.err.println("Arquivo original preservado em " + destino);
        } catch (IOException e) {
            System.err.println("Não foi possível preservar o arquivo corrompido: " + e.getMessage());
        }
    }

    /**
     * Gravação segura: escreve num arquivo temporário e só então substitui o
     * definitivo (atômico quando o sistema de arquivos permite), guardando a
     * versão anterior em ".bak". Uma queda no meio da gravação nunca deixa o
     * arquivo de dados pela metade.
     */
    private void persistir() {
        try {
            Files.createDirectories(arquivo.getParent());
            Path temporario = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");
            mapper.writeValue(temporario.toFile(), cache);
            if (Files.exists(arquivo)) {
                Files.copy(arquivo, backup(), StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar " + arquivo, e);
        }
    }
}
