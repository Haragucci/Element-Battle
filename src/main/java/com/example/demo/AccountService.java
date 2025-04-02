package com.example.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AccountService {

    public static final String BACKGROUNDS_FILE_PATH = "back.json";
    public static final String ACCOUNTS_FILE_PATH = "acc.json";
    public static final String STATS_FILE_PATH = "stats.json";
    private static final String CARDS_FILE_PATH = "cards.json";

    Map<String, String> cardDesigns = new HashMap<>();

    public record Account(String username, String password, int coins) {}

    public Map<String, Object> loadUserStats(String username) {return new HashMap<>(stats.getOrDefault(username, new HashMap<>()));}
    private Map<String, Account> accounts = new HashMap<>();
    public Map<String, Map<String, Object>> stats = new HashMap<>();


    public final Map<String, String> backgrounds = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();



    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> user) {
        String username = user.get("username");
        String password = user.get("password");
        Account account = accounts.get(username);
        if (account != null && account.password().equals(password)) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "username", username,
                    "coins", account.coins()
            ));
        }
        return ResponseEntity.ok(Map.of("success", false));
    }

    public ResponseEntity<String> deleteUser(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body("Username is required");
        }

        try {
            File accountFile = new File(ACCOUNTS_FILE_PATH);
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<Map<String, Map<String, Object>>> typeRef = new TypeReference<>() {};
            Map<String, Map<String, Object>> users = mapper.readValue(accountFile, typeRef);

            if (users.containsKey(username)) {
                users.remove(username);
                mapper.writeValue(accountFile, users);

                accounts.remove(username);
                stats.remove(username);
                cardDesigns.remove(username);
                backgrounds.remove(username);
                saveAccounts();

                return ResponseEntity.ok("User deleted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    public ResponseEntity<Map<String, Object>> updateAccount(@RequestBody Map<String, String> updateData) {
        String oldUsername = updateData.get("oldUsername");
        String newUsername = updateData.get("newUsername");
        String newPassword = updateData.get("newPassword");

        Account account = accounts.get(oldUsername);
        if (account == null) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Benutzer nicht gefunden"
            ));
        }

        if (!oldUsername.equals(newUsername) && accounts.containsKey(newUsername)) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Neuer Benutzername bereits vergeben"
            ));
        }

        accounts.remove(oldUsername);
        Account updatedAccount = new Account(
                newUsername,
                newPassword != null ? newPassword : account.password(),
                account.coins()
        );
        accounts.put(newUsername, updatedAccount);
        saveAccounts();

        if (backgrounds.containsKey(oldUsername)) {
            String background = backgrounds.remove(oldUsername);
            backgrounds.put(newUsername, background);
            saveBackgrounds();
        }

        if (cardDesigns.containsKey(oldUsername)) {
            String cardDesign = cardDesigns.remove(oldUsername);
            cardDesigns.put(newUsername, cardDesign);
            saveCardDesigns();
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Kontoinformationen erfolgreich aktualisiert"
        ));
    }

    public ResponseEntity<Map<String, Object>> toggleCardDesign(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String designId = request.get("designId");

        if (cardDesigns.containsKey(username)) {
            cardDesigns.put(username, designId);
            saveCardDesigns();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "activeDesign", cardDesigns.get(username)
            ));
        } else {
            return ResponseEntity.ok(Map.of("success", false, "message", "Benutzer hat keine Kartendesigns gekauft"));
        }
    }

    public ResponseEntity<Map<String, Object>> buyCardDesign(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        final int COST = 2;

        synchronized (this) {
            Account account = accounts.get(username);
            if (account != null) {
                if (account.coins() >= COST) {
                    Account updatedAccount = new Account(
                            account.username(),
                            account.password(),
                            account.coins() - COST
                    );
                    accounts.put(username, updatedAccount);
                    saveAccounts();

                    System.out.println("Benutzer: " + username);
                    System.out.println("Münzen vor dem Kauf: " + account.coins());
                    System.out.println("Münzen nach dem Kauf: " + updatedAccount.coins());

                    if (!cardDesigns.containsKey(username)) {
                        cardDesigns.put(username, "default");
                        saveCardDesigns();
                    }
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "coins", updatedAccount.coins(),
                            "activeDesign", cardDesigns.get(username)
                    ));
                } else {
                    System.out.println("Nicht genug Münzen für Benutzer: " + username + ". Aktueller Stand: " + account.coins());
                    return ResponseEntity.ok(Map.of("success", false, "message", "Nicht genug Münzen"));
                }
            }

            System.out.println("Benutzer nicht gefunden: " + username);
            return ResponseEntity.ok(Map.of("success", false, "message", "Benutzer nicht gefunden"));
        }
    }

    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> user) {
        String username = user.get("username");
        String password = user.get("password");

        if (accounts.containsKey(username)) {
            Account existingAccount = accounts.get(username);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Benutzername existiert bereits",
                    "username", username,
                    "coins", existingAccount.coins
            ));
        }

        Account newAccount = new Account(username, password, 0);
        accounts.put(username, newAccount);
        saveAccounts();

        Map<String, Object> newUserStats = new HashMap<>();
        newUserStats.put("wins", 0);
        newUserStats.put("coins", 0);
        newUserStats.put("damage", 0);
        newUserStats.put("direkt-damage", 0);
        newUserStats.put("lose", 0);
        newUserStats.put("winrate", 0.0);

        saveUserStats(username, newUserStats);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "username", username,
                "coins", 0
        ));
    }

    public ResponseEntity<Map<String, Object>> buyBackground(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String background = (String) request.get("background");
        int cost = (int) request.get("cost");

        Account account = accounts.get(username);
        if (account != null) {
            if (account.coins() >= cost) {
                Account updatedAccount = new Account(
                        account.username(),
                        account.password(),
                        account.coins() - cost
                );
                accounts.put(username, updatedAccount);
                saveAccounts();

                backgrounds.put(username, background);
                saveBackgrounds();

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "coins", updatedAccount.coins(),
                        "background", background
                ));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "Nicht genug Münzen"));
            }
        }

        return ResponseEntity.ok(Map.of("success", false, "message", "Benutzer nicht gefunden"));
    }

    public ResponseEntity<Map<String, Object>> checkUserBackground(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String background = backgrounds.get(username);
        boolean exists = background != null;

        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        if (exists) {
            response.put("activeBackground", background);
        }

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> hasBackground(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String background = request.get("background");

        if (username == null || background == null) {
            return ResponseEntity.badRequest().body(Map.of("hasBackground", false));
        }

        boolean hasBackground = backgrounds.containsKey(username);
        boolean isActive = hasBackground && backgrounds.get(username).equals(background);

        return ResponseEntity.ok(Map.of(
                "hasBackground", hasBackground,
                "isActive", isActive
        ));
    }

    public ResponseEntity<Map<String, Object>> toggleBackground(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String background = (String) request.get("background");
        if (backgrounds.containsKey(username)) {
            String currentBackground = backgrounds.get(username);
            boolean isActive = currentBackground != null && currentBackground.equals(background);
            if (isActive) {
                backgrounds.put(username, "");
            } else {
                backgrounds.put(username, background);
            }
            saveBackgrounds();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "isActive", !isActive
            ));
        } else {
            return ResponseEntity.ok(Map.of("success", false, "message", "Benutzer hat keinen Hintergrund"));
        }
    }

    public ResponseEntity<Map<String, Object>> checkUserCardDesign(@RequestBody Map<String, String> request) {
        String username = request.get("username");

        boolean purchased = cardDesigns.containsKey(username);
        String activeDesign = cardDesigns.getOrDefault(username, "");

        return ResponseEntity.ok(Map.of(
                "purchased", purchased,
                "activeDesign", activeDesign
        ));
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

    public ResponseEntity<Map<String, Object>> getBackground(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");

        if (backgrounds.containsKey(username)) {
            String background = backgrounds.get(username);
            return ResponseEntity.ok(Map.of("success", true, "background", background));
        } else {
            return ResponseEntity.ok(Map.of("success", false, "message", "Kein Hintergrund für diesen Benutzer gefunden"));
        }
    }

    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestBody Map<String, String> request) {
        String username = request.get("username");

        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Kein Benutzername angegeben"
            ));
        }

        Account account = accounts.get(username);
        if (account == null) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Benutzer nicht gefunden"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "username", account.username(),
                "password", account.password(),
                "coins", account.coins()
        ));
    }

    public int getCoinsForUser(String username) {
        Account account = accounts.get(username);
        return account != null ? account.coins() : 0;
    }

    public void updateCoinsForUser(String username, int newCoins) {
        Account account = accounts.get(username);
        if (account != null) {
            accounts.put(username, new Account(username, account.password(), newCoins));
            saveAccounts();
        }
    }

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

    public void loadAccounts() {
        try {
            File file = new File(ACCOUNTS_FILE_PATH);
            if (file.exists()) {
                accounts = mapper.readValue(file, new TypeReference<>() {});
            } else {
                System.out.println("acc.json Datei existiert nicht.");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void saveAccounts() {
        try {
            mapper.writeValue(new File(ACCOUNTS_FILE_PATH), accounts);
        } catch (IOException e) {
            System.out.println("Fehler beim speichern der Accounts");
        }
    }

    public void loadBackgrounds() {
        try {
            File file = new File(BACKGROUNDS_FILE_PATH);
            if (file.exists()) {
                backgrounds.putAll(mapper.readValue(file, new TypeReference<Map<String, String>>() {}));
            }
        } catch (IOException e) {
            System.out.println("Fehler beim laden der Hintergründe!");
        }
    }

    public void saveBackgrounds() {
        try {
            mapper.writeValue(new File(BACKGROUNDS_FILE_PATH), backgrounds);
        } catch (IOException e) {
            System.out.println("Fehler beim speichern der Hintergründe!");
        }
    }

    public void loadCardDesigns() {
        try {
            File file = new File(CARDS_FILE_PATH);
            if (file.exists()) {
                cardDesigns.putAll(mapper.readValue(file, new TypeReference<Map<String, String>>() {}));
            }
        } catch (IOException e) {
            System.out.println("Fehler beim laden der Kartendesigns");
        }
    }

    public void saveCardDesigns() {
        try {
            mapper.writeValue(new File(CARDS_FILE_PATH), cardDesigns);
        } catch (IOException e) {
            System.out.println("Fehler beim speichern der Kartendesigns");
        }
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