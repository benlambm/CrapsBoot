# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

This is a Maven-based Spring Boot project (Java 21).

- **Build:** `./mvnw clean package` (or `mvn clean package`)
- **Run:** `./mvnw spring-boot:run` (serves on default port 8080)
- **Run all tests:** `./mvnw test`
- **Run a single test class:** `./mvnw test -Dtest=CrapsApplicationTests`
- **Run a single test method:** `./mvnw test -Dtest=CrapsApplicationTests#testComeOutRoll_NaturalWins`

Note: There is no Maven wrapper checked in yet; use `mvn` directly or add a wrapper with `mvn wrapper:wrapper`.

## Architecture

Spring Boot 3.4 web app implementing a simplified casino craps game with server-side rendering.

**Runtime stack:** Spring MVC + Thymeleaf templates + JPA with an in-memory H2 database. No separate database setup required. H2 console is available at `/h2-console`.

**Key design decisions:**
- `GameSession` is a `@SessionScope` Spring component — each user's HTTP session gets its own game state (bankroll, point, wins/losses, last dice roll). All game logic (come-out roll vs point roll, win/loss resolution) lives here, not in the controller.
- `CrapsController` is a traditional Spring MVC `@Controller` using POST-redirect-GET pattern. Dice rolls happen via `POST /roll` which redirects to `GET /`. When bankroll hits zero, the player is redirected to `/game-over`.
- `LeaderboardEntry` is a JPA `@Entity` persisted to H2. The repository uses a Spring Data derived query method (`findTop5ByOrderByScoreDesc`) to fetch the top 5 scores.

**Request flow:** `GET /` renders the game board from session state. `POST /roll` generates random dice, delegates to `GameSession.roll()`, redirects back. `POST /save-score` persists to leaderboard and resets the session. `POST /reset` resets session state.

**Test structure:** Tests are in a single file (`CrapsApplicationTests.java`) containing two test classes — `CrapsApplicationTests` (unit tests for `GameSession` game logic) and `LeaderboardRepositoryTest` (a `@DataJpaTest` for the repository query).
