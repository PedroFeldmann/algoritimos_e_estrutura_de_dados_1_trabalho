package br.edu.trabalho.gerenciadorprojetos.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmazenamentoConfigTest {

    @TempDir
    Path raiz;

    @Test
    void mudarDePastaLevaContasEDadosDeTodosOsUsuarios() throws IOException {
        Path antiga = Files.createDirectory(raiz.resolve("antiga"));
        Path nova = raiz.resolve("nova");
        for (String nome : new String[]{"usuarios.json", "dados-ana.json", "dados-bia.json", "dados-ana.json.bak"}) {
            Files.writeString(antiga.resolve(nome), nome);
        }
        Files.writeString(antiga.resolve("foto.png"), "não é do app");

        ArmazenamentoConfig.migrarArquivos(antiga, nova);

        for (String nome : new String[]{"usuarios.json", "dados-ana.json", "dados-bia.json", "dados-ana.json.bak"}) {
            assertEquals(nome, Files.readString(nova.resolve(nome)));
            assertFalse(Files.exists(antiga.resolve(nome)));
        }
        assertTrue(Files.exists(antiga.resolve("foto.png")), "arquivos alheios ao app não são tocados");
        assertFalse(Files.exists(nova.resolve("foto.png")));
    }

    @Test
    void pastaDeDestinoComDadosNaoESobrescrita() throws IOException {
        Path antiga = Files.createDirectory(raiz.resolve("antiga"));
        Path nova = Files.createDirectory(raiz.resolve("nova"));
        Files.writeString(antiga.resolve("usuarios.json"), "da antiga");
        Files.writeString(nova.resolve("usuarios.json"), "da nova");

        ArmazenamentoConfig.migrarArquivos(antiga, nova);

        assertEquals("da nova", Files.readString(nova.resolve("usuarios.json")));
        assertEquals("da antiga", Files.readString(antiga.resolve("usuarios.json")));
    }

    @Test
    void arquivoDoUsuarioFicaNaPastaComNomeDoLogin() {
        assertEquals(raiz.resolve("dados-maria.json"), ArmazenamentoConfig.arquivoDoUsuario(raiz, "maria"));
    }
}
