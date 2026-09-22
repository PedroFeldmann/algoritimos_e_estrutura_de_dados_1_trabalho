package br.edu.trabalho.gerenciadorprojetos.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Formulário compartilhado para criar/editar Projeto, Etapa ou Tarefa — tela 04
 * dos wireframes. Muda só o título da janela, o rótulo do campo principal e os
 * valores iniciais; o contexto (em qual Projeto/Etapa o item entra ou qual item
 * está sendo editado) é responsabilidade de quem abre o diálogo.
 */
public class ItemFormDialog {

    public record Resultado(String titulo, String descricao, LocalDate dataLimite) {
    }

    /** Diálogo de criação, campos em branco. */
    public static Optional<Resultado> abrir(String tituloJanela) {
        return abrir(tituloJanela, "Título", new Resultado("", "", null));
    }

    /** Diálogo de edição, pré-preenchido com {@code valoresAtuais}. */
    public static Optional<Resultado> abrir(String tituloJanela, String rotuloCampoPrincipal, Resultado valoresAtuais) {
        Dialog<Resultado> dialog = new Dialog<>();
        dialog.setTitle(tituloJanela);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        TextField campoTitulo = new TextField(valoresAtuais.titulo());
        campoTitulo.setPromptText(rotuloCampoPrincipal);
        campoTitulo.getStyleClass().add("form-field");
        TextArea campoDescricao = new TextArea(valoresAtuais.descricao());
        campoDescricao.setPromptText("Descrição");
        campoDescricao.setPrefRowCount(3);
        campoDescricao.getStyleClass().add("form-field");
        DatePicker campoData = new DatePicker(valoresAtuais.dataLimite());
        campoData.getStyleClass().add("form-field");

        dialog.getDialogPane().getStylesheets()
                .add(ItemFormDialog.class.getResource("app.css").toExternalForm());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.addRow(0, new Label(rotuloCampoPrincipal), campoTitulo);
        grid.addRow(1, new Label("Descrição"), campoDescricao);
        grid.addRow(2, new Label("Data limite"), campoData);
        dialog.getDialogPane().setContent(grid);

        Button botaoOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        botaoOk.getStyleClass().add("btn-primary");
        botaoOk.disableProperty().bind(campoTitulo.textProperty().isEmpty());

        dialog.setResultConverter(botao -> {
            if (botao == ButtonType.OK) {
                return new Resultado(campoTitulo.getText(), campoDescricao.getText(), campoData.getValue());
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
