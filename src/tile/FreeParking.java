package tile;

import model.Player;

public class FreeParking extends Tile {

    public FreeParking(String name) {
        super(name);
    }

    @Override
    public void onLand(Player player) {
        System.out.println(player.getName() + " landed on " + name + ". Nothing happens.");
    }
}