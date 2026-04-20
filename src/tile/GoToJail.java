package tile;

import model.Player;
import java.util.function.Consumer;

public class GoToJail extends Tile {
    public GoToJail(String name) { super(name); }

    @Override
    public void onLand(Player player, Consumer<String> log) {
        log.accept("🚔 " + player.getName() + " is sent to Jail!");
        player.setInJail(true);
        player.setPosition(Player.JAIL_POSITION);
    }
}
