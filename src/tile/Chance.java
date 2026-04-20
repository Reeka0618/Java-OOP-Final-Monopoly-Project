package tile;

import model.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class Chance extends Tile {

    private model.Board board;
    private Random random = new Random();

    public Chance(String name) { super(name); }

    public void setBoard(model.Board board) { this.board = board; }

    @Override
    public void onLand(Player player, Consumer<String> log) {
        int event = random.nextInt(10);
        log.accept("🃏 " + player.getName() + " drew a Chance card!");

        switch (event) {
            case 0:
                log.accept("🎉 Bank pays you $100!");
                player.receive(100);
                break;
            case 1:
                log.accept("💸 Pay $50 fine.");
                player.pay(50);
                break;
            case 2:
                log.accept("🚔 Go directly to Jail!");
                player.setInJail(true);
                player.setPosition(Player.JAIL_POSITION);
                break;
            case 3:
                log.accept("🔙 Move back 3 spaces!");
                int newPos3 = (player.getPosition() - 3 + board.getSize()) % board.getSize();
                player.setPosition(newPos3);
                board.getTile(player.getPosition()).onLand(player, log);
                break;
            case 4:
                log.accept("🏁 Advance to GO! Collect $200.");
                player.setPosition(0);
                player.receive(200);
                break;
            case 5:
                log.accept("🚶 Move forward 3 spaces!");
                int newPos5 = (player.getPosition() + 3) % board.getSize();
                player.setPosition(newPos5);
                board.getTile(player.getPosition()).onLand(player, log);
                break;
            case 6:
                log.accept("🏦 Bank error in your favor! Collect $150.");
                player.receive(150);
                break;
            case 7:
                int count = player.getProperties().size();
                int cost = count * 100;
                log.accept("🏚 Repairs! Pay $100 per property. Total: $" + cost);
                player.pay(cost);
                break;
            case 8:
                log.accept("🆓 Get Out of Jail Free card!");
                player.setHasJailFree(true);
                break;
            case 9:
                List<Integer> propTiles = new ArrayList<>();
                for (int i = 0; i < board.getSize(); i++) {
                    if (board.getTile(i) instanceof Property) propTiles.add(i);
                }
                int dest = propTiles.get(random.nextInt(propTiles.size()));
                player.setPosition(dest);
                log.accept("✈️ Vacation! Teleported to " + board.getTile(dest).getName());
                board.getTile(dest).onLand(player, log);
                break;
        }
    }
}
