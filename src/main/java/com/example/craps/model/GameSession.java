package com.example.craps.model;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class GameSession {
    private int bankroll = 100;
    private int point = 0;
    private int wins = 0;
    private int losses = 0;
    private int lastDice1 = 0;
    private int lastDice2 = 0;
    private String message = "Welcome to Craps! Click Roll Dice to place your $10 bet.";

    public void roll(int d1, int d2) {
        this.lastDice1 = d1;
        this.lastDice2 = d2;
        int sum = d1 + d2;

        if (point == 0) { // Come out roll
            if (sum == 7 || sum == 11) {
                win(sum, "Natural! You win $10!");
            } else if (sum == 2 || sum == 3 || sum == 12) {
                lose(sum, "Craps! You lose $10.");
            } else {
                point = sum;
                message = "Rolled " + sum + ". Point is set to " + point + ". Roll again!";
            }
        } else { // Point roll
            if (sum == point) {
                win(sum, "Hit the point (" + point + ")! You win $10!");
            } else if (sum == 7) {
                lose(sum, "Seven Out! You lose $10.");
            } else {
                message = "Rolled " + sum + ". Roll again to hit " + point + ".";
            }
        }
    }

    private void win(int sum, String msg) {
        bankroll += 10;
        wins++;
        point = 0;
        message = "Rolled " + sum + ". " + msg;
    }

    private void lose(int sum, String msg) {
        bankroll -= 10;
        losses++;
        point = 0;
        message = "Rolled " + sum + ". " + msg;
    }

    public void reset() {
        bankroll = 100;
        point = 0;
        wins = 0;
        losses = 0;
        lastDice1 = 0;
        lastDice2 = 0;
        message = "Welcome to Craps! Click Roll Dice to place your $10 bet.";
    }

    public String getDiceEmoji(int value) {
        return switch (value) {
            case 1 -> "\u2680"; case 2 -> "\u2681"; case 3 -> "\u2682";
            case 4 -> "\u2683"; case 5 -> "\u2684"; case 6 -> "\u2685";
            default -> "\uD83C\uDFB2";
        };
    }

    // Getters
    public int getBankroll() { return bankroll; }
    public int getPoint() { return point; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getLastDice1() { return lastDice1; }
    public int getLastDice2() { return lastDice2; }
    public String getMessage() { return message; }
}
