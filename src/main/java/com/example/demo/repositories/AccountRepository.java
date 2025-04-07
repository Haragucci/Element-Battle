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


    //===============================================VARIABLES===============================================\\

    public static final String ACCOUNTS_FILE_PATH = "files/acc.json";
    private Map<String, Account> accounts = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();


    //===============================================REQUEST METHODS===============================================\\

    public Account getAccount(String username) {
        return accounts.get(username);
    }

    public void removeAccount(String username) {
        accounts.remove(username);
    }

    public void updateAccount(String username, Account account) {
        accounts.put(username, account);
    }

    public boolean hasAccount(String username) {
        return accounts.containsKey(username);
    }

    //===============================================FILE MANAGEMENT===============================================\\


    public void loadAccounts() {
        try {
            File file = new File(ACCOUNTS_FILE_PATH);
            if (file.exists()) {
                accounts = mapper.readValue(file, new TypeReference<>() {});
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
            System.out.println("Fehler beim speichern der Accounts");
        }
    }
}