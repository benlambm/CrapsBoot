package com.example.craps.model;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.*;
import java.util.stream.Collectors;

@Component
@SessionScope
public class GameSession {
    private int bankroll = 100;
    private int point = 0;
    private int wins = 0;
    private int losses = 0;
    private int lastDice1 = 0;
    private int lastDice2 = 0;
    private String message = "Welcome to Craps! Choose your bet and click Roll Dice.";

    // Feature 1: Variable bet sizing
    private int currentBet = 10;

    // Feature 2: Roll history & streaks
    private final List<RollRecord> rollHistory = new ArrayList<>();
    private int currentStreak = 0;

    // Feature 3: Odds bet
    private int oddsBet = 0;

    // Feature 4: Achievements
    private final Set<Achievement> unlockedAchievements = new LinkedHashSet<>();
    private int natural7Wins = 0;
    private boolean wasBelow20 = false;
    private int rollsSincePointSet = 0;

    // ===== Roll Record =====
    public record RollRecord(int dice1, int dice2, int sum, String outcome, boolean win, boolean loss) {
        public String icon() {
            if (win) return "\u2713";
            if (loss) return "\u2717";
            return outcome.equals("POINT_SET") ? "\u2192" : "\u2022";
        }
        public String cssClass() {
            if (win) return "roll-win";
            if (loss) return "roll-loss";
            return outcome.equals("POINT_SET") ? "roll-point" : "roll-continue";
        }
    }

    // ===== Core Game Logic =====
    public void roll(int d1, int d2) {
        if (currentBet > bankroll) currentBet = bankroll;
        if (bankroll <= 0) return;

        this.lastDice1 = d1;
        this.lastDice2 = d2;
        int sum = d1 + d2;
        String outcome;
        boolean isWin = false, isLoss = false;

        if (point == 0) { // Come out roll
            if (sum == 7 || sum == 11) {
                if (sum == 7) natural7Wins++;
                win(sum, "Natural!");
                outcome = "WIN"; isWin = true;
            } else if (sum == 2 || sum == 3 || sum == 12) {
                lose(sum, "Craps!");
                outcome = "LOSS"; isLoss = true;
            } else {
                point = sum;
                rollsSincePointSet = 0;
                message = "Rolled " + sum + ". Point is set to " + point + ". Roll again!";
                outcome = "POINT_SET";
            }
        } else { // Point roll
            rollsSincePointSet++;
            if (sum == point) {
                boolean sniped = rollsSincePointSet == 1;
                win(sum, "Hit the point (" + point + ")!");
                outcome = "WIN"; isWin = true;
                if (sniped) unlockedAchievements.add(Achievement.POINT_SNIPER);
            } else if (sum == 7) {
                lose(sum, "Seven Out!");
                outcome = "LOSS"; isLoss = true;
            } else {
                message = "Rolled " + sum + ". Roll again to hit " + point + ".";
                outcome = "CONTINUE";
            }
        }

        // Track streak
        if (isWin) {
            currentStreak = currentStreak > 0 ? currentStreak + 1 : 1;
        } else if (isLoss) {
            currentStreak = currentStreak < 0 ? currentStreak - 1 : -1;
        }

        // Record roll history (cap at 15)
        rollHistory.add(new RollRecord(d1, d2, sum, outcome, isWin, isLoss));
        if (rollHistory.size() > 15) rollHistory.remove(0);

        // Track comeback state
        if (isLoss && bankroll < 20) wasBelow20 = true;

        // Check achievements
        checkAchievements(d1, d2, isWin);
    }

    private void win(int sum, String baseMsg) {
        int oddsPayout = calculateOddsPayout();
        int totalWin = currentBet + oddsPayout;
        bankroll += totalWin;
        wins++;
        String oddsInfo = oddsBet > 0 ? " (includes $" + oddsPayout + " odds payout)" : "";
        point = 0;
        oddsBet = 0;
        rollsSincePointSet = 0;
        message = "Rolled " + sum + ". " + baseMsg + " Won $" + totalWin + "!" + oddsInfo;
    }

    private void lose(int sum, String baseMsg) {
        int totalLoss = currentBet + oddsBet;
        bankroll -= totalLoss;
        losses++;
        String oddsInfo = oddsBet > 0 ? " (includes $" + oddsBet + " odds bet)" : "";
        point = 0;
        oddsBet = 0;
        rollsSincePointSet = 0;
        message = "Rolled " + sum + ". " + baseMsg + " Lost $" + totalLoss + "." + oddsInfo;
    }

    // ===== Variable Bet Sizing =====
    public void setCurrentBet(int bet) {
        if (point != 0) return;
        this.currentBet = Math.max(5, Math.min(bet, bankroll));
    }

    // ===== Odds Bet =====
    public boolean placeOddsBet(int amount) {
        if (point == 0 || oddsBet > 0) return false;
        int maxOdds = Math.min(currentBet * 3, bankroll);
        this.oddsBet = Math.max(0, Math.min(amount, maxOdds));
        return oddsBet > 0;
    }

    private int calculateOddsPayout() {
        if (oddsBet == 0) return 0;
        return switch (point) {
            case 4, 10 -> oddsBet * 2;
            case 5, 9  -> oddsBet * 3 / 2;
            case 6, 8  -> oddsBet * 6 / 5;
            default -> 0;
        };
    }

    public int getMaxOddsBet() {
        if (point == 0) return 0;
        return Math.min(currentBet * 3, bankroll);
    }

    public String getOddsRatio() {
        return switch (point) {
            case 4, 10 -> "2:1";
            case 5, 9  -> "3:2";
            case 6, 8  -> "6:5";
            default -> "";
        };
    }

    // ===== Achievements =====
    private void checkAchievements(int d1, int d2, boolean isWin) {
        if (isWin && wins == 1) unlockedAchievements.add(Achievement.FIRST_BLOOD);
        if (currentStreak >= 5) unlockedAchievements.add(Achievement.HOT_STREAK);
        if (bankroll >= 500) unlockedAchievements.add(Achievement.HIGH_ROLLER);
        if (natural7Wins >= 3) unlockedAchievements.add(Achievement.LUCKY_7);
        if (wasBelow20 && bankroll >= 200) unlockedAchievements.add(Achievement.COMEBACK_KID);
        if (d1 == 1 && d2 == 1) unlockedAchievements.add(Achievement.SNAKE_EYES);
    }

    public String getAchievementIds() {
        return unlockedAchievements.stream()
                .map(Achievement::name)
                .collect(Collectors.joining(","));
    }

    // ===== Reset =====
    public void reset() {
        bankroll = 100;
        point = 0;
        wins = 0;
        losses = 0;
        lastDice1 = 0;
        lastDice2 = 0;
        currentBet = 10;
        oddsBet = 0;
        currentStreak = 0;
        rollHistory.clear();
        unlockedAchievements.clear();
        natural7Wins = 0;
        wasBelow20 = false;
        rollsSincePointSet = 0;
        message = "Welcome to Craps! Choose your bet and click Roll Dice.";
    }

    // ===== Utility =====
    public String getDiceEmoji(int value) {
        return switch (value) {
            case 1 -> "\u2680"; case 2 -> "\u2681"; case 3 -> "\u2682";
            case 4 -> "\u2683"; case 5 -> "\u2684"; case 6 -> "\u2685";
            default -> "\uD83C\uDFB2";
        };
    }

    // ===== Getters =====
    public int getBankroll() { return bankroll; }
    public int getPoint() { return point; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getLastDice1() { return lastDice1; }
    public int getLastDice2() { return lastDice2; }
    public String getMessage() { return message; }
    public int getCurrentBet() { return currentBet; }
    public int getOddsBet() { return oddsBet; }
    public List<RollRecord> getRollHistory() { return Collections.unmodifiableList(rollHistory); }
    public int getCurrentStreak() { return currentStreak; }
    public int getAbsStreak() { return Math.abs(currentStreak); }
    public boolean isOnWinStreak() { return currentStreak > 0; }
    public boolean isOnLossStreak() { return currentStreak < 0; }
    public Set<Achievement> getUnlockedAchievements() { return Collections.unmodifiableSet(unlockedAchievements); }
}
