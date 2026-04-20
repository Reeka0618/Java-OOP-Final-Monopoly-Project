package gui;

import game.GameEngine;
import model.Player;
import tile.Property;
import tile.Tile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * 40-tile Monopoly board.
 *
 * Standard layout — 4 corners + 9 normal tiles per side:
 *
 *  [30:TL]  [29][28][27][26][25][24][23][22][21]  [20:TR]
 *  [31]                                            [19]
 *  [32]                                            [18]
 *  [33]               CENTER                       [17]
 *  [34]                                            [16]
 *  [35]                                            [15]
 *  [36]                                            [14]
 *  [37]                                            [13]
 *  [38]                                            [12]
 *  [39]                                            [11]
 *  [0:BL]   [1] [2] [3] [4] [5] [6] [7] [8] [9]  [10:BR]
 *
 * Corners:  0=GO(BL), 10=Jail(BR), 20=FreeParking(TR), 30=GoToJail(TL)
 * Segments:
 *   Bottom L→R : 0(BL), 1–9, 10(BR)       = 11 tiles
 *   Right  B→T : 11–19, 20(TR)            = 10 tiles
 *   Top    R→L : 20(TR), 21–29, 30(TL)    = 11 tiles
 *   Left   T→B : 31–39                    = 9 tiles (no extra corners needed)
 *   Total: corners shared = 4, unique = 4+9+9+9+9 = 40 ✓
 *
 * Board pixel dimensions:
 *   C  = corner size (px)
 *   TW = tile narrow width
 *   N  = 9 (normal tiles between corners)
 *   BW = BH = C + N*TW + C
 */
public class BoardPanel extends JPanel {

    private final GameEngine engine;
    private BufferedImage boardBgImage = null;

    // ── Size constants ─────────────────────────────────────────────
    public static final int C  = 90;            // corner square (px)
    public static final int TW = 56;            // tile narrow dimension
    public static final int TH = C;             // tile tall  dimension = C
    public static final int N  = 9;             // normal tiles between corners
    public static final int BW = C + N*TW + C;  // = 90 + 504 + 90 = 684
    public static final int BH = BW;            // square board

    // ── Colors ─────────────────────────────────────────────────────
    private static final Color BG_FELT     = new Color(28,  75, 28);
    private static final Color COL_CREAM   = new Color(255,248,220);
    private static final Color COL_CENTER  = new Color(195,232,195);
    private static final Color COL_BORDER  = new Color(60,  42, 12);
    private static final Color COL_PROP    = new Color(255,253,238);
    private static final Color COL_GO      = new Color(168,230,168);
    private static final Color COL_JAIL    = new Color(255,215, 80);
    private static final Color COL_PARK    = new Color(168,210,255);
    private static final Color COL_GOTOJ   = new Color(255,148,148);
    private static final Color COL_CHANCE  = new Color(215,175,255);
    private static final Color COL_TAX     = new Color(255,182,148);

    // Property color bands — 9 color groups to match initBoard
    private static final Color[] BAND = {
            new Color(150, 90, 40),   // 0 Brown      — Lào Cai, Điện Biên
            new Color( 75,195,230),   // 1 Light Blue  — Hà Giang, Cao Bằng, Lạng Sơn
            new Color(205,  0,200),   // 2 Pink        — Hồ Hoàn Kiếm, Phố Cổ
            new Color(255,138,  0),   // 3 Orange      — Hải Phòng, Nam Định, Ninh Bình
            new Color(220, 20, 60),   // 4 Red         — Thanh Hóa, Nghệ An, Huế, Đà Nẵng
            new Color(220,195,  0),   // 5 Yellow      — Hội An, Quy Nhơn, Nha Trang
            new Color(  0,162,  0),   // 6 Green       — Đà Lạt, Buôn Ma Thuột, Gia Lai, Kon Tum
            new Color( 70,150, 70),   // 7 Lt Green    — Bình Dương, Đồng Nai, TP.HCM, Vũng Tàu
            new Color(  0, 55,210),   // 8 Dark Blue   — Cần Thơ, Phú Quốc, Hà Nội
    };

    // ── Constructor ────────────────────────────────────────────────
    public BoardPanel(GameEngine engine) {
        this.engine = engine;
        setPreferredSize(new Dimension(BW + 20, BH + 20));
        setBackground(BG_FELT);
        loadBoardBg();
    }

    // ── Image loading ───────────────────────────────────────────────
    private void loadBoardBg() {
        // user.dir is the VS Code workspace root — exactly where your images/ folder lives
        String base = System.getProperty("user.dir");
        String[] candidates = {
                base + "/images/board_bg.png",
                base + "/images/dices/board_bg.png",
                base + "/../images/board_bg.png",
                "images/board_bg.png",
        };

        System.out.println("[BoardPanel] Working dir: " + base);
        for (String path : candidates) {
            File f = new File(path);
            System.out.println("[BoardPanel] Trying: " + f.getAbsolutePath() + " → exists=" + f.exists());
            if (f.exists()) {
                try {
                    boardBgImage = ImageIO.read(f);
                    System.out.println("[BoardPanel] ✓ Loaded board_bg.png");
                    return;
                } catch (IOException e) {
                    System.out.println("[BoardPanel] Failed to read: " + e.getMessage());
                }
            }
        }
        System.out.println("[BoardPanel] board_bg.png not found — using solid color.");
    }

    // ── Paint ───────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

        int offX = Math.max((getWidth()  - BW) / 2, 0);
        int offY = Math.max((getHeight() - BH) / 2, 0);

        // Board base
        if (boardBgImage != null) {
            g2.drawImage(boardBgImage, offX, offY, BW, BH, null);
        } else {
            g2.setColor(COL_CREAM);
            g2.fillRect(offX, offY, BW, BH);
        }
        g2.setColor(COL_BORDER);
        g2.setStroke(new BasicStroke(3));
        g2.drawRect(offX, offY, BW, BH);
        g2.setStroke(new BasicStroke(1));

        // Green center
        g2.setColor(COL_CENTER);
        g2.fillRect(offX + C, offY + C, N * TW, N * TW);

        // Tiles
        for (int i = 0; i < 40; i++) drawTile(g2, i, offX, offY);

        // Center text & legend
        drawCenter(g2, offX, offY);

        // Player tokens (on top of everything)
        drawPlayers(g2, offX, offY);
    }

    // ── Tile rect ───────────────────────────────────────────────────
    /**
     * Returns the screen rectangle for tile [index] given board offset (offX, offY).
     *
     * Corners  : 0(BL), 10(BR), 20(TR), 30(TL) → C×C
     * Bottom   : 1–9   → x=C+(i-1)*TW,  y=BH-TH,  w=TW, h=TH
     * Right    : 11–19 → x=BW-TH,  y=BH-C-(row+1)*TW,  w=TH, h=TW   row=i-11
     * Top      : 21–29 → x=C+(29-i)*TW, y=0,  w=TW, h=TH
     * Left     : 31–39 → x=0, y=C+(row)*TW, w=TH, h=TW   row=i-31
     */
    public Rectangle getTileRect(int index, int offX, int offY) {
        // Corners
        if (index ==  0) return new Rectangle(offX,             offY + BH - C, C,  C);  // BL GO
        if (index == 10) return new Rectangle(offX + C + N*TW,  offY + BH - C, C,  C);  // BR Jail
        if (index == 20) return new Rectangle(offX + C + N*TW,  offY,          C,  C);  // TR FreeParking
        if (index == 30) return new Rectangle(offX,             offY,          C,  C);  // TL GoToJail

        // Bottom row: tiles 1–9
        if (index >= 1 && index <= 9)
            return new Rectangle(offX + C + (index - 1) * TW, offY + BH - TH, TW, TH);

        // Right col: tiles 11–19
        if (index >= 11 && index <= 19) {
            int row = index - 11;   // 0=bottom(near jail), 8=top(near freepark)
            return new Rectangle(offX + BW - TH, offY + BH - C - (row + 1) * TW, TH, TW);
        }

        // Top row: tiles 21–29
        // tile 21 is adjacent to TR corner (right side), tile 29 adjacent to TL corner (left side)
        if (index >= 21 && index <= 29) {
            int col = index - 21;   // 0=rightmost (near TR), 8=leftmost (near TL)
            return new Rectangle(offX + C + (N - 1 - col) * TW, offY, TW, TH);
        }

        // Left col: tiles 31–39
        if (index >= 31 && index <= 39) {
            int row = index - 31;   // 0=top (near TL), 8=bottom (near BL)
            return new Rectangle(offX, offY + C + row * TW, TH, TW);
        }

        return new Rectangle(offX, offY, TW, TW); // fallback, never reached
    }

    // ── Draw one tile ───────────────────────────────────────────────
    private void drawTile(Graphics2D g2, int index, int offX, int offY) {
        Rectangle r  = getTileRect(index, offX, offY);
        Tile      t  = engine.getTileAt(index);
        boolean corner = (index == 0 || index == 10 || index == 20 || index == 30);

        // Background
        g2.setColor(getTileColor(t));
        g2.fillRect(r.x, r.y, r.width, r.height);

        // Property color band (on the inward-facing edge)
        if (t instanceof Property) {
            g2.setColor(getBandColor(index));
            final int BT = 13; // band thickness px
            if (index >= 1  && index <= 9)  g2.fillRect(r.x, r.y,                   r.width, BT);
            if (index >= 11 && index <= 19) g2.fillRect(r.x, r.y,                   BT, r.height);
            if (index >= 21 && index <= 29) g2.fillRect(r.x, r.y + r.height - BT,  r.width, BT);
            if (index >= 31 && index <= 39) g2.fillRect(r.x + r.width - BT, r.y,   BT, r.height);
        }

        // Owned-property dot
        if (t instanceof Property) {
            Player owner = ((Property) t).getOwner();
            if (owner != null) {
                int ox = r.x + r.width/2 - 7, oy = r.y + r.height/2 - 7;
                g2.setColor(owner.getTokenColor());
                g2.fillOval(ox, oy, 14, 14);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(ox, oy, 14, 14);
                g2.setStroke(new BasicStroke(1));
            }
        }

        // Border
        g2.setColor(COL_BORDER);
        g2.setStroke(new BasicStroke(1));
        g2.drawRect(r.x, r.y, r.width, r.height);

        if (corner) drawCorner(g2, r, index);
        else        drawTileName(g2, r, t.getName(), index);
    }

    // ── Corner tile artwork ─────────────────────────────────────────
    private void drawCorner(Graphics2D g2, Rectangle r, int idx) {
        int cx = r.x + r.width  / 2;
        int cy = r.y + r.height / 2;

        switch (idx) {
            case 0: { // GO — bottom-left
                g2.setColor(new Color(0, 118, 0));
                g2.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 24));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("GO", cx - fm.stringWidth("GO") / 2, cy + 4);
                g2.setFont(new Font("Arial", Font.PLAIN, 9));
                fm = g2.getFontMetrics();
                String s = "Collect $200";
                g2.setColor(new Color(0, 90, 0));
                g2.drawString(s, cx - fm.stringWidth(s) / 2, cy + 18);
                // Arrow →
                int ay = r.y + r.height - 16;
                g2.setColor(new Color(0, 130, 0));
                g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(r.x + 8, ay, r.x + r.width - 13, ay);
                int[] axP = {r.x+r.width-9, r.x+r.width-18, r.x+r.width-18};
                int[] ayP = {ay, ay-6, ay+6};
                g2.fillPolygon(axP, ayP, 3);
                g2.setStroke(new BasicStroke(1));
                break;
            }
            case 10: { // JAIL — bottom-right
                g2.setColor(new Color(140, 100, 0));
                g2.setFont(new Font("Georgia", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                String[] ls = {"JAIL", "/ VISITING"};
                int ly = cy - 6;
                for (String l : ls) {
                    g2.drawString(l, cx - fm.stringWidth(l) / 2, ly);
                    ly += 16;
                }
                // Bars
                g2.setColor(new Color(120, 80, 0));
                g2.setStroke(new BasicStroke(2.5f));
                for (int b = 0; b < 4; b++)
                    g2.drawLine(r.x + 10 + b*9, r.y + 6, r.x + 10 + b*9, r.y + 28);
                g2.setStroke(new BasicStroke(1));
                break;
            }
            case 20: { // FREE PARKING — top-right
                g2.setColor(new Color(20, 75, 185));
                g2.setFont(new Font("Georgia", Font.BOLD, 28));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("P", cx - fm.stringWidth("P") / 2, cy + 8);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(r.x + 10, r.y + 10, r.width - 20, r.height - 20);
                g2.setStroke(new BasicStroke(1));
                g2.setFont(new Font("Arial", Font.BOLD, 8));
                fm = g2.getFontMetrics();
                String fp = "FREE PARKING";
                g2.setColor(new Color(10, 55, 160));
                g2.drawString(fp, cx - fm.stringWidth(fp) / 2, r.y + r.height - 7);
                break;
            }
            case 30: { // GO TO JAIL — top-left
                g2.setColor(new Color(155, 0, 0));
                g2.setFont(new Font("Georgia", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                String[] ls = {"GO TO", "JAIL"};
                int ly = cy - 6;
                for (String l : ls) {
                    g2.drawString(l, cx - fm.stringWidth(l) / 2, ly);
                    ly += 16;
                }
                g2.setColor(new Color(130, 0, 0));
                g2.setStroke(new BasicStroke(2.5f));
                for (int b = 0; b < 4; b++)
                    g2.drawLine(r.x + 10 + b*9, r.y + 6, r.x + 10 + b*9, r.y + 28);
                g2.setStroke(new BasicStroke(1));
                break;
            }
        }
    }

    // ── Tile name ───────────────────────────────────────────────────
    private void drawTileName(Graphics2D g2, Rectangle r, String name, int idx) {
        boolean rightCol  = idx >= 11 && idx <= 19;
        boolean leftCol   = idx >= 31 && idx <= 39;
        boolean bottomRow = idx >= 1  && idx <= 9;
        boolean topRow    = idx >= 21 && idx <= 29;

        Font f = new Font("Arial", Font.PLAIN, 9);

        Graphics2D tg = (Graphics2D) g2.create();
        tg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        tg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        tg.setColor(new Color(18, 10, 2));
        tg.setFont(f);
        FontMetrics fm = tg.getFontMetrics();

        // Small icon prefix for special tiles
        String prefix = getTileIcon(engine.getTileAt(idx));
        String display = prefix + name;
        String[] words = display.split("\\s+");

        if (rightCol || leftCol) {
            // Rotated: long axis = r.height
            double angle = rightCol ? -Math.PI / 2.0 : Math.PI / 2.0;
            List<String> lines = wrap(words, fm, r.height - 8);
            int lineH = fm.getHeight();
            int totalH = lines.size() * lineH;

            tg.translate(r.x + r.width / 2.0, r.y + r.height / 2.0);
            tg.rotate(angle);
            int startY = -totalH / 2 + fm.getAscent();
            for (String line : lines) {
                tg.drawString(line, -fm.stringWidth(line) / 2, startY);
                startY += lineH;
            }
        } else {
            // Upright — bottom or top row
            final int BT = 13;
            int textTop, availH;
            if (bottomRow) {
                // Band at top → text below band
                textTop  = r.y + BT + 2;
                availH   = r.height - BT - 4;
            } else {
                // Top row, band at bottom → text in upper portion
                textTop  = r.y + 2;
                availH   = r.height - BT - 4;
            }

            List<String> lines = wrap(words, fm, r.width - 6);
            int lineH = fm.getHeight();
            int totalH = lines.size() * lineH;
            int startY = textTop + Math.max(0, (availH - totalH) / 2) + fm.getAscent();

            for (String line : lines) {
                tg.drawString(line, r.x + (r.width - fm.stringWidth(line)) / 2, startY);
                startY += lineH;
            }
        }
        tg.dispose();
    }

    private String getTileIcon(Tile t) {
        if (t instanceof tile.Chance) return "? ";
        if (t instanceof tile.Tax)    return "$ ";
        return "";
    }

    private List<String> wrap(String[] words, FontMetrics fm, int maxW) {
        List<String> lines = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String w : words) {
            String test = cur.length() == 0 ? w : cur + " " + w;
            if (fm.stringWidth(test) <= maxW) {
                cur = new StringBuilder(test);
            } else {
                if (cur.length() > 0) lines.add(cur.toString());
                cur = new StringBuilder(w);
            }
        }
        if (cur.length() > 0) lines.add(cur.toString());
        return lines;
    }

    // ── Center ──────────────────────────────────────────────────────
    private void drawCenter(Graphics2D g2, int offX, int offY) {
        int cx = offX + C;
        int cy = offY + C;
        int cw = N * TW; // 504

        g2.setColor(new Color(12, 72, 12));
        g2.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 30));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString("VIETNAM",  cx + (cw - fm.stringWidth("VIETNAM"))  / 2, cy + cw/2 - 22);
        g2.drawString("MONOPOLY",  cx + (cw - fm.stringWidth("MONOPOLY")) / 2, cy + cw/2 + 22);

        g2.setColor(new Color(0, 105, 0));
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(cx + cw/5, cy + cw/2 + 30, cx + cw*4/5, cy + cw/2 + 30);
        g2.setStroke(new BasicStroke(1));

        // Player legend
        Player[] players = engine.getPlayers();
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fm = g2.getFontMetrics();
        int ly = cy + cw/2 + 52;
        for (Player p : players) {
            if (p == null) continue;
            g2.setColor(p.getTokenColor());
            g2.fillOval(cx + 20, ly - 12, 16, 16);
            g2.setColor(p.getTokenColor().darker());
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawOval(cx + 20, ly - 12, 16, 16);
            g2.setStroke(new BasicStroke(1));
            g2.setColor(new Color(12, 42, 12));
            g2.drawString(p.getName() + "  $" + p.getMoney(), cx + 42, ly);
            ly += 24;
        }
    }

    // ── Player tokens ────────────────────────────────────────────────
    private void drawPlayers(Graphics2D g2, int offX, int offY) {
        Player[] players = engine.getPlayers();
        Map<Integer, List<Integer>> byPos = new LinkedHashMap<>();
        for (int i = 0; i < players.length; i++) {
            if (players[i] == null) continue;
            byPos.computeIfAbsent(players[i].getPosition(), k -> new ArrayList<>()).add(i);
        }

        for (Map.Entry<Integer, List<Integer>> e : byPos.entrySet()) {
            Rectangle r = getTileRect(e.getKey(), offX, offY);
            List<Integer> idx = e.getValue();
            for (int k = 0; k < idx.size(); k++) {
                int pi  = idx.get(k);
                Player p = players[pi];
                boolean cur = (pi == engine.getCurrentPlayerIndex());

                int sz = cur ? 22 : 18;
                int tx = r.x + 4 + (k % 2) * (sz + 3);
                int ty = r.y + r.height - sz - 4 - (k / 2) * (sz + 3);

                // Shadow
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillOval(tx+2, ty+2, sz, sz);
                // Body
                g2.setColor(p.getTokenColor());
                g2.fillOval(tx, ty, sz, sz);
                // Gloss
                g2.setColor(new Color(255,255,255,85));
                g2.fillOval(tx+3, ty+3, sz/2, sz/2);
                // Ring for current player
                if (cur) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawOval(tx, ty, sz, sz);
                    g2.setStroke(new BasicStroke(1));
                }
                // Initial
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, cur ? 11 : 9));
                FontMetrics fm = g2.getFontMetrics();
                String ini = p.getName().substring(0, 1).toUpperCase();
                g2.drawString(ini, tx + (sz - fm.stringWidth(ini))/2, ty + sz - (cur ? 5 : 4));
            }
        }
    }

    // ── Color helpers ────────────────────────────────────────────────
    private Color getTileColor(Tile t) {
        if (t instanceof tile.Go)          return COL_GO;
        if (t instanceof tile.Jail)        return COL_JAIL;
        if (t instanceof tile.FreeParking) return COL_PARK;
        if (t instanceof tile.GoToJail)    return COL_GOTOJ;
        if (t instanceof tile.Chance)      return COL_CHANCE;
        if (t instanceof tile.Tax)         return COL_TAX;
        if (t instanceof Property)         return COL_PROP;
        return Color.WHITE;
    }

    private Color getBandColor(int i) {
        // Match groups defined in GameEngine.initBoard
        if (i == 1 || i == 3)                        return BAND[0]; // brown
        if (i == 5 || i == 6 || i == 8)              return BAND[1]; // light blue
        if (i == 9 || i == 11)                       return BAND[2]; // pink
        if (i == 12 || i == 13 || i == 14)           return BAND[3]; // orange
        if (i == 16 || i == 17 || i == 18 || i==19) return BAND[4]; // red
        if (i == 21 || i == 23 || i == 24)           return BAND[5]; // yellow
        if (i == 26 || i == 27 || i == 28 || i==29) return BAND[6]; // green
        if (i == 31 || i == 32 || i == 34 || i==35) return BAND[7]; // lt green
        if (i == 37 || i == 38 || i == 39)           return BAND[8]; // dark blue
        return Color.LIGHT_GRAY;
    }
}