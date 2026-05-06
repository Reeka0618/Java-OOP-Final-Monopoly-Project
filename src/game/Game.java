package game;

import model.*;
import tile.*;
import java.util.Scanner;

public class Game {
    private Player[] players;
    private Board board;
    private Dice dice;
    private int currentPlayerIndex;
    private Scanner scanner;

    public Game() {
        initPlayers();
        initBoard();
        dice = new Dice();
        scanner = new Scanner(System.in);
        currentPlayerIndex = 0;
    }

    private void initPlayers() {
        scanner = new Scanner(System.in);

        int numPlayers;

	while (true) {
		System.out.print("Enter number of players (2-4): ");
				try {
					numPlayers = Integer.parseInt(scanner.nextLine());
					if (numPlayers >= 2 && numPlayers <=4) {
						break;
					} else {
						System.out.println("Must be between 2 and 4 players");
					}
				} catch (NumberFormatException e) {
					System.out.println("Invalid number. Try again");
				}
		}

		players = new Player[numPlayers];

		for (int i = 0; i < numPlayers; i++) {
			System.out.println("Enter name for Player " + (i+1) + ": ");
			String name = scanner.nextLine();
			players[i] = new Player(name);
		}
    }

    public void start() {
        while (true) {
            Player player = players[currentPlayerIndex];

			if (player == null) {
				nextTurn();
				continue;
			}

			if (handleJail(player)) {
				nextTurn();
				continue;
			}

            System.out.println("\n--- " + player.getName() + "'s turn ---");

            System.out.print("Press ENTER to roll dice...");
            scanner.nextLine();

            int steps = dice.roll();  // Tung xúc xắc
            System.out.println("Rolled: " + steps);

            player.move(steps, board.getSize());

            Tile tile = board.getTile(player.getPosition());
            System.out.println(player.getName() + " landed on " + tile.getName());

            tile.onLand(player);

			printPlayerStatus(player);
			
            System.out.println("Press ENTER to end turn...");
            scanner.nextLine();

            if (player.isBankrupt()) {
				System.out.println(player.getName() + " is bankrupt and eliminated!");
				for (Property p : player.getProperties()) {
					p.setOwner(null);
				}

				player.getProperties().clear();
				
                players[currentPlayerIndex] = null;
            }

            if (countActivePlayers() == 1) {
                Player winner = getWinner();
                System.out.println("🏆 " + winner.getName() + " wins the game!");
                break;
            }

			System.out.println("------------------");
            nextTurn();
        }
    }

    private void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.length;
    }

	private boolean handleJail(Player player) {
		if (player.isInJail()) {
			System.out.println(player.getName() + " is in Jail!");

			// CHECK JAIL FREE CARD
			if (player.hasJailFreeCard()) {
				System.out.println("You have a Jail Free Card. Use it? (y/n)");
				
				String input = scanner.nextLine();

				if (input.equalsIgnoreCase("y")) {
					player.setHasJailFree(false);
					player.setInJail(false);
					System.out.println("Used Jail Free Card. You're free!");
					return false; // không skip turn
				}
			}

			System.out.println("Press ENTER to roll dice...");
			scanner.nextLine();

			int dice1 = dice.roll();
			int dice2 = dice.roll();

			System.out.println("Rolled: " + dice1 + " and " + dice2);

			if (dice1 == dice2) {
				System.out.println("🎉 Double! You're free!");
				player.setInJail(false);
				player.move(dice1 + dice2, board.getSize());
				return false;
			} else {
				System.out.println("❌ Not double. Stay in Jail.");
				return true;
			}
		}
		return false;
	}

	private void printPlayerStatus(Player player) {
		System.out.println("===== STATUS =====");
		System.out.println("Name: " + player.getName());
		System.out.println("Money: " + player.getMoney());
		System.out.println("Position: " + player.getPosition());
		System.out.println("==================");
	}

	private int countActivePlayers() {
        int count = 0;
        for (Player p : players) {
            if (p != null && !p.isBankrupt()) {
                count++;
            }
        }
        return count;
    }

    private Player getWinner() {
        for (Player p : players) {
            if (p != null && !p.isBankrupt()) {
                return p;
            }
        }
        return null;
    }

	// ===== Create Game's Board =====
	private void initBoard() {
		Tile[] tiles = new Tile[30];

		// ===== SPECIAL TILES =====
		tiles[0] = new Go("Go", 200);
		tiles[3] = new Chance("Chance");
		tiles[5] = new Tax("Income tax", 100);
		tiles[7] = new FreeParking("Free Parking");
		tiles[10] = new Jail("Jail");
		tiles[14] = new Tax("Luxury tax", 150);
		tiles[17] = new Chance("Chance");
		tiles[21] = new GoToJail("Go to jail");

		// ===== PROPERTY =====
		tiles[1] = new Property("Hồ Hoàn Kiếm", 60, 10);
		tiles[2] = new Property("Phố Cổ Hà Nội", 60, 10);
		tiles[4] = new Property("Hạ Long", 100, 20);
		tiles[6] = new Property("Sapa", 100, 20);
		tiles[8] = new Property("Huế", 120, 25);
		tiles[9] = new Property("Đà Nẵng", 140, 30);
		tiles[11] = new Property("Hội An", 140, 30);
		tiles[12] = new Property("Phong Nha", 160, 35);
		tiles[13] = new Property("Nha Trang", 180, 40);
		tiles[15] = new Property("Đà Lạt", 180, 40);
		tiles[16] = new Property("Buôn Ma Thuột", 200, 45);
		tiles[18] = new Property("TP.HCM", 220, 50);
		tiles[19] = new Property("Cần Thơ", 220, 50);
		tiles[20] = new Property("Vũng Tàu", 240, 55);
		tiles[22] = new Property("Phan Thiết", 260, 60);
		tiles[23] = new Property("Tây Ninh", 260, 60);
		tiles[24] = new Property("Bình Dương", 280, 65);
		tiles[25] = new Property("Đồng Nai", 300, 70);
		tiles[26] = new Property("Long An", 300, 70);
		tiles[27] = new Property("Bắc Ninh", 320, 75);
		tiles[28] = new Property("Hải Phòng", 350, 80);
		tiles[29] = new Property("Cà Mau", 400, 100);

		board = new Board(tiles);
	}

}