package model;

import tile.Tile;

public class Board {
    private static Board instance;
    private Tile[] tiles;

    public Board(Tile[] tiles) {
        this.tiles = tiles;
        for (Tile tile : tiles) {
            if (tile instanceof tile.Chance) {
                ((tile.Chance) tile).setBoard(this);
            }
        }
    }

    public Tile getTile(int position) {
        return tiles[position];
    }

    public int getSize() {
        return tiles.length;
    }

    public static Board getInstance(Tile[] tiles) {
        if (instance == null) {
            instance = new Board(tiles);
        }
        return instance;
    }
}
