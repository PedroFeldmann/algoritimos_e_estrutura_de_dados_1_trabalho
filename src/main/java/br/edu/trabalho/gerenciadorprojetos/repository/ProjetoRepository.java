package br.edu.trabalho.gerenciadorprojetos.repository;

import br.edu.trabalho.gerenciadorprojetos.domain.Projeto;

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
}
