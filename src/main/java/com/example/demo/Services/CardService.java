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
public class CardService {

    private final AccountService accountService;

    @Autowired
    public CardService(AccountService accountService) {
        this.accountService = accountService;
    }

    private static final String CARDS_FILE_PATH = "cards.json";
    Map<String, String> cardDesigns = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public ResponseEntity<Map<String, Object>> buyCardDesign(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        final int COST = 2;

        synchronized (this) {
            AccountService.Account account = accountService.accounts.get(username);
            if (account != null) {
                if (account.coins() >= COST) {
                    AccountService.Account updatedAccount = new AccountService.Account(
                            account.username(),
                            account.password(),
                            account.coins() - COST
                    );
                    accountService.accounts.put(username, updatedAccount);
                    accountService.saveAccounts();

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

    public ResponseEntity<Map<String, Object>> checkUserCardDesign(@RequestBody Map<String, String> request) {
        String username = request.get("username");

        boolean purchased = cardDesigns.containsKey(username);
        String activeDesign = cardDesigns.getOrDefault(username, "");

        return ResponseEntity.ok(Map.of(
                "purchased", purchased,
                "activeDesign", activeDesign
        ));
    }

    public ResponseEntity<Map<String, Object>> toggleCardDesign(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String designId = request.get("designId");

        if (cardDesigns != null &&cardDesigns.containsKey(username)) {
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
