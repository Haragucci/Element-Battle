package com.example.demo.services;

import com.example.demo.classes.Account;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Service
public class CardService {

    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;

    @Autowired
    public CardService(AccountRepository accountRepository, CardRepository cardRepository) {
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
    }

    public ResponseEntity<Map<String, Object>> buyCardDesign(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        final int COST = 2;

        Account account = accountRepository.getAccountByUsername(username);
        if (account != null) {
            Account updatedAccount = spendCoinsOnAccount(account, COST);
            if (updatedAccount != null) {
                cardRepository.setCardDesign(account.id(), "default");

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "coins", updatedAccount.coins(),
                        "activeDesign", "default"
                ));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "Nicht genug Münzen"));
            }
        }
        return ResponseEntity.ok(Map.of("success", false, "message", "Benutzer nicht gefunden"));
    }

    public Account spendCoinsOnAccount(Account account, int cost) {
        if (account.coins() >= cost) {
            Account updatedAccount = new Account(
                    account.id(),
                    account.username(),
                    account.password(),
                    account.coins() - cost
            );
            accountRepository.updateAccount(updatedAccount);
            return updatedAccount;
        }
        return null;
    }

    public ResponseEntity<Map<String, Object>> checkUserCardDesign(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        if (username == null) {
            return ResponseEntity.ok(Map.of(
                    "purchased", false,
                    "activeDesign", ""
            ));
        }

        Account account = accountRepository.getAccountByUsername(username);
        if (account == null) {
            return ResponseEntity.ok(Map.of("purchased", false, "activeDesign", ""));
        }

        String activeDesign = "";
        int userId = account.id();
        boolean purchased = cardRepository.cardDesignExistsByUserId(userId);
        if(purchased) {
            activeDesign = cardRepository.getCardDesign(userId);
        }
        return ResponseEntity.ok(Map.of(
                "purchased", purchased,
                "activeDesign", activeDesign
        ));
    }

    public ResponseEntity<Map<String, Object>> toggleCardDesign(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String designId = request.get("designId");

        Account account = accountRepository.getAccountByUsername(username);
        if (account == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Benutzer nicht gefunden"));
        }

        int userId = account.id();
        if (cardRepository.cardDesignExistsByUserId(userId)) {
            cardRepository.setCardDesign(userId, designId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "activeDesign", designId
            ));
        } else {
            return ResponseEntity.ok(Map.of("success", false, "message", "Benutzer hat keine Kartendesigns gekauft"));
        }
    }

    public boolean checkCards(int userId) {
        return cardRepository.cardDesignExistsByUserId(userId);
    }

    public String removeCardDesign(int userId) {
        String design = cardRepository.getCardDesign(userId);
        cardRepository.deleteCardDesign(userId);
        return design;
    }

    public void putCardDesign(int userId, String designId) {
        cardRepository.setCardDesign(userId, designId);
    }
}
