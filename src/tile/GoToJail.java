package tile;

import model.Player;

public class GoToJail extends Tile {

    public GoToJail(String name) {
        super(name);
    }

    @Override
    public void onLand(Player player) {
        System.out.println(player.getName() + " is sent to Jail!");
        player.setInJail(true);
        player.setPosition(Player.JAIL_POSITION);
    }
}