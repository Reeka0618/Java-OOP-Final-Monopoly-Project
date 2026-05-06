package model;

import java.util.Random;

public class Dice {
    private Random random;
    private int lastDie1;
    private int lastDie2;

    public Dice() {
        random = new Random();
    }

    // Roll one die (1-6)
    public int roll() {
        return random.nextInt(6) + 1;
    }

    /** Rolls two dice, stores results, returns sum */
    public int rollTwo() {
        lastDie1 = roll();
        lastDie2 = roll();
        return lastDie1 + lastDie2;
    }

    public int getLastDie1() { return lastDie1; }
    public int getLastDie2() { return lastDie2; }

    public boolean isDouble() {
        return lastDie1 == lastDie2;
    }
    // Optional: roll two dice (1-12)
    public int rollTwoDice() {
        int die1 = roll();
        int die2 = roll();
        System.out.println("Rolled: " + die1 + " + " + die2);
        return die1 + die2;
    }
}
