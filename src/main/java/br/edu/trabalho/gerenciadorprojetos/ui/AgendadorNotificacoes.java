package br.edu.trabalho.gerenciadorprojetos.ui;

import br.edu.trabalho.gerenciadorprojetos.repository.ArmazenamentoConfig;
import br.edu.trabalho.gerenciadorprojetos.service.NotificadorPrazos;
import br.edu.trabalho.gerenciadorprojetos.service.PrazoItem;
import br.edu.trabalho.gerenciadorprojetos.service.ProjetoService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Verifica prazos ao entrar e a cada 30 minutos enquanto o app está aberto, e
 * avisa pela bandeja do sistema. Cada item é avisado uma vez por sessão; a
 * preferência "avisar prazos" é lida a cada verificação, então ligar/desligar
 * vale na hora.
 */
final class AgendadorNotificacoes {

    private static final Duration INTERVALO = Duration.minutes(30);

    private final ProjetoService service;
    private final String login;
    private final Set<String> jaAvisados = new HashSet<>();
    private final Timeline relogio;

    AgendadorNotificacoes(ProjetoService service, String login) {
        this.service = service;
        this.login = login;
        this.relogio = new Timeline(new KeyFrame(INTERVALO, e -> verificar()));
        this.relogio.setCycleCount(Animation.INDEFINITE);
    }

    void iniciar() {
        verificar();
        relogio.play();
    }

    void parar() {
        relogio.stop();
    }

    private void verificar() {
        if (!ArmazenamentoConfig.notificacoesAtivas(login)) {
            return;
        }
        List<PrazoItem> pendentes = NotificadorPrazos.pendentesDeAviso(
                service.listarProximosPrazos(), LocalDate.now());
        List<PrazoItem> novos = pendentes.stream()
                .filter(p -> jaAvisados.add(NotificadorPrazos.chave(p)))
                .toList();
        if (novos.isEmpty()) {
            return;
        }
        String detalhe = novos.stream().limit(3).map(PrazoItem::descricao)
                .reduce((a, b) -> a + ", " + b).orElse("");
        if (novos.size() > 3) {
            detalhe += "…";
        }
        NotificacaoSistema.notificar("Prazos do Gerenciador de Projetos",
                NotificadorPrazos.resumo(novos) + "\n" + detalhe);
    }
}
