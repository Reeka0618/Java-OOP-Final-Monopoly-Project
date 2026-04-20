package game;

import model.Board;
import model.Dice;
import model.Player;
import tile.*;

import java.awt.Color;
import java.util.function.Consumer;

/**
 * GameEngine drives all game logic.
 * Uses Phase enum + callbacks — no Scanner, no System.out.
 *
 * 40-tile board layout (standard Monopoly):
 *   BOTTOM (L→R) : 0(GO), 1,2,3,4,5,6,7,8,9,  10(Jail/Visiting)
 *   RIGHT  (B→T) : 11,12,13,14,15,16,17,18,19, 20(FreeParking)
 *   TOP    (R→L) : 21,22,23,24,25,26,27,28,29, 30(GoToJail)
 *   LEFT   (T→B) : 31,32,33,34,35,36,37,38,39
 */
public class GameEngine {

    public enum Phase {
        WAITING_TO_ROLL,
        WAITING_BUY_DECISION,
        WAITING_JAIL_DECISION,
        TURN_OVER,
        GAME_OVER
    }

    private Player[] players;
    private Board board;
    private Dice dice;
    private int currentPlayerIndex;
    private Phase phase;
    private Consumer<String> log;
    private Runnable onStateChange;

    private static final Color[] TOKEN_COLORS = {
            new Color(220, 53,  69),   // red
            new Color( 13,110, 253),   // blue
            new Color( 25,135,  84),   // green
            new Color(255,193,   7),   // yellow
    };

    public GameEngine(String[] playerNames, Consumer<String> log, Runnable onStateChange) {
        this.log           = log;
        this.onStateChange = onStateChange;
        this.dice          = new Dice();

        players = new Player[playerNames.length];
        for (int i = 0; i < playerNames.length; i++) {
            players[i] = new Player(playerNames[i], TOKEN_COLORS[i]);
        }

        initBoard();
        currentPlayerIndex = 0;
        phase = Phase.WAITING_TO_ROLL;

        log.accept("🎲 Game started! " + playerNames.length + " players.");
        log.accept("─────────────────────────");
        log.accept("➤  " + getCurrentPlayer().getName() + "'s turn");
    }

    // ─── Actions called by GUI ────────────────────────────────────────

    public void rollDice() {
        if (phase != Phase.WAITING_TO_ROLL) return;
        Player player = getCurrentPlayer();

        if (player.isInJail()) {
            handleJailRoll(player);
            return;
        }

        int total = dice.rollTwo();
        log.accept("🎲 Rolled " + dice.getLastDie1() + " + " + dice.getLastDie2() + " = " + total);

        player.move(total, board.getSize());
        tile.Tile tile = board.getTile(player.getPosition());
        log.accept("📍 " + player.getName() + " → " + tile.getName() + " (tile " + player.getPosition() + ")");

        tile.onLand(player, log);

        if (tile instanceof Property && ((Property) tile).hasPendingOffer()) {
            phase = Phase.WAITING_BUY_DECISION;
        } else {
            phase = Phase.TURN_OVER;
        }

        checkBankruptcy(player);
        onStateChange.run();
    }

    private void handleJailRoll(Player player) {
        if (player.hasJailFreeCard()) {
            phase = Phase.WAITING_JAIL_DECISION;
            log.accept("🔓 You have a Get Out of Jail Free card!");
            log.accept("   Click USE JAIL-FREE or ROLL DICE to try doubles.");
            onStateChange.run();
            return;
        }

        int total = dice.rollTwo();
        log.accept("🎲 Jail roll: " + dice.getLastDie1() + " + " + dice.getLastDie2());
        if (dice.isDouble()) {
            log.accept("🎉 Doubles! " + player.getName() + " breaks free!");
            player.setInJail(false);
            player.move(total, board.getSize());
            tile.Tile t = board.getTile(player.getPosition());
            log.accept("📍 Landed on: " + t.getName());
            t.onLand(player, log);
            phase = (t instanceof Property && ((Property) t).hasPendingOffer())
                    ? Phase.WAITING_BUY_DECISION : Phase.TURN_OVER;
        } else {
            log.accept("❌ No doubles. " + player.getName() + " stays in Jail.");
            phase = Phase.TURN_OVER;
        }
        onStateChange.run();
    }

    public void useJailFreeCard() {
        if (phase != Phase.WAITING_JAIL_DECISION) return;
        Player p = getCurrentPlayer();
        p.setHasJailFree(false);
        p.setInJail(false);
        log.accept("🆓 " + p.getName() + " used Jail Free card — now free!");
        phase = Phase.WAITING_TO_ROLL;
        onStateChange.run();
    }

    public void buyProperty() {
        if (phase != Phase.WAITING_BUY_DECISION) return;
        Player player = getCurrentPlayer();
        tile.Tile t = board.getTile(player.getPosition());
        if (t instanceof Property) {
            Property prop = (Property) t;
            prop.acceptPurchase();
            log.accept("✅ " + player.getName() + " bought " + prop.getName() + " for $" + prop.getPrice());
        }
        phase = Phase.TURN_OVER;
        checkBankruptcy(player);
        onStateChange.run();
    }

    public void skipBuy() {
        if (phase != Phase.WAITING_BUY_DECISION) return;
        tile.Tile t = board.getTile(getCurrentPlayer().getPosition());
        if (t instanceof Property) ((Property) t).declinePurchase();
        log.accept("⏭  " + getCurrentPlayer().getName() + " skipped buying.");
        phase = Phase.TURN_OVER;
        onStateChange.run();
    }

    public void endTurn() {
        if (phase != Phase.TURN_OVER) return;

        Player current = getCurrentPlayer();
        if (current.isBankrupt()) {
            log.accept("💀 " + current.getName() + " is bankrupt and eliminated!");
            for (Property p : current.getProperties()) p.setOwner(null);
            current.getProperties().clear();
            players[currentPlayerIndex] = null;
        }

        if (countActive() == 1) {
            Player winner = getWinner();
            log.accept("🏆 " + winner.getName() + " wins the game!");
            phase = Phase.GAME_OVER;
            onStateChange.run();
            return;
        }

        advanceToNextPlayer();
        phase = Phase.WAITING_TO_ROLL;
        log.accept("─────────────────────────");
        log.accept("➤  " + getCurrentPlayer().getName() + "'s turn");
        if (getCurrentPlayer().isInJail()) {
            log.accept("🔒 " + getCurrentPlayer().getName() + " is in Jail — roll for doubles!");
        }
        onStateChange.run();
    }

    // ─── Helpers ─────────────────────────────────────────────────────

    private void checkBankruptcy(Player p) {
        if (p.isBankrupt()) log.accept("⚠️  " + p.getName() + " is bankrupt!");
    }

    private void advanceToNextPlayer() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.length;
        } while (players[currentPlayerIndex] == null);
    }

    private int countActive() {
        int c = 0;
        for (Player p : players) if (p != null && !p.isBankrupt()) c++;
        return c;
    }

    private Player getWinner() {
        for (Player p : players) if (p != null && !p.isBankrupt()) return p;
        return null;
    }

    // ─── Getters ─────────────────────────────────────────────────────

    public Player   getCurrentPlayer()      { return players[currentPlayerIndex]; }
    public Player[] getPlayers()            { return players; }
    public Board    getBoard()              { return board; }
    public Dice     getDice()               { return dice; }
    public Phase    getPhase()              { return phase; }
    public int      getCurrentPlayerIndex() { return currentPlayerIndex; }
    public tile.Tile getTileAt(int pos)     { return board.getTile(pos); }

    // ─── 40-Tile Board ───────────────────────────────────────────────
    //
    // Standard Monopoly layout: 4 corners + 9 normal tiles per side = 40
    //
    //  Corner tiles : 0 (GO/BL), 10 (Jail/BR), 20 (FreeParking/TR), 30 (GoToJail/TL)
    //  Bottom L→R   : 0–10
    //  Right  B→T   : 11–19, then corner 20
    //  Top    R→L   : 20–30
    //  Left   T→B   : 31–39, wraps back to 0
    //
    // Vietnamese city ordering (roughly N→S then back up the other side):
    //   Group 1 (Brown)      : Lào Cai, Điện Biên
    //   Group 2 (Light Blue) : Hà Giang, Cao Bằng, Lạng Sơn
    //   Group 3 (Pink)       : Hải Phòng, Nam Định
    //   Group 4 (Orange)     : Ninh Bình, Thanh Hóa, Nghệ An
    //   Group 5 (Red)        : Huế, Đà Nẵng
    //   Group 6 (Yellow)     : Hội An, Nha Trang, Đà Lạt
    //   Group 7 (Green)      : TP.HCM, Cần Thơ, Vũng Tàu
    //   Group 8 (Dark Blue)  : Phú Quốc, Hà Nội
    //
    private void initBoard() {
        tile.Tile[] tiles = new tile.Tile[40];

        // ── Corners ──────────────────────────────────────────────────
        tiles[0]  = new Go("GO", 200);
        tiles[10] = new Jail("Jail");
        tiles[20] = new FreeParking("Free Parking");
        tiles[30] = new GoToJail("Go To Jail");

        // ── Special non-corner tiles ──────────────────────────────────
        tiles[2]  = new Chance("Chance");           // bottom row
        tiles[4]  = new Tax("Income Tax", 200);     // bottom row
        tiles[7]  = new Chance("Chance");           // bottom row
        tiles[15] = new Tax("Gift Tax", 100);       // right col
        tiles[22] = new Chance("Chance");           // top row
        tiles[25] = new Tax("Luxury Tax", 150);     // top row
        tiles[33] = new Chance("Chance");           // left col
        tiles[36] = new Tax("Super Tax", 100);      // left col

        // ── Group 1 – Brown (Tây Bắc) ────────────────────────────────
        tiles[1]  = new Property("Lào Cai",          60,  10);
        tiles[3]  = new Property("Điện Biên",         60,  10);

        // ── Group 2 – Light Blue (Đông Bắc) ──────────────────────────
        tiles[5]  = new Property("Hà Giang",         100,  20);
        tiles[6]  = new Property("Cao Bằng",         100,  20);
        tiles[8]  = new Property("Lạng Sơn",         120,  25);

        // ── Group 3 – Pink (Đồng Bằng Bắc Bộ) ───────────────────────
        tiles[9]  = new Property("Hồ Hoàn Kiếm",    140,  30);
        tiles[11] = new Property("Phố Cổ Hà Nội",   140,  30);

        // ── Group 4 – Orange (Bắc Trung Bộ) ─────────────────────────
        tiles[12] = new Property("Hải Phòng",        160,  35);
        tiles[13] = new Property("Nam Định",         180,  40);
        tiles[14] = new Property("Ninh Bình",        180,  40);

        // ── Group 5 – Red (Trung Bộ) ─────────────────────────────────
        tiles[16] = new Property("Thanh Hóa",        200,  45);
        tiles[17] = new Property("Nghệ An",          220,  50);
        tiles[18] = new Property("Huế",              220,  50);
        tiles[19] = new Property("Đà Nẵng",          240,  55);

        // ── Group 6 – Yellow (Nam Trung Bộ) ──────────────────────────
        tiles[21] = new Property("Hội An",           260,  60);
        tiles[23] = new Property("Quy Nhơn",         260,  60);
        tiles[24] = new Property("Nha Trang",        280,  65);

        // ── Group 7 – Green (Tây Nguyên) ─────────────────────────────
        tiles[26] = new Property("Đà Lạt",           300,  70);
        tiles[27] = new Property("Buôn Ma Thuột",    300,  70);
        tiles[28] = new Property("Gia Lai",          320,  75);
        tiles[29] = new Property("Kon Tum",          320,  75);

        // ── Group 8 – Light Green (Đông Nam Bộ) ──────────────────────
        tiles[31] = new Property("Bình Dương",       350,  80);
        tiles[32] = new Property("Đồng Nai",         350,  80);
        tiles[34] = new Property("TP. HCM",          400, 100);
        tiles[35] = new Property("Vũng Tàu",         400, 100);

        // ── Group 9 – Dark Blue (Tây Nam Bộ / flagship) ──────────────
        tiles[37] = new Property("Cần Thơ",          450, 120);
        tiles[38] = new Property("Phú Quốc",         500, 140);
        tiles[39] = new Property("Hà Nội",           600, 160);

        board = new Board(tiles);
    }
}