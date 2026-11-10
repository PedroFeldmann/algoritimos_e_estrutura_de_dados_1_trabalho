package br.edu.trabalho.gerenciadorprojetos.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;

/**
 * Notificação do sistema operacional (balão na bandeja, no Windows). Se o
 * sistema não suportar bandeja, não faz nada — o banner dentro do app continua
 * avisando. Todo acesso ao AWT roda fora da thread do JavaFX.
 */
final class NotificacaoSistema {

    private static TrayIcon icone;

    private NotificacaoSistema() {
    }

    static void notificar(String titulo, String mensagem) {
        Thread t = new Thread(() -> exibir(titulo, mensagem), "notificacao-sistema");
        t.setDaemon(true);
        t.start();
    }

    private static synchronized void exibir(String titulo, String mensagem) {
        try {
            System.setProperty("java.awt.headless", "false");
            if (!SystemTray.isSupported()) {
                return;
            }
            if (icone == null) {
                icone = new TrayIcon(desenharIcone(), "Gerenciador de Projetos");
                icone.setImageAutoSize(true);
                SystemTray.getSystemTray().add(icone);
            }
            icone.displayMessage(titulo, mensagem, TrayIcon.MessageType.WARNING);
        } catch (Exception | LinkageError e) {
            System.err.println("Notificação do sistema indisponível: " + e);
        }
    }

    /** Remove o ícone da bandeja ao fechar o app (senão ele fica "fantasma" até passar o mouse). */
    static synchronized void remover() {
        if (icone != null) {
            try {
                SystemTray.getSystemTray().remove(icone);
            } catch (Exception | LinkageError ignorado) {
                // encerrando de qualquer forma
            }
            icone = null;
        }
    }

    private static BufferedImage desenharIcone() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0x48, 0x55, 0xC9));
        g.fillRoundRect(1, 1, 30, 30, 10, 10);
        g.setColor(Color.WHITE);
        g.setStroke(new java.awt.BasicStroke(4f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        g.drawLine(9, 17, 14, 22);
        g.drawLine(14, 22, 23, 10);
        g.dispose();
        return img;
    }
}
