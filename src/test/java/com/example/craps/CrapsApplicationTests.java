package com.example.craps;

import com.example.craps.model.Achievement;
import com.example.craps.model.GameSession;
import com.example.craps.model.LeaderboardEntry;
import com.example.craps.repository.LeaderboardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrapsApplicationTests {

    private GameSession session;

    @BeforeEach
    void setUp() {
        session = new GameSession();
    }

    // ===== Original Game Logic Tests =====

    @Test
    void testComeOutRoll_NaturalWins() {
        session.roll(3, 4); // Sum 7
        assertThat(session.getBankroll()).isEqualTo(110);
        assertThat(session.getWins()).isEqualTo(1);
        assertThat(session.getPoint()).isEqualTo(0);

        session.roll(5, 6); // Sum 11
        assertThat(session.getBankroll()).isEqualTo(120);
        assertThat(session.getWins()).isEqualTo(2);
        assertThat(session.getPoint()).isEqualTo(0);
    }

    @Test
    void testComeOutRoll_CrapsLoses() {
        session.roll(1, 1); // Sum 2
        assertThat(session.getBankroll()).isEqualTo(90);
        assertThat(session.getLosses()).isEqualTo(1);

        session.roll(1, 2); // Sum 3
        assertThat(session.getBankroll()).isEqualTo(80);
        assertThat(session.getLosses()).isEqualTo(2);

        session.roll(6, 6); // Sum 12
        assertThat(session.getBankroll()).isEqualTo(70);
        assertThat(session.getLosses()).isEqualTo(3);
    }

    @Test
    void testPointRoll_HitPointWins() {
        session.roll(2, 2); // Sum 4, establishes point
        assertThat(session.getPoint()).isEqualTo(4);
        assertThat(session.getBankroll()).isEqualTo(100);

        session.roll(1, 2); // Sum 3, nothing happens on point roll
        assertThat(session.getPoint()).isEqualTo(4);
        assertThat(session.getBankroll()).isEqualTo(100);

        session.roll(3, 1); // Sum 4, hit point
        assertThat(session.getPoint()).isEqualTo(0);
        assertThat(session.getBankroll()).isEqualTo(110);
        assertThat(session.getWins()).isEqualTo(1);
    }

    @Test
    void testPointRoll_SevenOutLoses() {
        session.roll(4, 5); // Sum 9, establishes point
        assertThat(session.getPoint()).isEqualTo(9);

        session.roll(3, 4); // Sum 7, Seven Out
        assertThat(session.getPoint()).isEqualTo(0);
        assertThat(session.getBankroll()).isEqualTo(90);
        assertThat(session.getLosses()).isEqualTo(1);
    }

    // ===== Feature 1: Variable Bet Sizing =====

    @Test
    void testVariableBet_CustomBetAffectsBankroll() {
        session.setCurrentBet(25);
        session.roll(3, 4); // Natural 7, win
        assertThat(session.getBankroll()).isEqualTo(125);
    }

    @Test
    void testVariableBet_BetCappedAtBankroll() {
        session.setCurrentBet(200);
        assertThat(session.getCurrentBet()).isEqualTo(100);
    }

    @Test
    void testVariableBet_CannotChangeBetDuringPointPhase() {
        session.roll(2, 2); // Point set to 4
        session.setCurrentBet(50);
        assertThat(session.getCurrentBet()).isEqualTo(10);
    }

    @Test
    void testVariableBet_MinimumBet() {
        session.setCurrentBet(1);
        assertThat(session.getCurrentBet()).isEqualTo(5);
    }

    // ===== Feature 2: Roll History & Streaks =====

    @Test
    void testRollHistory_RecordsRolls() {
        session.roll(3, 4); // Natural 7, win
        assertThat(session.getRollHistory()).hasSize(1);
        assertThat(session.getRollHistory().get(0).sum()).isEqualTo(7);
        assertThat(session.getRollHistory().get(0).win()).isTrue();
    }

    @Test
    void testRollHistory_CappedAt15() {
        for (int i = 0; i < 20; i++) {
            session.roll(3, 4); // Natural 7
        }
        assertThat(session.getRollHistory()).hasSize(15);
    }

    @Test
    void testRollHistory_TracksOutcomes() {
        session.roll(2, 2); // Point set to 4
        assertThat(session.getRollHistory().get(0).outcome()).isEqualTo("POINT_SET");

        session.roll(1, 2); // Continue (sum 3, point is 4)
        assertThat(session.getRollHistory().get(1).outcome()).isEqualTo("CONTINUE");

        session.roll(1, 3); // Hit point 4, win
        assertThat(session.getRollHistory().get(2).outcome()).isEqualTo("WIN");
    }

    @Test
    void testStreak_WinStreak() {
        session.roll(3, 4); // Win
        session.roll(5, 6); // Win
        session.roll(3, 4); // Win
        assertThat(session.getCurrentStreak()).isEqualTo(3);
        assertThat(session.isOnWinStreak()).isTrue();
    }

    @Test
    void testStreak_LossStreak() {
        session.roll(1, 1); // Loss
        session.roll(1, 2); // Loss
        assertThat(session.getCurrentStreak()).isEqualTo(-2);
        assertThat(session.isOnLossStreak()).isTrue();
    }

    @Test
    void testStreak_PointSetDoesNotBreakStreak() {
        session.roll(3, 4); // Win
        session.roll(2, 2); // Point set (no win/loss)
        assertThat(session.getCurrentStreak()).isEqualTo(1);
    }

    @Test
    void testRollHistory_ClearedOnReset() {
        session.roll(3, 4);
        session.reset();
        assertThat(session.getRollHistory()).isEmpty();
        assertThat(session.getCurrentStreak()).isEqualTo(0);
    }

    // ===== Feature 3: Odds Bet =====

    @Test
    void testOddsBet_CannotPlaceWithoutPoint() {
        boolean placed = session.placeOddsBet(10);
        assertThat(placed).isFalse();
        assertThat(session.getOddsBet()).isEqualTo(0);
    }

    @Test
    void testOddsBet_PlacedAfterPoint() {
        session.roll(2, 2); // Point 4
        boolean placed = session.placeOddsBet(10);
        assertThat(placed).isTrue();
        assertThat(session.getOddsBet()).isEqualTo(10);
    }

    @Test
    void testOddsBet_LimitedTo3xPassLine() {
        session.roll(2, 2); // Point 4, default bet $10
        session.placeOddsBet(50); // Exceeds 3x limit (30)
        assertThat(session.getOddsBet()).isEqualTo(30);
    }

    @Test
    void testOddsBet_PayoutPoint4() {
        // Point 4 pays 2:1 on odds
        session.roll(2, 2); // Point 4
        session.placeOddsBet(10);
        session.roll(1, 3); // Hit point 4
        // Win: $10 pass line + $20 odds payout (10 * 2)
        assertThat(session.getBankroll()).isEqualTo(130);
    }

    @Test
    void testOddsBet_PayoutPoint5() {
        // Point 5 pays 3:2 on odds
        session.roll(2, 3); // Point 5
        session.placeOddsBet(10);
        session.roll(1, 4); // Hit point 5
        // Win: $10 pass line + $15 odds payout (10 * 3/2)
        assertThat(session.getBankroll()).isEqualTo(125);
    }

    @Test
    void testOddsBet_PayoutPoint6() {
        // Point 6 pays 6:5 on odds
        session.roll(2, 4); // Point 6
        session.placeOddsBet(10);
        session.roll(1, 5); // Hit point 6
        // Win: $10 pass line + $12 odds payout (10 * 6/5)
        assertThat(session.getBankroll()).isEqualTo(122);
    }

    @Test
    void testOddsBet_LostOnSevenOut() {
        session.roll(2, 2); // Point 4
        session.placeOddsBet(10);
        session.roll(3, 4); // Seven out
        // Lose: $10 pass line + $10 odds bet
        assertThat(session.getBankroll()).isEqualTo(80);
    }

    @Test
    void testOddsBet_ResetsAfterResolution() {
        session.roll(2, 2); // Point 4
        session.placeOddsBet(10);
        session.roll(1, 3); // Hit point
        assertThat(session.getOddsBet()).isEqualTo(0);
    }

    // ===== Feature 4: Achievements =====

    @Test
    void testAchievement_FirstBlood() {
        session.roll(3, 4); // First win
        assertThat(session.getUnlockedAchievements()).contains(Achievement.FIRST_BLOOD);
    }

    @Test
    void testAchievement_SnakeEyes() {
        session.roll(1, 1); // Snake eyes
        assertThat(session.getUnlockedAchievements()).contains(Achievement.SNAKE_EYES);
    }

    @Test
    void testAchievement_HotStreak() {
        for (int i = 0; i < 5; i++) {
            session.roll(3, 4); // Win natural 7
        }
        assertThat(session.getUnlockedAchievements()).contains(Achievement.HOT_STREAK);
    }

    @Test
    void testAchievement_HotStreak_NotEarlyUnlock() {
        for (int i = 0; i < 4; i++) {
            session.roll(3, 4); // Win natural 7
        }
        assertThat(session.getUnlockedAchievements()).doesNotContain(Achievement.HOT_STREAK);
    }

    @Test
    void testAchievement_PointSniper() {
        session.roll(2, 2); // Point set to 4
        session.roll(1, 3); // Hit point on very next roll
        assertThat(session.getUnlockedAchievements()).contains(Achievement.POINT_SNIPER);
    }

    @Test
    void testAchievement_PointSniper_NotAfterMultipleRolls() {
        session.roll(2, 2); // Point set to 4
        session.roll(1, 2); // Continue (sum 3)
        session.roll(1, 3); // Hit point on second roll
        assertThat(session.getUnlockedAchievements()).doesNotContain(Achievement.POINT_SNIPER);
    }

    @Test
    void testAchievement_Lucky7() {
        session.roll(3, 4); // Natural 7 win #1
        session.roll(3, 4); // Natural 7 win #2
        assertThat(session.getUnlockedAchievements()).doesNotContain(Achievement.LUCKY_7);
        session.roll(3, 4); // Natural 7 win #3
        assertThat(session.getUnlockedAchievements()).contains(Achievement.LUCKY_7);
    }

    @Test
    void testAchievement_ClearedOnReset() {
        session.roll(3, 4); // First win
        assertThat(session.getUnlockedAchievements()).isNotEmpty();
        session.reset();
        assertThat(session.getUnlockedAchievements()).isEmpty();
    }

    @Test
    void testAchievementIds_ForPersistence() {
        session.roll(1, 1); // Snake eyes + loss
        session.roll(3, 4); // First blood (first win)
        String ids = session.getAchievementIds();
        assertThat(ids).contains("SNAKE_EYES");
        assertThat(ids).contains("FIRST_BLOOD");
    }
}

@DataJpaTest
class LeaderboardRepositoryTest {

    @Autowired
    private LeaderboardRepository repository;

    @Test
    void testFindTop5() {
        repository.save(new LeaderboardEntry("Player1", 100));
        repository.save(new LeaderboardEntry("Player2", 50));
        repository.save(new LeaderboardEntry("Player3", 200));
        repository.save(new LeaderboardEntry("Player4", 300));
        repository.save(new LeaderboardEntry("Player5", 150));
        repository.save(new LeaderboardEntry("Player6", 10));

        List<LeaderboardEntry> top5 = repository.findTop5ByOrderByScoreDesc();

        assertThat(top5).hasSize(5);
        assertThat(top5.get(0).getScore()).isEqualTo(300);
        assertThat(top5.get(0).getPlayerName()).isEqualTo("Player4");
        assertThat(top5.get(4).getScore()).isEqualTo(50); // 50 is 5th place
    }
}
