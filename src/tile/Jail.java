package tile;

import model.Player;

public class Jail extends Tile {

    public Jail(String name) {
        super(name);
    }

    @Override
    public void onLand(Player player) {
        System.out.println(player.getName() + " is just visiting Jail.");
    }
}