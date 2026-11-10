package br.edu.trabalho.gerenciadorprojetos.ui;

import br.edu.trabalho.gerenciadorprojetos.domain.Status;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Estado e controles de "filtrar por status" e "ordenar" de uma lista. Vive na
 * instância da tela, então a escolha sobrevive aos redesenhos após cada ação.
 */
final class FiltroLista {

    enum Filtro {
        TODAS("Todas"),
        ATRASADAS("Atrasadas"),
        NAO_INICIADAS("Não iniciadas"),
        EM_ANDAMENTO("Em andamento"),
        CONCLUIDAS("Concluídas");

        private final String rotulo;

        Filtro(String rotulo) {
            this.rotulo = rotulo;
        }

        boolean aceita(Status status, boolean atrasado) {
            return switch (this) {
                case TODAS -> true;
                case ATRASADAS -> atrasado;
                case NAO_INICIADAS -> status == Status.NAO_INICIADA && !atrasado;
                case EM_ANDAMENTO -> status == Status.EM_ANDAMENTO && !atrasado;
                case CONCLUIDAS -> status == Status.CONCLUIDA;
            };
        }

        @Override
        public String toString() {
            return rotulo;
        }
    }

    enum Ordem {
        MANUAL("Ordem manual"),
        PRAZO("Por prazo");

        private final String rotulo;

        Ordem(String rotulo) {
            this.rotulo = rotulo;
        }

        @Override
        public String toString() {
            return rotulo;
        }
    }

    private Filtro filtro = Filtro.TODAS;
    private Ordem ordem = Ordem.MANUAL;

    boolean filtrando() {
        return filtro != Filtro.TODAS;
    }

    /** Subir/descer só faz sentido na ordem manual e sem filtro (senão os índices não batem). */
    boolean reordenavel() {
        return ordem == Ordem.MANUAL && filtro == Filtro.TODAS;
    }

    /** Barra com os seletores; {@code comOrdem=false} mostra só o filtro. */
    HBox barra(boolean comOrdem, Runnable aoMudar) {
        ComboBox<Filtro> seletorFiltro = new ComboBox<>();
        seletorFiltro.getItems().addAll(Filtro.values());
        seletorFiltro.setValue(filtro);
        seletorFiltro.setOnAction(e -> {
            filtro = seletorFiltro.getValue();
            aoMudar.run();
        });

        HBox barra = new HBox(8, new Label("Mostrar:"), seletorFiltro);
        barra.setAlignment(Pos.CENTER_LEFT);

        if (comOrdem) {
            ComboBox<Ordem> seletorOrdem = new ComboBox<>();
            seletorOrdem.getItems().addAll(Ordem.values());
            seletorOrdem.setValue(ordem);
            seletorOrdem.setOnAction(e -> {
                ordem = seletorOrdem.getValue();
                aoMudar.run();
            });
            Label rotuloOrdem = new Label("Ordenar:");
            HBox.setMargin(rotuloOrdem, new javafx.geometry.Insets(0, 0, 0, 12));
            barra.getChildren().addAll(rotuloOrdem, seletorOrdem);
        }
        return barra;
    }

    /** Aplica filtro e ordenação (estável; itens sem data ficam por último) numa cópia da lista. */
    <T> List<T> aplicar(List<T> itens, Function<T, Status> status, Predicate<T> atrasado,
                        Function<T, LocalDate> data) {
        List<T> resultado = new ArrayList<>();
        for (T item : itens) {
            if (filtro.aceita(status.apply(item), atrasado.test(item))) {
                resultado.add(item);
            }
        }
        if (ordem == Ordem.PRAZO) {
            resultado.sort(Comparator.comparing(data, Comparator.nullsLast(Comparator.naturalOrder())));
        }
        return resultado;
    }

    /** Seletor de status (não iniciada / em andamento / concluída) para uma linha de etapa ou tarefa. */
    static ComboBox<Status> seletorStatus(Status atual, Consumer<Status> aoEscolher) {
        ComboBox<Status> seletor = new ComboBox<>();
        seletor.getItems().addAll(Status.values());
        seletor.setConverter(new StringConverter<>() {
            @Override
            public String toString(Status s) {
                return s == null ? "" : s.getRotulo();
            }

            @Override
            public Status fromString(String texto) {
                return null;
            }
        });
        seletor.setValue(atual);
        seletor.setOnAction(e -> {
            Status escolhido = seletor.getValue();
            if (escolhido != null && escolhido != atual) {
                aoEscolher.accept(escolhido);
            }
        });
        return seletor;
    }
}
