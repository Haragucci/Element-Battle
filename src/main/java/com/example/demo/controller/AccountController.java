package com.example.demo.controller;

import com.example.demo.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> user) {
        return accountService.register(user);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> user) {
        return accountService.login(user);
    }

    @PostMapping("/del-user")
    public ResponseEntity<String> deleteUser(@RequestBody Map<String, String> payload) {
        return accountService.deleteUser(payload);
    }

    @PostMapping("/getUserInfo")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestBody Map<String, String> request) {
        return accountService.getUserInfo(request);
    }

    @PostMapping("/updateAccount")
    public ResponseEntity<Map<String, Object>> updateAccount(@RequestBody Map<String, String> updateData) {
        return accountService.updateAccount(updateData);
    }

    @PostMapping("/updateCoins")
    public ResponseEntity<Map<String, Object>> updateCoins(@RequestBody Map<String, Object> request) {
        return accountService.updateCoins(request);
    }

    @PostMapping("/addCoins")
    public ResponseEntity<Map<String, Object>> addCoins(@RequestBody Map<String, Object> request) {
        return accountService.addCoins(request);
    }

}
