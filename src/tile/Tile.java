package tile;

import model.Board;
import model.Player;

public abstract class Tile {
    protected String name;
    protected Board board;

    public Tile(String name) {
        this.name = name;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public abstract void onLand(Player player, java.util.function.Consumer<String> log);

    public void onLand(Player player) { }

    public String getName() {
        return name;
    }
}
