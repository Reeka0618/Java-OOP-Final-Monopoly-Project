package model;

import java.util.Random;

public class Dice {
    private Random random;
    private int lastDie1;
    private int lastDie2;

    public Dice() {
        random = new Random();
    }

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
}
