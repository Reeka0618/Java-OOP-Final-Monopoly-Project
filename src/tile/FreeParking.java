package tile;

import model.Player;
import java.util.function.Consumer;

public class FreeParking extends Tile {
    public FreeParking(String name) { super(name); }

    @Override
    public void onLand(Player player, Consumer<String> log) {
        log.accept("🅿️ " + player.getName() + " is parked for free. Nothing happens.");
    }
}
