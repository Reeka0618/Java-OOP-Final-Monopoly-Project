package tile;

import model.Player;

public class Property extends Tile {
    private int price;
    private int rent;
    private Player owner;

    // GUI callback: when landing on an unowned property, the GUI will ask via dialog
    // We store a pending purchase state that GameEngine checks
    private boolean pendingPurchaseOffer = false;
    private Player pendingBuyer = null;

    public Property(String name, int price, int rent) {
        super(name);
        this.price = price;
        this.rent = rent;
        this.owner = null;
    }

    public int getPrice() { return price; }
    public int getRent() { return rent; }
    public Player getOwner() { return owner; }
    public void setOwner(Player owner) { this.owner = owner; }

    @Override
    public void onLand(Player player, java.util.function.Consumer<String> log) {
        if (owner == null) {
            if (player.getMoney() >= price) {
                pendingPurchaseOffer = true;
                pendingBuyer = player;
                log.accept("🏠 " + name + " is unowned. Price: $" + price + " | Rent: $" + rent);
                log.accept("💬 Use the BUY button to purchase, or SKIP to pass.");
            } else {
                log.accept("💸 " + player.getName() + " can't afford " + name + " ($" + price + ")");
            }
        } else if (owner != player) {
            log.accept("💰 " + player.getName() + " pays $" + rent + " rent to " + owner.getName());
            player.pay(rent);
            owner.receive(rent);
        } else {
            log.accept("🏡 " + player.getName() + " landed on their own property: " + name);
        }
    }

    public boolean hasPendingOffer() { return pendingPurchaseOffer; }
    public Player getPendingBuyer() { return pendingBuyer; }

    public void acceptPurchase() {
        if (pendingBuyer != null) {
            pendingBuyer.pay(price);
            owner = pendingBuyer;
            pendingBuyer.addProperty(this);
        }
        clearPending();
    }

    public void declinePurchase() {
        clearPending();
    }

    private void clearPending() {
        pendingPurchaseOffer = false;
        pendingBuyer = null;
    }
}
