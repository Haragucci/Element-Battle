package com.example.demo.Services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

    //===============================================SERVICE INTEGRATION===============================================\\

    private final AccountService accountService;

    @Autowired
    public StatsService(AccountService accountService) {
        this.accountService = accountService;
    }


    //===============================================VARIABLES===============================================\\

    public Map<String, Map<String, Object>> stats = new HashMap<>();
    public static final String STATS_FILE_PATH = "files/stats.json";
    public Map<String, Object> loadUserStats(String username) {return new HashMap<>(stats.getOrDefault(username, new HashMap<>()));}
    private final ObjectMapper mapper = new ObjectMapper();


    //===============================================REQUEST METHODS===============================================\\

    public ResponseEntity<Map<String, Object>> updateStats(@RequestBody Map<String, Object> statsUpdate) {

        String username = (String) statsUpdate.get("username");
        int wins = ((Number) statsUpdate.getOrDefault("win", 0)).intValue();
        int losses = ((Number) statsUpdate.getOrDefault("lose", 0)).intValue();
        int damage = ((Number) statsUpdate.getOrDefault("damage", 0)).intValue();
        int directDamage = ((Number) statsUpdate.getOrDefault("directDamage", 0)).intValue();
        int coinsEarned = ((Number) statsUpdate.getOrDefault("coins", 0)).intValue();

        Map<String, Object> userStats = loadUserStats(username);

        userStats.put("wins", ((Number) userStats.getOrDefault("wins", 0)).intValue() + wins);
        userStats.put("lose", ((Number) userStats.getOrDefault("lose", 0)).intValue() + losses);
        userStats.put("damage", ((Number) userStats.getOrDefault("damage", 0)).intValue() + damage);
        userStats.put("direkt-damage", ((Number) userStats.getOrDefault("direkt-damage", 0)).intValue() + directDamage);

        int currentCoins = getCoinsForUser(username);
        int newCoins = currentCoins + coinsEarned;
        updateCoinsForUser(username, newCoins);
        userStats.put("coins", newCoins);

        int totalGames = ((Number) userStats.get("wins")).intValue() + ((Number) userStats.get("lose")).intValue();
        double winrate = totalGames > 0 ? (((Number) userStats.get("wins")).doubleValue() / totalGames) * 100 : 0;
        userStats.put("winrate", winrate);

        saveUserStats(username, userStats);

        return ResponseEntity.ok(Map.of("success", true, "message", "Statistiken aktualisiert", "updatedStats", userStats));
    }

    public ResponseEntity<List<Map<String, Object>>> getLeaderboard() {
        List<Map<String, Object>> leaderboard = stats.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> playerStats = new HashMap<>(entry.getValue());
                    playerStats.put("username", entry.getKey());
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
                    "message", "Benutzername ist erforderlich"
            ));
        }

        Map<String, Object> userStats = loadUserStats(username);
        if (userStats.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Keine Statistiken für diesen Benutzer gefunden"
            ));
        }

        int coins = getCoinsForUser(username);
        userStats.put("coins", coins);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "stats", userStats
        ));
    }



    //===============================================HELPING METHODS===============================================\\

    private int getCoinsForUser(String username) {
        AccountService.Account account = accountService.accounts.get(username);
        return account != null ? account.coins() : 0;
    }

    private void updateCoinsForUser(String username, int newCoins) {
        AccountService.Account account = accountService.accounts.get(username);
        if (account != null) {
            accountService.accounts.put(username, new AccountService.Account(username, account.password(), newCoins));
            accountService.saveAccounts();
        }
    }


    //===============================================FILE MANAGEMENT===============================================\\

    public void saveStats() {
        try {
            mapper.writeValue(new File(STATS_FILE_PATH), stats);
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern der Statistiken: " + e.getMessage());
        }
    }

    public void saveUserStats(String username, Map<String, Object> userStats) {
        stats.put(username, new HashMap<>(userStats));
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
