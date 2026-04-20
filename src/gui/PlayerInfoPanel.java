package gui;

import game.GameEngine;
import model.Player;

import javax.swing.*;
import java.awt.*;

public class PlayerInfoPanel extends JPanel {

    private GameEngine engine;
    private JPanel[] cards;

    public PlayerInfoPanel(GameEngine engine) {
        this.engine = engine;
        setLayout(new GridLayout(0, 1, 6, 6));
        setOpaque(false);
        rebuild();
    }

    public void refresh() {
        rebuild();
        revalidate();
        repaint();
    }

    private void rebuild() {
        removeAll();
        Player[] players = engine.getPlayers();
        int current = engine.getCurrentPlayerIndex();

        for (int i = 0; i < players.length; i++) {
            if (players[i] == null) continue;
            Player p = players[i];
            boolean isActive = (i == current);
            add(buildCard(p, isActive));
        }
    }

    private JPanel buildCard(Player p, boolean active) {
        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBackground(active ? new Color(255, 250, 220) : new Color(245, 245, 245));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(active ? new Color(200, 160, 0) : new Color(180, 180, 180), active ? 2 : 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        // Token dot
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(p.getTokenColor());
                g2.fillOval(2, 2, 18, 18);
                g2.setColor(p.getTokenColor().darker());
                g2.drawOval(2, 2, 18, 18);
            }
        };
        dot.setPreferredSize(new Dimension(22, 22));
        dot.setOpaque(false);

        // Name + status
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        namePanel.setOpaque(false);
        JLabel nameLabel = new JLabel(p.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(active ? new Color(100, 60, 0) : Color.DARK_GRAY);
        namePanel.add(nameLabel);

        if (p.isInJail()) {
            JLabel jailBadge = new JLabel(" 🔒 JAIL ");
            jailBadge.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            jailBadge.setForeground(new Color(180, 0, 0));
            namePanel.add(jailBadge);
        }
        if (active) {
            JLabel turnBadge = new JLabel(" ← TURN ");
            turnBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            turnBadge.setForeground(new Color(0, 120, 0));
            namePanel.add(turnBadge);
        }

        // Money & properties
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        statsPanel.setOpaque(false);
        JLabel moneyLabel = new JLabel("$" + p.getMoney());
        moneyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        moneyLabel.setForeground(p.getMoney() < 200 ? new Color(180, 0, 0) : new Color(30, 100, 30));
        statsPanel.add(moneyLabel);

        JLabel propLabel = new JLabel("🏠 " + p.getProperties().size());
        propLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        propLabel.setForeground(Color.GRAY);
        statsPanel.add(propLabel);

        if (p.hasJailFreeCard()) {
            JLabel cardLabel = new JLabel("🆓");
            statsPanel.add(cardLabel);
        }

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(namePanel, BorderLayout.NORTH);
        left.add(statsPanel, BorderLayout.SOUTH);

        card.add(dot, BorderLayout.WEST);
        card.add(left, BorderLayout.CENTER);

        return card;
    }
}
