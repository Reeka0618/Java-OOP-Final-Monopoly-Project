package tile;

import model.Board;
import model.Player;

public abstract class Tile {
    protected String name;
    protected Board board;

    public void setBoard(Board board) {
        this.board = board;
    }

    public Tile(String name) {
        this.name = name;
    }

    public abstract void onLand(Player player, java.util.function.Consumer<String> log);

    public String getName() {
        return name;
    }
}
