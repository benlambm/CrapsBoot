package com.example.craps.controller;

import com.example.craps.model.GameSession;
import com.example.craps.model.LeaderboardEntry;
import com.example.craps.repository.LeaderboardRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.concurrent.ThreadLocalRandom;

@Controller
public class CrapsController {

    private final GameSession gameSession;
    private final LeaderboardRepository leaderboardRepository;

    public CrapsController(GameSession gameSession, LeaderboardRepository leaderboardRepository) {
        this.gameSession = gameSession;
        this.leaderboardRepository = leaderboardRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        if (gameSession.getBankroll() <= 0) {
            return "redirect:/game-over";
        }
        model.addAttribute("game", gameSession);
        return "index";
    }

    @PostMapping("/roll")
    public String rollDice() {
        if (gameSession.getBankroll() > 0) {
            int d1 = ThreadLocalRandom.current().nextInt(1, 7);
            int d2 = ThreadLocalRandom.current().nextInt(1, 7);
            gameSession.roll(d1, d2);
        }
        return "redirect:/";
    }

    @GetMapping("/game-over")
    public String gameOver(Model model) {
        model.addAttribute("score", gameSession.getBankroll());
        model.addAttribute("wins", gameSession.getWins());
        return "game-over";
    }

    @PostMapping("/save-score")
    public String saveScore(@RequestParam String playerName) {
        if (playerName != null && !playerName.trim().isEmpty()) {
            LeaderboardEntry entry = new LeaderboardEntry(playerName.trim(), gameSession.getBankroll());
            leaderboardRepository.save(entry);
        }
        gameSession.reset();
        return "redirect:/leaderboard";
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        model.addAttribute("leaders", leaderboardRepository.findTop5ByOrderByScoreDesc());
        return "leaderboard";
    }

    @PostMapping("/reset")
    public String resetGame() {
        gameSession.reset();
        return "redirect:/";
    }
}
