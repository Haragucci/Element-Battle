package com.example.ElementBattle.services;

import com.example.ElementBattle.classes.Account;
import com.example.ElementBattle.repositories.AccountRepository;
import com.example.ElementBattle.repositories.StatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    private final StatsRepository statsRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public StatsService(AccountRepository accountRepository, StatsRepository statsRepository) {
        this.accountRepository = accountRepository;
        this.statsRepository = statsRepository;
    }

    public Map<String, Object> loadUserStats(int userId) {
        return statsRepository.getStatsByUserId(userId);
    }

    public ResponseEntity<Map<String, Object>> updateStats(Map<String, Object> statsUpdate) {
        String userName = statsUpdate.get("username").toString();
        try {
            if (accountRepository.accountExistsByUsername(userName)) {
                Account account = accountRepository.getAccountByUsername(userName);
                int userId = account.id();
                int wins = ((Number) statsUpdate.getOrDefault("win", 0)).intValue();
                int losses = ((Number) statsUpdate.getOrDefault("lose", 0)).intValue();
                int damage = ((Number) statsUpdate.getOrDefault("damage", 0)).intValue();
                int directDamage = ((Number) statsUpdate.getOrDefault("directDamage", 0)).intValue();
                int coinsEarned = ((Number) statsUpdate.getOrDefault("coins", 0)).intValue();

                Map<String, Object> userStats = loadUserStats(userId);

                userStats.put("wins", ((Number) userStats.getOrDefault("wins", 0)).intValue() + wins);
                userStats.put("lose", ((Number) userStats.getOrDefault("lose", 0)).intValue() + losses);
                userStats.put("damage", ((Number) userStats.getOrDefault("damage", 0)).intValue() + damage);
                userStats.put("direkt-damage", ((Number) userStats.getOrDefault("direkt-damage", 0)).intValue() + directDamage);

                int currentCoins = getCoinsForUser(userId);
                int newCoins = currentCoins + coinsEarned;
                updateCoinsForUser(userId, newCoins);
                userStats.put("coins", newCoins);

                int totalGames = ((Number) userStats.get("wins")).intValue() + ((Number) userStats.get("lose")).intValue();
                double winrate = totalGames > 0 ? (((Number) userStats.get("wins")).doubleValue() / totalGames) * 100 : 0;
                userStats.put("winrate", winrate);

                statsRepository.updateUserStats(userId, userStats);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Statistiken aktualisiert",
                        "updatedStats", userStats
                ));
            } else {
                return ResponseEntity.ok(Map.of("message", "Account not found!"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("message", "Account not found!"));
        }
    }

    public ResponseEntity<List<Map<String, Object>>> getLeaderboard() {
        Map<Integer, Map<String, Object>> allStats = statsRepository.getAllStats();
        List<Map<String, Object>> leaderboard = new ArrayList<>();

        for (Map.Entry<Integer, Map<String, Object>> entry : allStats.entrySet()) {
            Map<String, Object> playerStats = new HashMap<>(entry.getValue());
            int userId = entry.getKey();

            try {
                if (accountRepository.accountExistsById(userId)) {
                    Account account = accountRepository.getAccountById(userId);
                        playerStats.put("username", account.username());

                } else {
                    playerStats.put("username", "Unbekannt");
                }
            } catch (Exception e) {
                playerStats.put("username", "Fehler beim Abruf");
            }

            playerStats.put("userId", userId);
            leaderboard.add(playerStats);
        }

        leaderboard.sort((a, b) -> Integer.compare(
                ((Number) b.getOrDefault("wins", 0)).intValue(),
                ((Number) a.getOrDefault("wins", 0)).intValue()
        ));

        return ResponseEntity.ok(leaderboard.stream().limit(5).toList());
    }


    public ResponseEntity<Map<String, Object>> getUserStats(Map<String, String> request) {
        String username = request.get("username");
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Username ist erforderlich"
            ));
        }

        try {
            if (accountRepository.accountExistsByUsername(username)) {
                Account account = accountRepository.getAccountByUsername(username);

                Map<String, Object> userStats = statsRepository.getStatsByUserId(account.id());
                if (userStats.isEmpty()) {
                    return ResponseEntity.ok(Map.of(
                            "success", false,
                            "message", "Keine Statistiken für diesen Benutzer gefunden"
                    ));
                }

                int coins = getCoinsForUser(account.id());
                userStats.put("coins", coins);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "stats", userStats
                ));
            } else {
                return ResponseEntity.ok(Map.of("message", false));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }

    }

    private int getCoinsForUser(int userId) {
        try {
            if (accountRepository.accountExistsById(userId)) {
                Account account = accountRepository.getAccountById(userId);
                return account.coins();
            } else {
                return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    private void updateCoinsForUser(int userId, int newCoins) {
        try {
            if (accountRepository.accountExistsById(userId)) {
                Account account = accountRepository.getAccountById(userId);
                accountRepository.updateAccount(new Account(userId, account.username(), account.password(), newCoins));
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}

