package model;

import java.util.Random;

public class Dice {
    private Random random;

    public Dice() {
        random = new Random();
    }

    // Roll one die (1-6)
    public int roll() {
        return random.nextInt(6) + 1;
    }

    // Optional: roll two dice (1-12)
    public int rollTwoDice() {
        int die1 = roll();
        int die2 = roll();
        System.out.println("Rolled: " + die1 + " + " + die2);
        return die1 + die2;
    }
}
