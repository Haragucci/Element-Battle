package com.example.demo.services;

import com.example.demo.classes.Account;
import com.example.demo.repositories.*;
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
    private final StatsRepository statsRepository;
    private final BackgroundRepository backgroundRepository;
    private final CardRepository cardRepository;
    private final GameRepository gameRepository;


    @Autowired
    public AccountService(AccountRepository accountRepository, StatsRepository statsRepository, BackgroundRepository backgroundRepository, CardRepository cardRepository, GameRepository gameRepository) {
        this.accountRepository = accountRepository;
        this.statsRepository = statsRepository;
        this.backgroundRepository = backgroundRepository;
        this.cardRepository = cardRepository;
        this.gameRepository = gameRepository;
    }

    //===============================================REQUEST METHODS===============================================\\

    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> user) {
        String username = user.get("username");
        String password = user.get("password");

        Account account = accountRepository.getAccountByUsername(username);

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
            Account account = accountRepository.getAccountByUsername(username);

            if (account != null) {
                int accountId = account.id();
                accountRepository.deleteAccount(accountId);
                statsRepository.deleteStatsByUserId(accountId);
                backgroundRepository.removeBackground(accountId);
                cardRepository.removeCardDesign(accountId);
                gameRepository.removeGame(accountId);


                return ResponseEntity.ok("User deleted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    public ResponseEntity<Map<String, Object>> updateAccount(@RequestBody Map<String, String> updateData) {
        String oldUsername = updateData.get("oldUsername");
        String newUsername = updateData.get("newUsername");
        String newPassword = updateData.get("newPassword");

        Account account = accountRepository.getAccountByUsername(oldUsername);
        if (account == null) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Benutzer nicht gefunden"
            ));
        }

        if (!oldUsername.equals(newUsername) && accountRepository.userExistsByUsername(newUsername)) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Neuer Benutzername bereits vergeben"
            ));
        }

        Account updatedAccount = new Account(
                account.id(),
                newUsername,
                newPassword != null ? newPassword : account.password(),
                account.coins()
        );
        accountRepository.updateAccount(updatedAccount.id(), updatedAccount);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Kontoinformationen erfolgreich aktualisiert"
        ));
    }


    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> user) {
        String username = user.get("username");
        String password = user.get("password");

        Account existingAccount = accountRepository.getAccountByUsername(username);

        if (existingAccount != null) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Benutzername existiert bereits",
                    "username", username,
                    "coins", existingAccount.coins()
            ));
        }

        int newAccountId = accountRepository.generateUniqueId();
        Account newAccount = new Account(newAccountId, username, password, 0);
        accountRepository.createAccount(newAccountId, newAccount);
        accountRepository.saveAccounts();

        Map<String, Object> newUserStats = new HashMap<>();
        newUserStats.put("wins", 0);
        newUserStats.put("coins", 0);
        newUserStats.put("damage", 0);
        newUserStats.put("direkt-damage", 0);
        newUserStats.put("lose", 0);
        newUserStats.put("winrate", 0.0);

        statsRepository.putStats(newAccountId, newUserStats);

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

        Account account = accountRepository.getAccountByUsername(username);

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

        Account account = accountRepository.getAccountByUsername(username);

        if (account != null) {
            Account updatedAccount = new Account(
                    account.id(),
                    account.username(),
                    account.password(),
                    coins
            );

            accountRepository.updateAccount(account.id(), updatedAccount);
            accountRepository.saveAccounts();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "coins", updatedAccount.coins()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Benutzer nicht gefunden"
            ));
        }
    }

    public ResponseEntity<Map<String, Object>> addCoins(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        int amount = (int) request.get("amount");

        Account account = accountRepository.getAccountByUsername(username);

        if (account != null) {
            Account updatedAccount = new Account(
                    account.id(),
                    account.username(),
                    account.password(),
                    account.coins() + amount
            );

            accountRepository.updateAccount(account.id(), updatedAccount);
            accountRepository.saveAccounts();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "coins", updatedAccount.coins()
            ));
        }

        return ResponseEntity.ok(Map.of("success", false));
    }

}