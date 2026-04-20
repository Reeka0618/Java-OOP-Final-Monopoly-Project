package tile;

import model.Player;
import java.util.function.Consumer;

public class Tax extends Tile {
    private int tax;
    public Tax(String name, int tax) { super(name); this.tax = tax; }

    @Override
    public void onLand(Player player, Consumer<String> log) {
        log.accept("🧾 " + player.getName() + " pays $" + tax + " in tax!");
        player.pay(tax);
    }
}
