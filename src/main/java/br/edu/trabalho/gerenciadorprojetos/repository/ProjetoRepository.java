package br.edu.trabalho.gerenciadorprojetos.repository;

import br.edu.trabalho.gerenciadorprojetos.domain.Projeto;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Só esta camada conhece o formato de armazenamento (ADR-003). O domínio e o
 * service dependem apenas desta interface.
 */
public interface ProjetoRepository {

    List<Projeto> listarTodos();

    Optional<Projeto> buscarPorId(String id);

    void salvar(Projeto projeto);

    void excluir(String id);

    /** Grava uma cópia de todos os projetos em {@code destino} (backup / transferência). */
    void exportarPara(Path destino);

    /**
     * Lê projetos de um arquivo exportado e os incorpora: projetos com o mesmo id
     * são substituídos, os demais são adicionados. Devolve quantos foram lidos.
     *
     * @throws IllegalArgumentException se o arquivo não for um export válido
     */
    int importarDe(Path origem);
}
