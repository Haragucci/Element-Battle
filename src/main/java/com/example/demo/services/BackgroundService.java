package com.example.demo.services;

import com.example.demo.classes.Account;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.BackgroundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;

@Service
public class BackgroundService {

    //===============================================SERVICE INTEGRATION===============================================\\

    private final AccountRepository accountRepository;
    private final BackgroundRepository backgroundRepository;

    @Autowired
    public BackgroundService(AccountRepository accountRepository, BackgroundRepository backgroundRepository) {
        this.accountRepository = accountRepository;
        this.backgroundRepository = backgroundRepository;
    }

    //===============================================REQUEST METHODS===============================================\\

    public ResponseEntity<Map<String, Object>> buyBackground(Map<String, Object> request) {
        String username = (String) request.get("username");
        String background = (String) request.get("background");
        int cost = (int) request.get("cost");

        try {
            if (accountRepository.accountExistsByUsername(username)) {
                Account account = accountRepository.getAccountByUsername(username);
                int userId = account.id();

                if (account.coins() >= cost) {
                    Account updatedAccount = new Account(
                            userId,
                            account.username(),
                            account.password(),
                            account.coins() - cost
                    );
                    accountRepository.updateAccount(updatedAccount);

                    backgroundRepository.updateBackground(userId, background);

                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "coins", updatedAccount.coins(),
                            "background", background
                    ));
                } else {
                    return ResponseEntity.ok(Map.of("success", false, "message", "Nicht genug Münzen"));
                }
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "Benutzer nicht gefunden"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    public ResponseEntity<Map<String, Object>> checkUserBackground(Map<String, String> request) {
        String username = request.get("username");

        try {
            if (accountRepository.accountExistsByUsername(username)) {
                Account account = accountRepository.getAccountByUsername(username);

                int userId = account.id();
                String background = backgroundRepository.getBackground(userId);
                boolean exists = background != null;

                Map<String, Object> response = new HashMap<>();
                response.put("exists", exists);
                if (exists) {
                    response.put("activeBackground", background);
                }
                return ResponseEntity.ok(response);
            }
            else{
                return ResponseEntity.ok(Map.of("success", false, "message", "Benutzer nicht gefunden"));
            }
        }catch (Exception e){
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    public ResponseEntity<Map<String, Object>> hasBackground(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String background = request.get("background");

        if (username == null || background == null) {
            return ResponseEntity.badRequest().body(Map.of("hasBackground", false));
        }

        try {
            if (accountRepository.accountExistsByUsername(username)) {
                Account account = accountRepository.getAccountByUsername(username);

                int userId = account.id();

                boolean hasBackground = backgroundRepository.backgroundExistsById(userId);
                boolean isActive = hasBackground && backgroundRepository.getBackground(userId).equals(background);

                return ResponseEntity.ok(Map.of(
                        "hasBackground", hasBackground,
                        "isActive", isActive
                ));
            }
            else {return ResponseEntity.ok(Map.of("hasBackground", false));}
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("hasBackground", false));
        }

    }

    public ResponseEntity<Map<String, Object>> toggleBackground(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String background = (String) request.get("background");

        try {
            if (accountRepository.accountExistsByUsername(username)) {
                Account account = accountRepository.getAccountByUsername(username);

                int userId = account.id();

                if (backgroundRepository.backgroundExistsById(userId)) {
                    String currentBackground = backgroundRepository.getBackground(userId);
                    boolean isActive = currentBackground != null && currentBackground.equals(background);
                    if (isActive) {
                        backgroundRepository.updateBackground(userId, "");
                    } else {
                        backgroundRepository.updateBackground(userId, background);
                    }
                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "isActive", !isActive
                    ));
                } else {
                    return ResponseEntity.ok(Map.of("success", false, "message", "Benutzer hat keinen Hintergrund"));
                }
            }
            else {return ResponseEntity.ok(Map.of("success", false, "message", "Benutzer nicht gefunden"));}
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    public ResponseEntity<Map<String, Object>> getBackground(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");

        try{
            if (accountRepository.accountExistsByUsername(username)) {
                Account account = accountRepository.getAccountByUsername(username);

                int userId = account.id();

                if (backgroundRepository.backgroundExistsById(userId)) {
                    String background = backgroundRepository.getBackground(userId);
                    return ResponseEntity.ok(Map.of("success", true, "background", background));
                } else {
                    return ResponseEntity.ok(Map.of("success", false, "message", "Kein Hintergrund für diesen Benutzer gefunden"));
                }
            }
            else {return ResponseEntity.ok(Map.of("success", false, "message", "Benutzer nicht gefunden"));}

        }catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }

    }
}
