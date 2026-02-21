package com.example.craps.model;

public enum Achievement {
    FIRST_BLOOD("First Blood", "Win your first roll", "\uD83E\uDE78"),
    HOT_STREAK("Hot Streak", "Win 5 in a row", "\uD83D\uDD25"),
    HIGH_ROLLER("High Roller", "Reach $500 bankroll", "\uD83D\uDCB0"),
    LUCKY_7("Lucky 7", "Win on natural 7 three times", "\uD83C\uDFB0"),
    POINT_SNIPER("Point Sniper", "Hit the point on the very next roll", "\uD83C\uDFAF"),
    COMEBACK_KID("Comeback Kid", "Recover from below $20 to above $200", "\uD83D\uDCAA"),
    SNAKE_EYES("Snake Eyes", "Roll snake eyes (1+1)", "\uD83D\uDC0D");

    private final String displayName;
    private final String description;
    private final String icon;

    Achievement(String displayName, String description, String icon) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
}
