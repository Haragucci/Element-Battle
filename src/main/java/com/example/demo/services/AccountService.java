package com.example.demo.services;

import com.example.demo.classes.Account;
import com.example.demo.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;

@Service
public class AccountService {

    //===============================================SERVICE INTEGRATION===============================================\\

    private final AccountRepository accountRepository;
    private final StatsService statsService;
    private final BackgroundService backgroundService;
    private final CardService cardService;


    @Autowired
    public AccountService(AccountRepository accountRepository, StatsService statsService, BackgroundService backgroundService,CardService cardService) {
        this.accountRepository = accountRepository;
        this.statsService = statsService;
        this.backgroundService = backgroundService;
        this.cardService = cardService;
    }

    //===============================================REQUEST METHODS===============================================\\

    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> user) {
        String username = user.get("username");
        String password = user.get("password");
        Account account = accountRepository.getAccount(username);
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

        /* FIXME
        try {
            File accountFile = new File(ACCOUNTS_FILE_PATH);
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<Map<String, Map<String, Object>>> typeRef = new TypeReference<>() {};
            Map<String, Map<String, Object>> users = mapper.readValue(accountFile, typeRef);

            if (users.containsKey(username)) {
                users.remove(username);
                mapper.writeValue(accountFile, users);

                accountRepository.removeAccount(username);
                statsService.stats.remove(username);
                cardService.cardDesigns.remove(username);
                backgroundService.backgrounds.remove(username);
                accountRepository.saveAccounts();

                return ResponseEntity.ok("User deleted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }

         */
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    public ResponseEntity<Map<String, Object>> updateAccount(@RequestBody Map<String, String> updateData) {
        String oldUsername = updateData.get("oldUsername");
        String newUsername = updateData.get("newUsername");
        String newPassword = updateData.get("newPassword");

        Account account = accountRepository.getAccount(oldUsername);
        if (account == null) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Benutzer nicht gefunden"
            ));
        }

        if (!oldUsername.equals(newUsername) && accountRepository.hasAccount(newUsername)) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Neuer Benutzername bereits vergeben"
            ));
        }

        accountRepository.removeAccount(oldUsername);
        Account updatedAccount = new Account(
                newUsername,
                newPassword != null ? newPassword : account.password(),
                account.coins()
        );
        accountRepository.updateAccount(newUsername, updatedAccount);
        accountRepository.saveAccounts();

        if (backgroundService.backgrounds.containsKey(oldUsername)) {
            String background = backgroundService.backgrounds.remove(oldUsername);
            backgroundService.backgrounds.put(newUsername, background);
            backgroundService.saveBackgrounds();
        }

        if (cardService.cardDesigns.containsKey(oldUsername)) {
            String cardDesign = cardService.cardDesigns.remove(oldUsername);
            cardService.cardDesigns.put(newUsername, cardDesign);
            cardService.saveCardDesigns();
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Kontoinformationen erfolgreich aktualisiert"
        ));
    }

    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> user) {
        String username = user.get("username");
        String password = user.get("password");

        if (accountRepository.hasAccount(username)) {
            Account existingAccount = accountRepository.getAccount(username);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Benutzername existiert bereits",
                    "username", username,
                    "coins", existingAccount.coins()
            ));
        }

        Account newAccount = new Account(username, password, 0);
        accountRepository.updateAccount(username, newAccount);
        accountRepository.saveAccounts();

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

    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestBody Map<String, String> request) {
        String username = request.get("username");

        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Kein Benutzername angegeben"
            ));
        }

        Account account = accountRepository.getAccount(username);
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

        Account account = accountRepository.getAccount(username);
        if (account != null) {
            Account updatedAccount = new Account(
                    account.username(),
                    account.password(),
                    coins
            );
            accountRepository.updateAccount(username, updatedAccount);
            accountRepository.saveAccounts();

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

        Account account = accountRepository.getAccount(username);
        if (account != null) {
            Account updatedAccount = new Account(
                    account.username(),
                    account.password(),
                    account.coins() + amount
            );
            accountRepository.updateAccount(username, updatedAccount);
            accountRepository.saveAccounts();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "coins", updatedAccount.coins()
            ));
        }

        return ResponseEntity.ok(Map.of("success", false));
    }
}