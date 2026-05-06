package tile;

import model.Player;
import java.util.function.Consumer;

public class Go extends Tile {
    private int reward;
    public Go(String name, int reward) { super(name); this.reward = reward; }

    @Override
    public void onLand(Player player, Consumer<String> log) {
        log.accept("🟢 " + player.getName() + " landed on GO and collects $" + reward + "!");
        player.receive(reward);
    }
}