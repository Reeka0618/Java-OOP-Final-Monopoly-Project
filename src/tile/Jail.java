package tile;

import model.Player;
import java.util.function.Consumer;

public class Jail extends Tile {
    public Jail(String name) { super(name); }

    @Override
    public void onLand(Player player, Consumer<String> log) {
        log.accept("👀 " + player.getName() + " is just visiting Jail.");
    }
}