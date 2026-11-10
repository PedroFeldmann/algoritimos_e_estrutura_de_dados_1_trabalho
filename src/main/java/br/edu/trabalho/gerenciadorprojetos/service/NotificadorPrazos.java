package br.edu.trabalho.gerenciadorprojetos.service;

import br.edu.trabalho.gerenciadorprojetos.domain.Status;

import java.time.LocalDate;
import java.util.List;

/**
 * Decide o que merece aviso (ADR-006): itens não concluídos que já estão
 * atrasados ou vencem dentro da janela de antecedência. Só lógica — mostrar o
 * aviso (bandeja do sistema, banner) é responsabilidade da UI.
 */
public final class NotificadorPrazos {

    /** Avisa de itens que vencem hoje ou nos próximos N dias. */
    public static final int DIAS_ANTECEDENCIA = 2;

    private NotificadorPrazos() {
    }

    public static List<PrazoItem> pendentesDeAviso(List<PrazoItem> prazos, LocalDate hoje) {
        LocalDate limite = hoje.plusDays(DIAS_ANTECEDENCIA);
        return prazos.stream()
                .filter(p -> p.status() != Status.CONCLUIDA)
                .filter(p -> p.dataLimite() != null)
                .filter(p -> p.atrasado() || !p.dataLimite().isAfter(limite))
                .toList();
    }

    /** Texto curto: "2 atrasados · 1 vence nos próximos 2 dias". Vazio se não há nada. */
    public static String resumo(List<PrazoItem> pendentes) {
        long atrasados = pendentes.stream().filter(PrazoItem::atrasado).count();
        long proximos = pendentes.size() - atrasados;
        StringBuilder texto = new StringBuilder();
        if (atrasados > 0) {
            texto.append(atrasados).append(atrasados == 1 ? " atrasado" : " atrasados");
        }
        if (proximos > 0) {
            if (texto.length() > 0) {
                texto.append(" · ");
            }
            texto.append(proximos).append(proximos == 1 ? " vence" : " vencem")
                    .append(" nos próximos ").append(DIAS_ANTECEDENCIA).append(" dias");
        }
        return texto.toString();
    }

    /** Chave estável de um item, para não repetir o mesmo aviso a cada verificação. */
    public static String chave(PrazoItem item) {
        return item.origem() + "|" + item.descricao() + "|" + item.dataLimite();
    }
}
