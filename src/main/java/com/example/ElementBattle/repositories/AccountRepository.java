package com.example.ElementBattle.repositories;

import com.example.ElementBattle.classes.Account;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class AccountRepository {

    public static final String ACCOUNTS_FILE_PATH = "files/acc.json";
    private Map<Integer, Account> accounts = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    private int counter = 1;


    public AccountRepository() {
        loadAccounts();
    }

    public Account deleteAccount(int id) {
        if (accountExistsById(id)) {
            Account account = getAccountById(id);
            accounts.remove(id);
            saveAccounts();
            return account;
        } else {
            throw new IllegalArgumentException("Account with id " + id + " does not exist");
        }
    }

    public Account updateAccount(Account account) {
        int id = account.id();
        if (accountExistsById(id)) {
            accounts.put(id, account);
            saveAccounts();
            return account;
        } else {
            throw new IllegalArgumentException("Account with id " + id + " does not exist");
        }
    }

    public Account createAccount(Account account) {
        if (!accountExistsByUsername(account.username())) {
            int id = counter++;
            Account account1 = new Account(id, account.username(), account.password(), account.coins());
            accounts.put(account1.id(), account1);
            saveAccounts();
            return account1;
        } else {
            throw new IllegalArgumentException("Account with id " + account.username() + " already exist");
        }
    }

    public boolean accountExistsByUsername(String username) {
        return accounts.values().stream()
                .anyMatch(account -> account.username().equals(username));
    }

    public Account getAccountById(int id) {
        return accounts.values().stream()
                .filter(account -> account.id() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Account with id "+ id + " does not exist"));
    }

    public boolean accountExistsById(int id) {
        return accounts.containsKey(id);
    }

    public Account getAccountByUsername(String username) {
        return accounts.values().stream()
                .filter(account -> account.username().equals(username))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Account with name " + username + " does not exist"));
    }

    private void loadAccounts() {
        try {
            File file = new File(ACCOUNTS_FILE_PATH);
            if (file.exists()) {
                accounts = mapper.readValue(file, new TypeReference<>() {});
                counter = accounts.values().stream().mapToInt(Account::id).max().orElse(0) + 1;
            } else {
                if(file.createNewFile()){
                    saveAccounts();
                    loadAccounts();
                }
                else {
                    System.out.println("Error creating file");
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @PreDestroy
    private void saveAccounts() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(ACCOUNTS_FILE_PATH), accounts);
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern der Accounts");
        }
    }
}
