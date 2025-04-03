/*
package com.example.demo;

import com.example.demo.Services.AccountService;
import com.example.demo.Services.BackgroundService;
import com.example.demo.Services.CardService;
import com.example.demo.Services.StatsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootConfiguration
@ComponentScan(basePackages = {"com.example.demo.Services"})
class ControllerTest {

    @MockBean
    private BackgroundService backgroundService;

    @MockBean
    private CardService cardService;

    @MockBean
    private AccountService accountService;

    @Autowired
    public ControllerTest(CardService cardService, AccountService accountService, BackgroundService backgroundService) {
        this.cardService = cardService;
        this.accountService = accountService;
        this.backgroundService = backgroundService;
    }

    @Test
    void contextLoads() {
        assertNotNull(accountService);
    }

    @BeforeEach
    void setUp() {
        assertNotNull(accountService);
    }

    @Test
    void testRegister_Success() {
        Map<String, String> user = new HashMap<>();
        user.put("username", "testuser");
        user.put("password", "password123");

        ResponseEntity<Map<String, Object>> response = accountService.register(user);

        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("testuser", response.getBody().get("username"));
        assertEquals(0, response.getBody().get("coins"));
    }

    @Test
    void testRegister_UsernameExists() {
        accountService.accounts.put("testuser", new AccountService.Account("testuser", "password123", 0));

        Map<String, String> user = new HashMap<>();
        user.put("username", "testuser");
        user.put("password", "password123");

        ResponseEntity<Map<String, Object>> response = accountService.register(user);

        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Benutzername existiert bereits", response.getBody().get("message"));
    }

    @Test
    void testLogin_Success() {
        accountService.accounts.put("testuser", new AccountService.Account("testuser", "password123", 100));

        Map<String, String> user = new HashMap<>();
        user.put("username", "testuser");
        user.put("password", "password123");

        ResponseEntity<Map<String, Object>> response = accountService.login(user);

        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("testuser", response.getBody().get("username"));
        assertEquals(100, response.getBody().get("coins"));
    }

    @Test
    void testLogin_Failure() {
        Map<String, String> user = new HashMap<>();
        user.put("username", "unknown");
        user.put("password", "wrongpassword");

        ResponseEntity<Map<String, Object>> response = accountService.login(user);

        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    void testUpdateAccount_Success() {
        accountService.accounts.put("oldUser", new AccountService.Account("oldUser", "password123", 50));

        Map<String, String> updateData = new HashMap<>();
        updateData.put("oldUsername", "oldUser");
        updateData.put("newUsername", "newUser");
        updateData.put("newPassword", "newPass123");

        ResponseEntity<Map<String, Object>> response = accountService.updateAccount(updateData);

        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Kontoinformationen erfolgreich aktualisiert", response.getBody().get("message"));
        assertNotNull(accountService.accounts.get("newUser"));
        assertNull(accountService.accounts.get("oldUser"));
    }

    @Test
    void testDeleteUser_Success() {
        accountService.accounts.put("testuser", new AccountService.Account("testuser", "password123", 50));
        when(backgroundService.backgrounds).thenReturn(new HashMap<>());
        when(cardService.cardDesigns).thenReturn(new HashMap<>());

        Map<String, String> payload = new HashMap<>();
        payload.put("username", "testuser");

        ResponseEntity<String> response = accountService.deleteUser(payload);

        assertEquals("User deleted successfully", response.getBody());
        assertNull(accountService.accounts.get("testuser"));
    }
}
*/
