package tile;

import model.Player;

public class Property extends Tile {
    private int price;
    private int rent;
    private Player owner;

    public Property(String name, int price, int rent) {
        super(name);
        this.price = price;
        this.rent = rent;
        this.owner = null;
    }

    public int getPrice(){
        return price;
    }

    public String getName() {
        return name;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    @Override
    public void onLand(Player player) {
        java.util.Scanner sc = new java.util.Scanner(System.in);
        if (owner == null) {
            if (player.getMoney() >= price) {
                System.out.println(name + " costs " + price + ", " + "rent fee: " + rent);

                System.out.print(player.getName() + " has " + player.getMoney() + ", buy this property? (y/n): ");
                String choice = sc.nextLine();

                if (choice.equalsIgnoreCase("y")) {
                    System.out.println(player.getName() + " buys " + name);
                    player.pay(price);
                    owner = player;
                    player.addProperty(this);
                } else {
                    System.out.println(player.getName() + " skipped buying " + name);
                }

            } else {
                System.out.println("Not enough money to buy " + name);
            }

        } else if (owner != player) {
            System.out.println(player.getName() + " pays rent " + rent + " to " + owner.getName());
            player.pay(rent);
            owner.receive(rent);

        } else {
            System.out.println(player.getName() + " landed on their own property.");
        }
    }
}