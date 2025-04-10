package com.example.demo.services;

import com.example.demo.classes.Account;
import com.example.demo.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;

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

    public AccountService(AccountRepository accountRepository, StatsRepository statsRepository, BackgroundRepository backgroundRepository, CardRepository cardRepository, GameRepository gameRepository) {
        this.accountRepository = accountRepository;
        this.statsRepository = statsRepository;
        this.backgroundRepository = backgroundRepository;
        this.cardRepository = cardRepository;
        this.gameRepository = gameRepository;
    }

    //===============================================REQUEST METHODS===============================================\\

    public ResponseEntity<Map<String, Object>> login(Map<String, String> user) {
        String username = user.get("username");
        String password = user.get("password");

        try {
            Account account = accountRepository.getAccountByUsername(username);

            if (account.password().equals(password)) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "username", username,
                        "coins", account.coins()
                ));
            } else {
                return ResponseEntity.ok(Map.of("success", false));
            }

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false));
        }
    }


    public ResponseEntity<String> deleteUser(Map<String, String> payload) {
        String username = payload.get("username");

        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body("Username is required");
        }

        try {
            if (accountRepository.accountExistsByUsername(username)) {
                Account account = accountRepository.getAccountByUsername(username);

                int accountId = account.id();
                accountRepository.deleteAccount(accountId);
                statsRepository.deleteStatsByUserId(accountId);
                backgroundRepository.deleteBackground(accountId);
                cardRepository.deleteCardDesign(accountId);
                gameRepository.deleteGame(accountId);


                return ResponseEntity.ok("User deleted successfully");

            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }

    public ResponseEntity<Map<String, Object>> updateAccount(Map<String, String> updateData) {
        String oldUsername = updateData.get("oldUsername");
        String newUsername = updateData.get("newUsername");
        String newPassword = updateData.get("newPassword");

        try {
            Account oldAccount = accountRepository.getAccountByUsername(oldUsername);
            if (oldAccount == null) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Benutzer nicht gefunden"
                ));
            }

            if (!oldUsername.equals(newUsername) && accountRepository.accountExistsByUsername(newUsername)) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Neuer Benutzername bereits vergeben"
                ));
            }

            Account updatedAccount = new Account(
                    oldAccount.id(),
                    newUsername != null && !newUsername.isEmpty() ? newUsername : oldUsername,
                    newPassword != null && !newPassword.isEmpty() ? newPassword : oldAccount.password(),
                    oldAccount.coins()
            );
            accountRepository.updateAccount(updatedAccount);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Kontoinformationen erfolgreich aktualisiert"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of());
        }
    }


    public ResponseEntity<Map<String, Object>> register(Map<String, String> user) {
        String username = user.get("username");
        String password = user.get("password");
        Account existingAccount;

        try {
            if (accountRepository.accountExistsByUsername(username)) {
                existingAccount = accountRepository.getAccountByUsername(username);
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Benutzername existiert bereits",
                        "username", username,
                        "coins", existingAccount.coins()
                ));
            }

            Account newAccount = new Account(0, username, password, 0);
            Account newAccount2 = accountRepository.createAccount(newAccount);

            Map<String, Object> newUserStats = new HashMap<>();
            newUserStats.put("wins", 0);
            newUserStats.put("coins", 0);
            newUserStats.put("damage", 0);
            newUserStats.put("direkt-damage", 0);
            newUserStats.put("lose", 0);
            newUserStats.put("winrate", 0.0);

            statsRepository.createStats(newAccount2.id(), newUserStats);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "username", username,
                    "coins", 0
            ));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false));
        }
    }

    public ResponseEntity<Map<String, Object>> getUserInfo(Map<String, String> request) {
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


    public ResponseEntity<Map<String, Object>> updateCoins(Map<String, Object> request) {
        String username = (String) request.get("username");
        int coins = (int) request.get("coins");

        try {
            if (accountRepository.accountExistsByUsername(username)) {
                Account account = accountRepository.getAccountByUsername(username);

                Account updatedAccount = new Account(
                        account.id(),
                        account.username(),
                        account.password(),
                        coins
                );

                accountRepository.updateAccount(updatedAccount);

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
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    public ResponseEntity<Map<String, Object>> addCoins(Map<String, Object> request) {
        String username = (String) request.get("username");

        try {
            int amount = (int) request.get("amount");
            if (accountRepository.accountExistsByUsername(username)) {
                Account account = accountRepository.getAccountByUsername(username);

                Account updatedAccount = new Account(
                        account.id(),
                        account.username(),
                        account.password(),
                        account.coins() + amount
                );

                accountRepository.updateAccount(updatedAccount);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "coins", updatedAccount.coins()
                ));
            } else {
                return ResponseEntity.ok(Map.of("success", false));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("message", e.getMessage()));
        }
    }
}