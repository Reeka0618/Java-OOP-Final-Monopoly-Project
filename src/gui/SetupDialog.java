package gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A dialog that asks for number of players and their names before starting the game.
 */
public class SetupDialog extends JDialog {

    private String[] playerNames;
    private boolean confirmed = false;

    public SetupDialog(JFrame parent) {
        super(parent, "New Game Setup", true);
        buildUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        setBackground(new Color(28, 70, 28));
        JPanel content = new JPanel();
        content.setLayout(new BorderLayout(10, 10));
        content.setBackground(new Color(28, 70, 28));
        content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Title
        JLabel title = new JLabel("Vietnam Monopoly");
        title.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 20));
        title.setForeground(new Color(255, 220, 80));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        content.add(title, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(40, 60, 40));
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 120, 80), 1, true),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Number of players
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel numLabel = new JLabel("Number of players:");
        numLabel.setForeground(Color.WHITE);
        numLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(numLabel, gbc);

        gbc.gridx = 1;
        String[] nums = {"2", "3", "4"};
        JComboBox<String> numBox = new JComboBox<>(nums);
        numBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        numBox.setPreferredSize(new Dimension(60, 28));
        form.add(numBox, gbc);

        // Name fields (max 4)
        JTextField[] nameFields = new JTextField[4];
        JLabel[] nameLabels = new JLabel[4];
        String[] defaults = {"Alice", "Bob", "Carol", "Dave"};
        Color[] colors = {new Color(220, 80, 80), new Color(80, 120, 220), new Color(60, 160, 80), new Color(200, 160, 0)};

        for (int i = 0; i < 4; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1;
            JLabel dot = new JLabel("●  Player " + (i + 1) + ":");
            dot.setForeground(colors[i]);
            dot.setFont(new Font("Segoe UI", Font.BOLD, 12));
            nameLabels[i] = dot;
            form.add(dot, gbc);

            gbc.gridx = 1;
            nameFields[i] = new JTextField(defaults[i], 14);
            nameFields[i].setFont(new Font("Segoe UI", Font.PLAIN, 13));
            form.add(nameFields[i], gbc);
        }

        // Initially show only 2
        for (int i = 2; i < 4; i++) {
            nameLabels[i].setVisible(false);
            nameFields[i].setVisible(false);
        }

        numBox.addActionListener(e -> {
            int count = Integer.parseInt((String) numBox.getSelectedItem());
            for (int i = 0; i < 4; i++) {
                boolean vis = (i < count);
                nameLabels[i].setVisible(vis);
                nameFields[i].setVisible(vis);
            }
            pack();
        });

        content.add(form, BorderLayout.CENTER);

        // Start button
        JButton startBtn = new JButton("START GAME  ▶") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(20, 100, 20) : new Color(34, 139, 34));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
            }
        };
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        startBtn.setForeground(Color.WHITE);
        startBtn.setOpaque(false);
        startBtn.setContentAreaFilled(false);
        startBtn.setBorderPainted(false);
        startBtn.setFocusPainted(false);
        startBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startBtn.setPreferredSize(new Dimension(200, 42));
        startBtn.addActionListener(e -> {
            int count = Integer.parseInt((String) numBox.getSelectedItem());
            playerNames = new String[count];
            for (int i = 0; i < count; i++) {
                String name = nameFields[i].getText().trim();
                playerNames[i] = name.isEmpty() ? ("Player " + (i + 1)) : name;
            }
            confirmed = true;
            dispose();
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(new Color(28, 70, 28));
        btnPanel.add(startBtn);
        content.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(content);
    }

    public String[] getPlayerNames() { return playerNames; }
    public boolean isConfirmed() { return confirmed; }
}
