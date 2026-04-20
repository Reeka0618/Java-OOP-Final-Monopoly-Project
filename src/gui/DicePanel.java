package gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Displays two dice side by side.
 * Images loaded from: <workspace>/images/dices/1/dice{n}.png
 *                and: <workspace>/images/dices/2/dice{n}.png
 * Falls back to drawn pip faces if images missing.
 */
public class DicePanel extends JPanel {

    private int die1 = 1, die2 = 1;
    private boolean showing  = false;
    private boolean rolling  = false;

    static final int DIE = 68;  // die image size px
    static final int PAD = 12;

    // [dieIndex 0|1][face 0–5]
    private final BufferedImage[][] imgs = new BufferedImage[2][6];

    public DicePanel() {
        setPreferredSize(new Dimension(DIE * 2 + PAD * 3, DIE + PAD * 2));
        setOpaque(false);
        loadImages();
    }

    // ── Image loading ───────────────────────────────────────────────
    private void loadImages() {
        // user.dir = VS Code workspace root = where your images/ folder is
        String base = System.getProperty("user.dir");
        System.out.println("[DicePanel] Working dir: " + base);

        int loaded = 0;
        for (int d = 0; d < 2; d++) {
            for (int face = 1; face <= 6; face++) {
                // Primary: <workspace>/images/dices/<d+1>/dice<face>.png
                String[] paths = {
                        base + "/images/dices/" + (d+1) + "/dice" + face + ".png",
                        base + "/../images/dices/" + (d+1) + "/dice" + face + ".png",
                        "images/dices/" + (d+1) + "/dice" + face + ".png",
                };
                for (String path : paths) {
                    File f = new File(path);
                    if (f.exists()) {
                        try {
                            imgs[d][face-1] = ImageIO.read(f);
                            loaded++;
                            break;
                        } catch (IOException ignored) {}
                    }
                }
            }
        }

        if (loaded == 12) {
            System.out.println("[DicePanel] ✓ All 12 dice images loaded.");
        } else if (loaded > 0) {
            System.out.println("[DicePanel] Partially loaded: " + loaded + "/12 dice images.");
        } else {
            System.out.println("[DicePanel] No dice images found — using drawn fallback.");
            System.out.println("[DicePanel] Expected path: " + base + "/images/dices/1/dice1.png");
        }
    }

    // ── Public API ──────────────────────────────────────────────────
    public void setValues(int d1, int d2) {
        die1 = d1; die2 = d2; showing = true;
        repaint();
    }

    public void startAnimation(Runnable onDone) {
        showing = true; rolling = true;
        Timer t = new Timer(70, null);
        int[] count = {0};
        t.addActionListener(e -> {
            die1 = (int)(Math.random() * 6) + 1;
            die2 = (int)(Math.random() * 6) + 1;
            repaint();
            if (++count[0] >= 14) {
                t.stop();
                rolling = false;
                onDone.run();
            }
        });
        t.start();
    }

    // ── Paint ───────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);

        int totalW = DIE * 2 + PAD;
        int x1 = (getWidth()  - totalW) / 2;
        int y  = (getHeight() - DIE)    / 2;
        int x2 = x1 + DIE + PAD;

        if (!showing) {
            // Placeholder outlines
            g2.setColor(new Color(180, 180, 180, 80));
            g2.fillRoundRect(x1, y, DIE, DIE, 12, 12);
            g2.fillRoundRect(x2, y, DIE, DIE, 12, 12);
            g2.setColor(new Color(140, 140, 140, 120));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x1, y, DIE, DIE, 12, 12);
            g2.drawRoundRect(x2, y, DIE, DIE, 12, 12);
            return;
        }

        float alpha = rolling ? 0.82f : 1.0f;
        Composite orig = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        paintDie(g2, 0, die1, x1, y);
        paintDie(g2, 1, die2, x2, y);
        g2.setComposite(orig);
    }

    private void paintDie(Graphics2D g2, int dieIdx, int val, int x, int y) {
        BufferedImage img = (val >= 1 && val <= 6) ? imgs[dieIdx][val-1] : null;
        if (img != null) {
            // Drop shadow
            g2.setColor(new Color(0, 0, 0, 55));
            g2.fillRoundRect(x+3, y+3, DIE, DIE, 12, 12);
            g2.drawImage(img, x, y, DIE, DIE, null);
        } else {
            drawPipDie(g2, val, x, y);
        }
    }

    private void drawPipDie(Graphics2D g2, int val, int x, int y) {
        // Shadow
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillRoundRect(x+3, y+3, DIE, DIE, 12, 12);
        // Face
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x, y, DIE, DIE, 12, 12);
        // Border
        g2.setColor(new Color(70, 50, 30));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, DIE, DIE, 12, 12);
        g2.setStroke(new BasicStroke(1));
        // Pips
        g2.setColor(new Color(20, 20, 20));
        for (int[] pip : pips(val)) {
            // pip positions are in 0–100 scale; map to die size
            int px = x + pip[0] * DIE / 100;
            int py = y + pip[1] * DIE / 100;
            g2.fillOval(px - 5, py - 5, 10, 10);
        }
    }

    private int[][] pips(int v) {
        switch (v) {
            case 1: return new int[][]{{50,50}};
            case 2: return new int[][]{{25,25},{75,75}};
            case 3: return new int[][]{{25,25},{50,50},{75,75}};
            case 4: return new int[][]{{25,25},{75,25},{25,75},{75,75}};
            case 5: return new int[][]{{25,25},{75,25},{50,50},{25,75},{75,75}};
            case 6: return new int[][]{{25,25},{75,25},{25,50},{75,50},{25,75},{75,75}};
            default: return new int[][]{};
        }
    }
}