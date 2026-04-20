package gui;

import game.GameEngine;
import game.GameEngine.Phase;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main Swing window for the Monopoly game.
 * Layout:
 *   LEFT:  BoardPanel (the 30-tile board)
 *   RIGHT: PlayerInfoPanel + DicePanel + ActionPanel + LogPanel
 */
public class GameWindow extends JFrame {

    private GameEngine engine;
    private BoardPanel boardPanel;
    private DicePanel dicePanel;
    private PlayerInfoPanel playerInfoPanel;
    private JTextArea logArea;

    // Action buttons
    private JButton rollBtn;
    private JButton endTurnBtn;
    private JButton buyBtn;
    private JButton skipBtn;
    private JButton jailFreeBtn;

    public GameWindow(GameEngine engine) {
        this.engine = engine;

        setTitle("🇻🇳 Việt Nam Monopoly");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 0));
        getContentPane().setBackground(new Color(28, 70, 28));

        buildUI();
        updateButtonStates();

        pack();
        setLocationRelativeTo(null);
        // Board is BW+20 wide (704px), sidebar 290px, plus frame chrome
        setMinimumSize(new Dimension(BoardPanel.BW + 20 + 310, BoardPanel.BH + 60));
        setVisible(true);
    }

    private void buildUI() {
        // ── LEFT: Board ───────────────────────────────────────────────
        boardPanel = new BoardPanel(engine);
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));
        add(boardPanel, BorderLayout.CENTER);

        // ── RIGHT: Sidebar ────────────────────────────────────────────
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(38, 38, 38));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));
        sidebar.setPreferredSize(new Dimension(280, 600));

        // Player Info
        sidebar.add(sectionLabel("PLAYERS"));
        sidebar.add(Box.createVerticalStrut(5));
        playerInfoPanel = new PlayerInfoPanel(engine);
        sidebar.add(playerInfoPanel);

        sidebar.add(Box.createVerticalStrut(12));

        // Dice
        sidebar.add(sectionLabel("DICE"));
        sidebar.add(Box.createVerticalStrut(5));
        dicePanel = new DicePanel();
        dicePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel diceWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        diceWrapper.setOpaque(false);
        diceWrapper.add(dicePanel);
        sidebar.add(diceWrapper);

        sidebar.add(Box.createVerticalStrut(10));

        // Action Buttons
        sidebar.add(sectionLabel("ACTIONS"));
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(buildButtonPanel());

        sidebar.add(Box.createVerticalStrut(12));

        // Log
        sidebar.add(sectionLabel("GAME LOG"));
        sidebar.add(Box.createVerticalStrut(5));
        logArea = new JTextArea(10, 20);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBackground(new Color(20, 20, 20));
        logArea.setForeground(new Color(200, 220, 180));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        logScroll.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        logScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 999));
        sidebar.add(logScroll);

        add(sidebar, BorderLayout.EAST);
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(160, 160, 160));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        return lbl;
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 6));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        rollBtn     = makeButton("🎲  ROLL DICE",       new Color(34, 139, 34),  this::onRoll);
        endTurnBtn  = makeButton("▶  END TURN",         new Color(60, 100, 180), this::onEndTurn);
        buyBtn      = makeButton("🏠  BUY PROPERTY",    new Color(180, 120, 0),  this::onBuy);
        skipBtn     = makeButton("⏭  SKIP PURCHASE",    new Color(100, 100, 100),this::onSkip);
        jailFreeBtn = makeButton("🆓  USE JAIL-FREE",   new Color(150, 0, 150),  this::onJailFree);

        panel.add(rollBtn);
        panel.add(buyBtn);
        panel.add(skipBtn);
        panel.add(jailFreeBtn);
        panel.add(endTurnBtn);

        return panel;
    }

    private JButton makeButton(String text, Color color, Runnable action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(color.darker());
                } else if (getModel().isRollover() && isEnabled()) {
                    g2.setColor(color.brighter());
                } else {
                    g2.setColor(isEnabled() ? color : new Color(70, 70, 70));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(240, 38));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    // ── Button Handlers ───────────────────────────────────────────────

    private void onRoll() {
        rollBtn.setEnabled(false);
        dicePanel.startAnimation(() -> {
            engine.rollDice();
            dicePanel.setValues(engine.getDice().getLastDie1(), engine.getDice().getLastDie2());
            dicePanel.repaint();
            refreshAll();
        });
    }

    private void onEndTurn() {
        engine.endTurn();
        refreshAll();
    }

    private void onBuy() {
        engine.buyProperty();
        refreshAll();
    }

    private void onSkip() {
        engine.skipBuy();
        refreshAll();
    }

    private void onJailFree() {
        engine.useJailFreeCard();
        refreshAll();
    }

    // ── UI Refresh ────────────────────────────────────────────────────

    public void refreshAll() {
        updateButtonStates();
        playerInfoPanel.refresh();
        boardPanel.repaint();

        if (engine.getPhase() == Phase.GAME_OVER) {
            JOptionPane.showMessageDialog(this,
                    "Game over! Check the log for the winner.",
                    "Game Over", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateButtonStates() {
        Phase phase = engine.getPhase();
        rollBtn.setEnabled(phase == Phase.WAITING_TO_ROLL);
        endTurnBtn.setEnabled(phase == Phase.TURN_OVER);
        buyBtn.setEnabled(phase == Phase.WAITING_BUY_DECISION);
        skipBtn.setEnabled(phase == Phase.WAITING_BUY_DECISION);
        jailFreeBtn.setEnabled(phase == Phase.WAITING_JAIL_DECISION &&
                engine.getCurrentPlayer() != null &&
                engine.getCurrentPlayer().hasJailFreeCard());
    }

    // Called by engine via log consumer
    public void appendLog(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}