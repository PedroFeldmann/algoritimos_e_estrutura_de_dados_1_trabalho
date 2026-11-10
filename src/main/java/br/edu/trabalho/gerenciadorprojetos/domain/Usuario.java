package br.edu.trabalho.gerenciadorprojetos.domain;

/**
 * Conta de usuário local. A senha nunca é guardada: só o sal e o hash
 * (PBKDF2), ambos em Base64. Cada usuário tem o seu próprio arquivo de dados.
 */
public class Usuario {

    private String login;
    private String nome;
    private String salt;
    private String hash;

    /** Construtor vazio + setters existem só para desserialização Jackson (ADR-002/ADR-003). */
    public Usuario() {
    }

    public Usuario(String login, String nome, String salt, String hash) {
        this.login = login;
        this.nome = nome;
        this.salt = salt;
        this.hash = hash;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
