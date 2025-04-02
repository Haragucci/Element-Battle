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
import java.util.Map;

@Service
public class BackgroundService {

    private final AccountService accountService;

    @Autowired
    public BackgroundService(AccountService accountService) {
        this.accountService = accountService;
    }

    public static final String BACKGROUNDS_FILE_PATH = "back.json";
    public final Map<String, String> backgrounds = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public ResponseEntity<Map<String, Object>> buyBackground(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String background = (String) request.get("background");
        int cost = (int) request.get("cost");

        AccountService.Account account = accountService.accounts.get(username);
        if (account != null) {
            if (account.coins() >= cost) {
                AccountService.Account updatedAccount = new AccountService.Account(
                        account.username(),
                        account.password(),
                        account.coins() - cost
                );
                accountService.accounts.put(username, updatedAccount);
                accountService.saveAccounts();

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

    public ResponseEntity<Map<String, Object>> getBackground(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");

        if (backgrounds.containsKey(username)) {
            String background = backgrounds.get(username);
            return ResponseEntity.ok(Map.of("success", true, "background", background));
        } else {
            return ResponseEntity.ok(Map.of("success", false, "message", "Kein Hintergrund für diesen Benutzer gefunden"));
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

}
