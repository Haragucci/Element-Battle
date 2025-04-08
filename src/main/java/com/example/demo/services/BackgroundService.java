package com.example.demo.services;

import com.example.demo.classes.Account;
import com.example.demo.repositories.AccountRepository;
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

    //===============================================SERVICE INTEGRATION===============================================\\

    private final AccountRepository accountRepository;

    @Autowired
    public BackgroundService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        this.backgrounds = new HashMap<>();
    }


    //===============================================VARIABLES===============================================\\

    public static final String BACKGROUNDS_FILE_PATH = "files/back.json";
    public final Map<Integer, String> backgrounds; // <-- int ID statt username!
    private final ObjectMapper mapper = new ObjectMapper();


    //===============================================REQUEST METHODS===============================================\\

    public ResponseEntity<Map<String, Object>> buyBackground(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String background = (String) request.get("background");
        int cost = (int) request.get("cost");

        Account account = accountRepository.getAccountByUsername(username);
        if (account != null) {
            int userId = account.id();

            if (account.coins() >= cost) {
                Account updatedAccount = new Account(
                        userId,
                        account.username(),
                        account.password(),
                        account.coins() - cost
                );
                accountRepository.updateAccount(userId, updatedAccount);
                accountRepository.saveAccounts();

                backgrounds.put(userId, background);
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
        Account account = accountRepository.getAccountByUsername(username);
        if (account == null) {
            return ResponseEntity.ok(Map.of("exists", false, "message", "Benutzer nicht gefunden"));
        }

        int userId = account.id();
        String background = backgrounds.get(userId);
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

        Account account = accountRepository.getAccountByUsername(username);
        if (account == null) {
            return ResponseEntity.ok(Map.of("hasBackground", false));
        }

        int userId = account.id();

        boolean hasBackground = backgrounds.containsKey(userId);
        boolean isActive = hasBackground && backgrounds.get(userId).equals(background);

        return ResponseEntity.ok(Map.of(
                "hasBackground", hasBackground,
                "isActive", isActive
        ));
    }

    public ResponseEntity<Map<String, Object>> toggleBackground(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String background = (String) request.get("background");

        Account account = accountRepository.getAccountByUsername(username);
        if (account == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Benutzer nicht gefunden"));
        }

        int userId = account.id();

        if (backgrounds.containsKey(userId)) {
            String currentBackground = backgrounds.get(userId);
            boolean isActive = currentBackground != null && currentBackground.equals(background);
            if (isActive) {
                backgrounds.put(userId, "");
            } else {
                backgrounds.put(userId, background);
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
        Account account = accountRepository.getAccountByUsername(username);

        if (account == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Benutzer nicht gefunden"));
        }

        int userId = account.id();

        if (backgrounds.containsKey(userId)) {
            String background = backgrounds.get(userId);
            return ResponseEntity.ok(Map.of("success", true, "background", background));
        } else {
            return ResponseEntity.ok(Map.of("success", false, "message", "Kein Hintergrund für diesen Benutzer gefunden"));
        }
    }


    //===============================================HELPING METHODS===============================================\\


    public boolean checkBackground(String username){
        Account account = accountRepository.getAccountByUsername(username);
        return account != null && backgrounds.containsKey(account.id());
    }

    public void removeBackgroundAndSave(int id){
        backgrounds.remove(id);
        saveBackgrounds();
    }

    public String removeBackground(String username){
        Account account = accountRepository.getAccountByUsername(username);
        return account != null ? backgrounds.remove(account.id()) : null;
    }

    public void putBackground(String username, String background){
        Account account = accountRepository.getAccountByUsername(username);
        if (account != null) {
            backgrounds.put(account.id(), background);
        }
    }


    //===============================================FILE MANAGEMENT===============================================\\

    public void loadBackgrounds() {
        try {
            File file = new File(BACKGROUNDS_FILE_PATH);
            if (file.exists()) {
                backgrounds.putAll(mapper.readValue(file, new TypeReference<Map<Integer, String>>() {}));
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
