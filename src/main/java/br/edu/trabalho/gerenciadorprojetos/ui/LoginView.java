package br.edu.trabalho.gerenciadorprojetos.ui;

import br.edu.trabalho.gerenciadorprojetos.domain.Usuario;
import br.edu.trabalho.gerenciadorprojetos.repository.ArmazenamentoConfig;
import br.edu.trabalho.gerenciadorprojetos.service.UsuarioService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/** Tela de entrada (ADR-006): login ou criação de conta. Sem conta nenhuma, abre direto no cadastro. */
public class LoginView {

    private final Navigator navigator;
    private final Consumer<Sessao> aoEntrar;
    private final UsuarioService usuarios = new UsuarioService(ArmazenamentoConfig.pastaAtual());
    private boolean modoCadastro;

    public LoginView(Navigator navigator, Consumer<Sessao> aoEntrar) {
        this.navigator = navigator;
        this.aoEntrar = aoEntrar;
        this.modoCadastro = !usuarios.existemUsuarios();
    }

    public void exibir() {
        navigator.mostrar(construir(null), modoCadastro ? "Criar conta" : "Entrar");
    }

    private Parent construir(String mensagemErro) {
        Label titulo = new Label("Gerenciador de Projetos");
        titulo.getStyleClass().add("page-title");
        Label subtitulo = new Label(modoCadastro
                ? (usuarios.existemUsuarios()
                        ? "Crie uma nova conta."
                        : "Crie o primeiro usuário. Dados já cadastrados neste computador passam a ser dele.")
                : "Entre para ver os seus projetos.");
        subtitulo.getStyleClass().add("page-subtitle");
        subtitulo.setWrapText(true);

        TextField login = campo("Usuário (ex.: maria)");
        TextField nome = campo("Seu nome");
        PasswordField senha = new PasswordField();
        senha.setPromptText("Senha");
        senha.getStyleClass().add("form-field");
        PasswordField confirmacao = new PasswordField();
        confirmacao.setPromptText("Repita a senha");
        confirmacao.getStyleClass().add("form-field");

        Label erro = new Label(mensagemErro == null ? "" : mensagemErro);
        erro.getStyleClass().add("form-error");
        erro.setWrapText(true);
        erro.setManaged(mensagemErro != null);
        erro.setVisible(mensagemErro != null);

        Button acao = new Button(modoCadastro ? "Criar conta e entrar" : "Entrar");
        acao.getStyleClass().add("btn-primary");
        acao.setMaxWidth(Double.MAX_VALUE);
        acao.setDefaultButton(true);
        acao.setOnAction(e -> {
            try {
                Usuario usuario;
                if (modoCadastro) {
                    if (!senha.getText().equals(confirmacao.getText())) {
                        throw new IllegalArgumentException("As senhas não são iguais.");
                    }
                    usuario = usuarios.cadastrar(login.getText(), nome.getText(), senha.getText());
                } else {
                    usuario = usuarios.autenticar(login.getText(), senha.getText());
                }
                aoEntrar.accept(new Sessao(usuario.getLogin(), usuario.getNome()));
            } catch (IllegalArgumentException ex) {
                navigator.mostrar(construir(ex.getMessage()), modoCadastro ? "Criar conta" : "Entrar");
            }
        });

        Button alternar = new Button(modoCadastro ? "Já tenho conta" : "Criar uma conta");
        alternar.getStyleClass().add("btn-ghost");
        alternar.setMaxWidth(Double.MAX_VALUE);
        alternar.setVisible(!modoCadastro || usuarios.existemUsuarios());
        alternar.setManaged(alternar.isVisible());
        alternar.setOnAction(e -> {
            modoCadastro = !modoCadastro;
            exibir();
        });

        VBox card = new VBox(12, titulo, subtitulo, login);
        if (modoCadastro) {
            card.getChildren().add(nome);
        }
        card.getChildren().add(senha);
        if (modoCadastro) {
            card.getChildren().add(confirmacao);
        }
        card.getChildren().addAll(erro, acao, alternar);
        card.getStyleClass().addAll("card", "login-card");
        card.setMaxWidth(360);
        card.setPadding(new Insets(28));

        StackPane centro = new StackPane(card);
        centro.setAlignment(Pos.CENTER);
        centro.setPadding(new Insets(24));
        return centro;
    }

    private TextField campo(String dica) {
        TextField campo = new TextField();
        campo.setPromptText(dica);
        campo.getStyleClass().add("form-field");
        return campo;
    }
}
