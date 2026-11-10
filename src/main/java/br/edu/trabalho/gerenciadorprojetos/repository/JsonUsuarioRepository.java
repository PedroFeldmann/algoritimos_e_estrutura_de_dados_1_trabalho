package br.edu.trabalho.gerenciadorprojetos.repository;

import br.edu.trabalho.gerenciadorprojetos.domain.Usuario;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** Lista de contas em {@code usuarios.json}, na mesma pasta dos dados (RF10). Arquivo pequeno: lido a cada consulta. */
public class JsonUsuarioRepository {

    private final Path arquivo;
    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public JsonUsuarioRepository(Path arquivo) {
        this.arquivo = arquivo;
    }

    public List<Usuario> listarTodos() {
        if (!Files.exists(arquivo)) {
            return new ArrayList<>();
        }
        try {
            if (Files.size(arquivo) == 0) {
                return new ArrayList<>();
            }
            return new ArrayList<>(List.of(mapper.readValue(arquivo.toFile(), Usuario[].class)));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler " + arquivo, e);
        }
    }

    public void adicionar(Usuario usuario) {
        List<Usuario> todos = listarTodos();
        todos.add(usuario);
        try {
            Files.createDirectories(arquivo.getParent());
            Path temporario = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");
            mapper.writeValue(temporario.toFile(), todos);
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
