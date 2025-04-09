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

    public ResponseEntity<Map<String, Object>> buyCardDesign(Map<String, String> request) {
        String username = request.get("username");
        final int COST = 2;

        try {
            if (accountRepository.accountExistsByUsername(username)) {
                Account account = accountRepository.getAccountByUsername(username);
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

            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "Benutzer nicht gefunden"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    public Account spendCoinsOnAccount(Account account, int cost) {
        try{
            if(accountRepository.accountExistsByUsername(account.username())){
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
                else {return account;}
            }
            else {return account;}
        }catch (Exception e){
            System.out.println(e.getMessage());
            return account;
        }
    }

    public ResponseEntity<Map<String, Object>> checkUserCardDesign(Map<String, String> request) {
        String username = request.get("username");
        if (username == null) {
            return ResponseEntity.ok(Map.of(
                    "purchased", false,
                    "activeDesign", ""
            ));
        }
        try {
            if (accountRepository.accountExistsByUsername(username)) {
                Account account = accountRepository.getAccountByUsername(username);

                String activeDesign = "";
                int userId = account.id();
                boolean purchased = cardRepository.cardDesignExistsByUserId(userId);
                if (purchased) {
                    activeDesign = cardRepository.getCardDesign(userId);
                }
                return ResponseEntity.ok(Map.of(
                        "purchased", purchased,
                        "activeDesign", activeDesign
                ));
            }
            else {return ResponseEntity.ok(Map.of("purchased", false, "activeDesign", ""));}
        }catch (Exception e){
            return ResponseEntity.ok(Map.of("purchased", false, "activeDesign", ""));
        }
    }

    public ResponseEntity<Map<String, Object>> toggleCardDesign(Map<String, String> request) {
        String username = request.get("username");
        String designId = request.get("designId");

        try {
            if (accountRepository.accountExistsByUsername(username)) {
                Account account = accountRepository.getAccountByUsername(username);

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
            else {return ResponseEntity.ok(Map.of("success", false, "message", "Benutzer nicht gefunden"));}
        }catch (Exception e){
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }

    }
}
