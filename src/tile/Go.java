package tile;

import model.Player;

public class Go extends Tile {
    private int reward;

    public Go(String name, int reward) {
        super(name);
        this.reward = reward;
    }

    @Override
    public void onLand(Player player) {
        System.out.println(player.getName() + " receives " + reward + " from GO");
        player.receive(reward);
    }
}