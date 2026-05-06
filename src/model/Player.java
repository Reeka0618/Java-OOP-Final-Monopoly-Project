package model;

import java.util.ArrayList;

import tile.Property;

public class Player {
    private String name;
    private int money;
    private int position;
    private ArrayList<Property> properties = new ArrayList<>();
    private boolean inJail = false;
    public static final int JAIL_POSITION = 10;
    private boolean jailFreeCard;
    private boolean bankrupt = false;

    // Token colors for GUI display
    private java.awt.Color tokenColor;

    public Player(String name, java.awt.Color tokenColor) {
        this.name = name;
        this.money = 1500;
        this.position = 0;
        this.tokenColor = tokenColor;
    }

    public void move(int steps, int boardSize) {
        int newPos = (position + steps) % boardSize;
        // Passed GO
        if (steps > 0 && newPos < position) {
            receive(200);
        }
        position = newPos;
    }
    public Player(String name) {
        this.name = name;
        this.money = 100; // tiền khởi đầu
        this.position = 0;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void pay(int amount) {
        money -= amount;
        System.out.println(name + " pays " + amount);

        if (money < 0) {
            bankrupt = true;
            return;
        }
    }

    public void receive(int amount) {
        money += amount;
    }

    public java.awt.Color getTokenColor() { return tokenColor; }

    public boolean isBankrupt() {
        return bankrupt;
    }

    // getters
    public int getPosition() {
        return position;
    }

    public int getMoney() {
        return money;
    }

    public String getName() {
        return name;
    }

    public void addProperty(Property p) {
    properties.add(p);
    }

    public ArrayList<Property> getProperties() {
        return properties;
    }

    public boolean isInJail() {
        return inJail;
    }

    public void setInJail(boolean status) {
        inJail = status;
    }

    public boolean hasJailFreeCard() {
        return jailFreeCard;
    }

    public void setHasJailFree(boolean hasJailFree) {
        this.jailFreeCard = hasJailFree;
    }

    public boolean hasProperties() {
        return !properties.isEmpty();
    }
}
