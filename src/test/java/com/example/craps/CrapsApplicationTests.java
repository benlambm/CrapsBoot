package com.example.craps;

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
