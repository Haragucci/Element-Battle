package com.example.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class AccountService {

    private StatsService statsService;
    private BackgroundService backgroundService;

    @Autowired
    public void setStatsService(@Lazy StatsService statsService,@Lazy BackgroundService backgroundService) {
        this.statsService = statsService;
        this.backgroundService = backgroundService;
    }

    public static final String ACCOUNTS_FILE_PATH = "acc.json";
    private static final String CARDS_FILE_PATH = "cards.json";

    Map<String, String> cardDesigns = new HashMap<>();

    public record Account(String username, String password, int coins) {}

    public Map<String, Account> accounts = new HashMap<>();

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
                statsService.stats.remove(username);
                cardDesigns.remove(username);
                backgroundService.backgrounds.remove(username);
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

        if (backgroundService.backgrounds.containsKey(oldUsername)) {
            String background = backgroundService.backgrounds.remove(oldUsername);
            backgroundService.backgrounds.put(newUsername, background);
            backgroundService.saveBackgrounds();
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

        statsService.saveUserStats(username, newUserStats);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "username", username,
                "coins", 0
        ));
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

    public ResponseEntity<Map<String, Object>> updateCoins(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        int coins = (int) request.get("coins");

        Account account = accounts.get(username);
        if (account != null) {
            Account updatedAccount = new Account(
                    account.username(),
                    account.password(),
                    coins
            );
            accounts.put(username, updatedAccount);
            saveAccounts();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "coins", updatedAccount.coins()
            ));
        }

        return ResponseEntity.ok(Map.of("success", false));
    }

    public ResponseEntity<Map<String, Object>> addCoins(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        int amount = (int) request.get("amount");

        Account account = accounts.get(username);
        if (account != null) {
            Account updatedAccount = new Account(
                    account.username(),
                    account.password(),
                    account.coins() + amount
            );
            accounts.put(username, updatedAccount);
            saveAccounts();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "coins", updatedAccount.coins()
            ));
        }

        return ResponseEntity.ok(Map.of("success", false));
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

}