package br.edu.trabalho.gerenciadorprojetos.service;

import br.edu.trabalho.gerenciadorprojetos.domain.Usuario;
import br.edu.trabalho.gerenciadorprojetos.repository.JsonProjetoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioServiceTest {

    @TempDir
    Path pasta;

    UsuarioService usuarios;

    @BeforeEach
    void preparar() {
        usuarios = new UsuarioService(pasta);
    }

    @Test
    void cadastraEAutenticaComASenhaCerta() {
        usuarios.cadastrar("Maria", "Maria Silva", "segredo123");

        Usuario u = usuarios.autenticar("maria", "segredo123");
        assertEquals("maria", u.getLogin());
        assertEquals("Maria Silva", u.getNome());
    }

    @Test
    void senhaNuncaEhGravadaEmTextoPuro() throws IOException {
        usuarios.cadastrar("maria", "Maria", "segredo123");

        String arquivo = Files.readString(pasta.resolve("usuarios.json"));
        assertFalse(arquivo.contains("segredo123"));
        assertTrue(arquivo.contains("\"hash\""));
        assertTrue(arquivo.contains("\"salt\""));
    }

    @Test
    void mesmaSenhaEmContasDiferentesGeraHashesDiferentes() {
        Usuario a = usuarios.cadastrar("ana", "Ana", "mesmaSenha1");
        Usuario b = usuarios.cadastrar("bia", "Bia", "mesmaSenha1");

        assertNotEquals(a.getHash(), b.getHash());
        assertNotEquals(a.getSalt(), b.getSalt());
    }

    @Test
    void senhaErradaOuLoginInexistenteDaMesmaMensagem() {
        usuarios.cadastrar("maria", "Maria", "segredo123");

        var erroSenha = assertThrows(IllegalArgumentException.class, () -> usuarios.autenticar("maria", "errada!!"));
        var erroLogin = assertThrows(IllegalArgumentException.class, () -> usuarios.autenticar("ninguem", "segredo123"));
        assertEquals(erroSenha.getMessage(), erroLogin.getMessage());
    }

    @Test
    void rejeitaLoginDuplicadoLoginInvalidoESenhaCurta() {
        usuarios.cadastrar("maria", "Maria", "segredo123");

        assertThrows(IllegalArgumentException.class, () -> usuarios.cadastrar("MARIA", "Outra", "segredo123"));
        assertThrows(IllegalArgumentException.class, () -> usuarios.cadastrar("../etc", "X", "segredo123"));
        assertThrows(IllegalArgumentException.class, () -> usuarios.cadastrar("ab", "X", "segredo123"));
        assertThrows(IllegalArgumentException.class, () -> usuarios.cadastrar("joao", "João", "123"));
        assertThrows(IllegalArgumentException.class, () -> usuarios.cadastrar("joao", " ", "segredo123"));
    }

    @Test
    void usuariosTemDadosIsoladosUmDoOutro() {
        usuarios.cadastrar("ana", "Ana", "senhaAna1");
        usuarios.cadastrar("bia", "Bia", "senhaBia1");

        ProjetoService daAna = new ProjetoService(new JsonProjetoRepository(usuarios.arquivoDeDados("ana")));
        ProjetoService daBia = new ProjetoService(new JsonProjetoRepository(usuarios.arquivoDeDados("bia")));
        daAna.criarProjeto("Projeto da Ana", "");

        assertEquals(1, daAna.listarProjetos().size());
        assertTrue(daBia.listarProjetos().isEmpty());
        // e continua assim depois de reabrir
        assertEquals(1, new ProjetoService(new JsonProjetoRepository(usuarios.arquivoDeDados("ana")))
                .listarProjetos().size());
    }

    @Test
    void primeiroUsuarioAdotaOsDadosDaVersaoSingleUser() throws IOException {
        ProjetoService antigo = new ProjetoService(new JsonProjetoRepository(pasta.resolve("dados.json")));
        antigo.criarProjeto("Feito antes das contas", "");

        usuarios.cadastrar("maria", "Maria", "segredo123");

        assertFalse(Files.exists(pasta.resolve("dados.json")));
        ProjetoService daMaria = new ProjetoService(new JsonProjetoRepository(usuarios.arquivoDeDados("maria")));
        assertEquals("Feito antes das contas", daMaria.listarProjetos().get(0).getNome());

        // o segundo usuário começa vazio, não herda nada
        usuarios.cadastrar("joao", "João", "segredo123");
        assertTrue(new ProjetoService(new JsonProjetoRepository(usuarios.arquivoDeDados("joao")))
                .listarProjetos().isEmpty());
    }
}
