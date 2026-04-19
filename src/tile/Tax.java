package tile;

import model.Player;

public class Tax extends Tile {
    private int tax;

    public Tax(String name, int tax) {
        super(name);
        this.tax = tax;
    }

    @Override
    public void onLand(Player player) {
        System.out.println(player.getName() + " pays tax: " + tax);
        player.pay(tax);
    }
}