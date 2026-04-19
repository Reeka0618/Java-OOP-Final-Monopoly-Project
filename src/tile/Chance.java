package tile;

import model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Chance extends Tile {

    private model.Board board;
    private Random random = new Random();


    public Chance(String name) {
        super(name);
    }

    public void setBoard(model.Board board) {
        this.board = board;
    }

    @Override
    public void onLand(Player player) {
        int event = random.nextInt(10);

        System.out.println(player.getName() + " landed on " + name + " (Chance!)");

        switch (event) {
            case 0:
                System.out.println("🎉 You received 100!");
                player.receive(100);
                break;

            case 1:
                System.out.println("💸 You lost 50!");
                player.pay(50);
                break;

            case 5:
                System.out.println("🚶 Move forward 3 steps!");
                player.move(3, board.getSize());
                Tile next2 = board.getTile(player.getPosition());
                next2.onLand(player);
                System.out.println(player.getName() + " moved to position " + board.getTile(player.getPosition()).getName());
                break;

            case 2:
                int jailIndex = 10;

                System.out.println("🚔 Go to Jail!");
                player.setInJail(true);
                player.setPosition(jailIndex);
                break;
            case 3:
                System.out.println("🔙 Move backward 3 steps!");
                player.move(-3, board.getSize());
                Tile next1 = board.getTile(player.getPosition());
                next1.onLand(player);
                System.out.println(player.getName() + " moved to " + board.getTile(player.getPosition()).getName());
                break;
            case 4:
                System.out.println("🏁 Go directly to GO!, receive 200");
                player.setPosition(0);
                player.receive(200);
                break;
            case 6:
                System.out.println("🎁 Bank error in your favor +150!");
                player.receive(150);
                break;
            case 7:
                System.out.println("🏚 Repair cost: pay 100 per property");
                int propertyCount = player.getProperties().size();
                player.pay(propertyCount*100);
                break;
            case 8:
                System.out.println("🆓 Received a Jail Free Card!");
                player.setHasJailFree(true);
                break;
            case 9: //
                List<Integer> propertyTiles = new ArrayList<>();

                for (int i = 0; i < board.getSize(); i++) { // get list of property index
                    if (board.getTile(i) instanceof Property) {
                        propertyTiles.add(i);
                    }
                }

                int randomIndex = random.nextInt(propertyTiles.size());
                int dest = propertyTiles.get(randomIndex);

                player.setPosition(dest);

                System.out.println("✈️ You have a vacation to " + board.getTile(dest).getName());

                board.getTile(dest).onLand(player);
                break;
            default: 
                System.out.println("No event triggered.");
        }
    }
}