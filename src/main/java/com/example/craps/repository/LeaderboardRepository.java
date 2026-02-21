package com.example.craps.repository;

import com.example.craps.model.LeaderboardEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeaderboardRepository extends JpaRepository<LeaderboardEntry, Long> {
    List<LeaderboardEntry> findTop5ByOrderByScoreDesc();
}
