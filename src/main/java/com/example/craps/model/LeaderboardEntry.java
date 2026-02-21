package com.example.craps.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class LeaderboardEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String playerName;
    private int score;
    private String achievements;

    public LeaderboardEntry() {}

    public LeaderboardEntry(String playerName, int score) {
        this(playerName, score, "");
    }

    public LeaderboardEntry(String playerName, int score, String achievements) {
        this.playerName = playerName;
        this.score = score;
        this.achievements = achievements;
    }

    public Long getId() { return id; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getAchievements() { return achievements; }
    public void setAchievements(String achievements) { this.achievements = achievements; }
}
