package br.edu.trabalho.gerenciadorprojetos.service;

import br.edu.trabalho.gerenciadorprojetos.domain.Usuario;
import br.edu.trabalho.gerenciadorprojetos.repository.ArmazenamentoConfig;
import br.edu.trabalho.gerenciadorprojetos.repository.JsonUsuarioRepository;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Contas locais (ADR-006): cadastro e autenticação. Senhas são guardadas como
 * PBKDF2-HMAC-SHA256 com sal aleatório — nunca em texto puro. Cada conta tem o
 * seu arquivo de dados, então um usuário não enxerga os projetos do outro.
 */
public class UsuarioService {

    static final int TAMANHO_MINIMO_SENHA = 6;
    private static final Pattern LOGIN_VALIDO = Pattern.compile("[a-z0-9._-]{3,30}");
    private static final int ITERACOES = 120_000;
    private static final String MENSAGEM_CREDENCIAIS = "Usuário ou senha inválidos.";

    private final Path pasta;
    private final JsonUsuarioRepository repository;
    private final SecureRandom aleatorio = new SecureRandom();

    public UsuarioService(Path pasta) {
        this.pasta = pasta;
        this.repository = new JsonUsuarioRepository(pasta.resolve("usuarios.json"));
    }

    public boolean existemUsuarios() {
        return !repository.listarTodos().isEmpty();
    }

    /**
     * Cria a conta. O primeiro usuário adota os dados da versão single-user
     * ({@code dados.json}), se existirem, para ninguém perder o que já cadastrou.
     */
    public Usuario cadastrar(String login, String nome, String senha) {
        String loginNormalizado = login == null ? "" : login.trim().toLowerCase();
        if (!LOGIN_VALIDO.matcher(loginNormalizado).matches()) {
            throw new IllegalArgumentException(
                    "O usuário deve ter de 3 a 30 caracteres: letras minúsculas, números, ponto, hífen ou sublinhado.");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Informe o seu nome.");
        }
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new IllegalArgumentException("A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }
        var existentes = repository.listarTodos();
        if (existentes.stream().anyMatch(u -> u.getLogin().equals(loginNormalizado))) {
            throw new IllegalArgumentException("Já existe um usuário com esse login.");
        }

        byte[] sal = new byte[16];
        aleatorio.nextBytes(sal);
        Usuario usuario = new Usuario(loginNormalizado, nome.trim(),
                Base64.getEncoder().encodeToString(sal),
                Base64.getEncoder().encodeToString(derivar(senha, sal)));
        repository.adicionar(usuario);

        if (existentes.isEmpty()) {
            adotarDadosAntigos(loginNormalizado);
        }
        return usuario;
    }

    /** Devolve o usuário se login e senha conferem; mesma mensagem para login inexistente e senha errada. */
    public Usuario autenticar(String login, String senha) {
        String loginNormalizado = login == null ? "" : login.trim().toLowerCase();
        Usuario usuario = repository.listarTodos().stream()
                .filter(u -> u.getLogin().equals(loginNormalizado))
                .findFirst()
                .orElse(null);
        if (usuario == null || senha == null) {
            // Mesmo custo de uma tentativa real, para não revelar se o login existe pelo tempo de resposta.
            derivar(senha == null ? "" : senha, new byte[16]);
            throw new IllegalArgumentException(MENSAGEM_CREDENCIAIS);
        }
        byte[] esperado = Base64.getDecoder().decode(usuario.getHash());
        byte[] calculado = derivar(senha, Base64.getDecoder().decode(usuario.getSalt()));
        if (!MessageDigest.isEqual(esperado, calculado)) {
            throw new IllegalArgumentException(MENSAGEM_CREDENCIAIS);
        }
        return usuario;
    }

    public Path arquivoDeDados(String login) {
        return ArmazenamentoConfig.arquivoDoUsuario(pasta, login);
    }

    private void adotarDadosAntigos(String login) {
        try {
            Path legado = pasta.resolve("dados.json");
            if (Files.exists(legado)) {
                Files.move(legado, arquivoDeDados(login), StandardCopyOption.REPLACE_EXISTING);
            }
            Path legadoBackup = pasta.resolve("dados.json.bak");
            if (Files.exists(legadoBackup)) {
                Files.move(legadoBackup, arquivoDeDados(login).resolveSibling("dados-" + login + ".json.bak"),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao migrar os dados existentes para o novo usuário", e);
        }
    }

    private static byte[] derivar(String senha, byte[] sal) {
        PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), sal, ITERACOES, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 indisponível neste JDK", e);
        } finally {
            spec.clearPassword();
        }
    }
}
