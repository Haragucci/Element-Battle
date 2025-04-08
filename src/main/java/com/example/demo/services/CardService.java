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
public class CardService {

    //===============================================SERVICE INTEGRATION===============================================\\

    private final AccountRepository accountRepository;

    @Autowired
    public CardService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    //===============================================VARIABLES===============================================\\

    private static final String CARDS_FILE_PATH = "files/cards.json";
    public Map<Integer, String> cardDesigns = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    //===============================================REQUEST METHODS===============================================\\

    public ResponseEntity<Map<String, Object>> buyCardDesign(@RequestBody Map<String, Object> request) {
        int userId = ((Number) request.get("userId")).intValue();
        final int COST = 2;

        synchronized (this) {
            Account account = accountRepository.getAccountById(userId);
            if (account != null) {
                Account updatedAccount = spendCoinsOnAccount(account, COST);
                if (updatedAccount != null) {
                    cardDesigns.putIfAbsent(userId, "default");
                    saveCardDesigns();

                    return ResponseEntity.ok(Map.of(
                            "success", true,
                            "coins", updatedAccount.coins(),
                            "activeDesign", cardDesigns.get(userId)
                    ));
                } else {
                    return ResponseEntity.ok(Map.of("success", false, "message", "Nicht genug Münzen"));
                }
            }
            return ResponseEntity.ok(Map.of("success", false, "message", "Benutzer nicht gefunden"));
        }
    }

    public Account spendCoinsOnAccount(Account account, int cost) {
        if (account.coins() >= cost) {
            Account updatedAccount = new Account(
                    account.id(),
                    account.username(),
                    account.password(),
                    account.coins() - cost
            );
            accountRepository.updateAccount(account.id(), updatedAccount);
            accountRepository.saveAccounts();
            return updatedAccount;
        }

        return null;
    }

    public ResponseEntity<Map<String, Object>> checkUserCardDesign(@RequestBody Map<String, String> request) {
        String username = request.get("username");

        Account account = accountRepository.getAccountByUsername(username);
        int userId = account.id();

        boolean purchased = cardDesigns.containsKey(userId);
        var activeDesign = cardDesigns.getOrDefault(userId, "");

        return ResponseEntity.ok(Map.of(
                "purchased", purchased,
                "activeDesign", activeDesign
        ));
    }

    public ResponseEntity<Map<String, Object>> toggleCardDesign(@RequestBody Map<String, String> request) {
        int userId = Integer.parseInt(request.get("userId"));
        String designId = request.get("designId");

        if (cardDesigns.containsKey(userId)) {
            cardDesigns.put(userId, designId);
            saveCardDesigns();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "activeDesign", cardDesigns.get(userId)
            ));
        } else {
            return ResponseEntity.ok(Map.of("success", false, "message", "Benutzer hat keine Kartendesigns gekauft"));
        }
    }

    //===============================================HELPING METHODS===============================================\\

    public boolean checkCards(int userId) {
        return cardDesigns.containsKey(userId);
    }

    public void removeCardStatsAndSave(int userId) {
        cardDesigns.remove(userId);
        saveCardDesigns();
    }

    public String removeCardDesign(int userId) {
        return cardDesigns.remove(userId);
    }

    public void putCardDesign(int userId, String designId) {
        cardDesigns.put(userId, designId);
    }

    //===============================================FILE MANAGEMENT===============================================\\

    public void loadCardDesigns() {
        try {
            File file = new File(CARDS_FILE_PATH);
            if (file.exists()) {
                Map<String, String> temp = mapper.readValue(file, new TypeReference<>() {});
                temp.forEach((key, value) -> cardDesigns.put(Integer.parseInt(key), value));
            }
        } catch (IOException e) {
            System.out.println("Fehler beim Laden der Kartendesigns");
        }
    }

    public void saveCardDesigns() {
        try {
            Map<String, String> stringKeyMap = new HashMap<>();
            cardDesigns.forEach((key, value) -> stringKeyMap.put(String.valueOf(key), value));
            mapper.writeValue(new File(CARDS_FILE_PATH), stringKeyMap);
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern der Kartendesigns");
        }
    }
}
