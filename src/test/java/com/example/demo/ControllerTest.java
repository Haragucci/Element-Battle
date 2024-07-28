/*package com.example.demo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.demo.Controller;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootConfiguration
class ControllerTest {

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private Controller controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testLoadAllStats() throws Exception {
        Map<String, Map<String, Object>> stats = new HashMap<>();
        when(mapper.readValue(any(File.class), any(TypeReference.class))).thenReturn(stats);

        mockMvc.perform(get("/loadAllStats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetUserStats() throws Exception {
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("wins", 10);
        userStats.put("lose", 5);
        userStats.put("damage", 100);
        userStats.put("directDamage", 50);
        userStats.put("coins", 20);

        when(controller.loadUserStats("testUser")).thenReturn(userStats);
        when(controller.getCoinsForUser("testUser")).thenReturn(20);

        mockMvc.perform(post("/getUserStats")
                        .contentType("application/json")
                        .content("{\"username\":\"testUser\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.stats.coins").value(20));
    }

    @Test
    void testUpdateStats() throws Exception {
        Map<String, Object> statsUpdate = new HashMap<>();
        statsUpdate.put("username", "testUser");
        statsUpdate.put("win", 2);
        statsUpdate.put("lose", 1);
        statsUpdate.put("damage", 30);
        statsUpdate.put("directDamage", 20);
        statsUpdate.put("coins", 10);

        Map<String, Object> userStats = new HashMap<>();
        userStats.put("wins", 5);
        userStats.put("lose", 3);
        userStats.put("damage", 70);
        userStats.put("directDamage", 40);
        userStats.put("coins", 15);

        when(controller.loadUserStats("testUser")).thenReturn(userStats);
        when(controller.getCoinsForUser("testUser")).thenReturn(15);
        doNothing().when(controller).saveUserStats(any(String.class), any(Map.class));
        doNothing().when(controller).updateCoinsForUser(any(String.class), anyInt());

        mockMvc.perform(post("/updateStats")
                        .contentType("application/json")
                        .content("{\"username\":\"testUser\",\"win\":2,\"lose\":1,\"damage\":30,\"directDamage\":20,\"coins\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.updatedStats.coins").value(25));
    }

    @Test
    void testLogin() throws Exception {
        Controller.Account account = new Controller.Account("testUser", "password", 100);
        when(controller.accounts.get("testUser")).thenReturn(account);

        mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content("{\"username\":\"testUser\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.coins").value(100));
    }

    @Test
    void testUpdateAccount() throws Exception {
        Controller.Account account = new Controller.Account("oldUser", "password", 100);
        when(controller.accounts.get("oldUser")).thenReturn(account);
        doNothing().when(controller).saveAccounts();

        mockMvc.perform(post("/updateAccount")
                        .contentType("application/json")
                        .content("{\"oldUsername\":\"oldUser\",\"newUsername\":\"newUser\",\"newPassword\":\"newPassword\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testAddCoins() throws Exception {
        Controller.Account account = new Controller.Account("testUser", "password", 100);
        when(controller.accounts.get("testUser")).thenReturn(account);
        doNothing().when(controller).saveAccounts();

        mockMvc.perform(post("/addCoins")
                        .contentType("application/json")
                        .content("{\"username\":\"testUser\",\"amount\":50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.coins").value(150));
    }

    @Test
    void testGetHeroTypes() throws Exception {
        List<String> heroTypes = Arrays.asList("Warrior", "Mage");
        when(Controller.getHeroTypes()).thenReturn(heroTypes);

        mockMvc.perform(get("/hero-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Warrior"))
                .andExpect(jsonPath("$[1]").value("Mage"));
    }

    @Test
    void testCreateHero() throws Exception {
        Controller.Hero hero = new Controller.Hero(1, "HeroName", 100, 50, "Warrior", "Description");
        when(Controller.getHeroTypes()).thenReturn(Collections.singletonList("Warrior"));

        mockMvc.perform(post("/hero")
                        .contentType("application/json")
                        .content("{\"name\":\"HeroName\",\"HP\":100,\"Damage\":50,\"type\":\"Warrior\",\"extra\":\"Description\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("HeroName"));
    }

    @Test
    void testDelHero() throws Exception {
        Controller.Hero hero = new Controller.Hero(1, "HeroName", 100, 50, "Warrior", "Description");
        Controller.heroes.add(hero);

        mockMvc.perform(delete("/herodelete")
                        .contentType("application/json")
                        .content("{\"id\":1}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Passt!"));
    }

    @Test
    void testGetBackground() throws Exception {
        controller.backgrounds.put("testUser", "background1");

        mockMvc.perform(post("/getBackground")
                        .contentType("application/json")
                        .content("{\"username\":\"testUser\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.background").value("background1"));
    }

    @Test
    void testToggleBackground() throws Exception {
        controller.backgrounds.put("testUser", "background1");

        mockMvc.perform(post("/toggleBackground")
                        .contentType("application/json")
                        .content("{\"username\":\"testUser\",\"background\":\"background2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void testBuyBackground() throws Exception {
        Controller.Account account = new Controller.Account("testUser", "password", 100);
        when(controller.accounts.get("testUser")).thenReturn(account);
        doNothing().when(controller).saveAccounts();
        doNothing().when(controller).saveBackgrounds();

        mockMvc.perform(post("/buyBackground")
                        .contentType("application/json")
                        .content("{\"username\":\"testUser\",\"background\":\"background1\",\"cost\":50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.background").value("background1"));
    }

    @Test
    void testBuyCardDesign() throws Exception {
        Controller.Account account = new Controller.Account("testUser", "password", 3);
        when(controller.accounts.get("testUser")).thenReturn(account);
        doNothing().when(controller).saveAccounts();
        doNothing().when(controller).saveCardDesigns();

        mockMvc.perform(post("/buyCardDesign")
                        .contentType("application/json")
                        .content("{\"username\":\"testUser\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.coins").value(1));
    }
}*/
