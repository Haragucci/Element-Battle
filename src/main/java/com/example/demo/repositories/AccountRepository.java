package com.example.demo.repositories;

import com.example.demo.classes.Account;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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


    public Account getAccount(int id) {
        return accounts.get(id);
    }

    public void removeAccountAndSave(int id) {
        if (userExistsById(id)) {
            accounts.remove(id);
            saveAccounts();
        } else {
            throw new IllegalArgumentException("Account with id " + id + " does not exist");
        }
    }

    public void updateAccount(int id, Account account) {
        if (userExistsById(id)) {
            accounts.put(id, account);
            saveAccounts();
        } else {
            throw new IllegalArgumentException("Account with id " + id + " does not exist");
        }
    }

    public boolean userExistsByUsername(String username) {
        return accounts.values().stream()
                .anyMatch(account -> account.username().equals(username));
    }

    public Account getAccountById(int id) {
        return accounts.get(id);
    }

    public boolean userExistsById(int id) {
        return accounts.containsKey(id);
    }

    public Account getAccountByUsername(String username) {
        return accounts.values().stream()
                .filter(account -> account.username().equals(username))
                .findFirst()
                .orElse(null);
    }

    public int generateUniqueId() {
        return accounts.size() + 1;
    }

    public void loadAccounts() {
        try {
            File file = new File(ACCOUNTS_FILE_PATH);
            if (file.exists()) {
                accounts = mapper.readValue(file, new TypeReference<Map<Integer, Account>>() {});
            } else {
                System.out.println("acc.json Datei existiert nicht.");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void saveAccounts() {
        try {
            mapper.writeValue(new File(ACCOUNTS_FILE_PATH), accounts);
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern der Accounts");
        }
    }
}
