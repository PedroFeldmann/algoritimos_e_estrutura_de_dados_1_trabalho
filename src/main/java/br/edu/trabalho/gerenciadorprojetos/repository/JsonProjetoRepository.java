package br.edu.trabalho.gerenciadorprojetos.repository;

import br.edu.trabalho.gerenciadorprojetos.domain.Projeto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private List<Projeto> carregarDoDisco() {
        if (!Files.exists(arquivo)) {
            return new ArrayList<>();
        }
        try {
            Projeto[] projetos = mapper.readValue(arquivo.toFile(), Projeto[].class);
            List<Projeto> lista = new ArrayList<>();
            for (Projeto p : projetos) {
                lista.add(p);
            }
            return lista;
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler " + arquivo, e);
        }
    }

    private void persistir() {
        try {
            Files.createDirectories(arquivo.getParent());
            mapper.writeValue(arquivo.toFile(), cache);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar " + arquivo, e);
        }
    }
}
