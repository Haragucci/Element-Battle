package com.example.demo.services;

import com.example.demo.classes.Account;
import com.example.demo.repositories.AccountRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsService {

    public Map<Integer, Map<String, Object>> stats = new HashMap<>();
    public static final String STATS_FILE_PATH = "files/stats.json";
    private final ObjectMapper mapper = new ObjectMapper();
    private final AccountRepository accountRepository;

    public StatsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Map<String, Object> loadUserStats(int userId) {
        return new HashMap<>(stats.getOrDefault(userId, new HashMap<>()));
    }

    //===============================================REQUEST METHODS===============================================\\

    public ResponseEntity<Map<String, Object>> updateStats(@RequestBody Map<String, Object> statsUpdate) {

        int userId = ((Number) statsUpdate.get("userId")).intValue();
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

        saveUserStats(userId, userStats);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Statistiken aktualisiert",
                "updatedStats", userStats
        ));
    }


    public ResponseEntity<List<Map<String, Object>>> getLeaderboard() {
        List<Map<String, Object>> leaderboard = stats.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> playerStats = new HashMap<>(entry.getValue());
                    int userId = entry.getKey();
                    Account account = accountRepository.getAccountById(userId);
                    if (account != null) {
                        playerStats.put("username", account.username());
                    }
                    playerStats.put("userId", userId);
                    return playerStats;
                })
                .sorted((a, b) -> Integer.compare(
                        ((Number) b.getOrDefault("wins", 0)).intValue(),
                        ((Number) a.getOrDefault("wins", 0)).intValue()
                ))
                .limit(5)
                .collect(Collectors.toList());

        return ResponseEntity.ok(leaderboard);
    }


    public ResponseEntity<Map<String, Object>> getUserStats(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Username ist erforderlich"
            ));
        }

        Account account = accountRepository.getAccountByUsername(username);

        Map<String, Object> userStats = loadUserStats(account.id());
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
    }

    //===============================================HELPING METHODS===============================================\\

    private int getCoinsForUser(int userId) {
        Account account = accountRepository.getAccountById(userId);
        return account != null ? account.coins() : 0;
    }

    private void updateCoinsForUser(int userId, int newCoins) {
        Account account = accountRepository.getAccountById(userId);
        if (account != null) {
            accountRepository.updateAccount(userId, new Account(userId ,account.username(), account.password(), newCoins));
            accountRepository.saveAccounts();
        }
    }

    public boolean checkStats(int userId) {
        return stats.containsKey(userId);
    }

    public void removeStatsAndSave(int userId) {
        stats.remove(userId);
        saveStats();
    }

    public void putStats(int userId, Map<String, Object> statsMap) {
        stats.put(userId, statsMap);
    }

    public Map<String, Object> removeStats(int userId) {
        return stats.remove(userId);
    }

    //===============================================FILE MANAGEMENT===============================================\\

    public void saveStats() {
        try {
            mapper.writeValue(new File(STATS_FILE_PATH), stats);
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern der Statistiken: " + e.getMessage());
        }
    }

    public void saveUserStats(int userId, Map<String, Object> userStats) {
        stats.put(userId, new HashMap<>(userStats));
        saveStats();
    }

    public void loadStats() {
        File file = new File(STATS_FILE_PATH);
        if (file.exists()) {
            try {
                stats = mapper.readValue(file, new TypeReference<>() {});
            } catch (IOException e) {
                stats = new HashMap<>();
            }
        } else {
            stats = new HashMap<>();
        }
    }
}

