package com.example.demo;

import com.example.demo.Classes.GameRequest;
import com.example.demo.Services.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.Serializable;

import java.util.*;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class Controller implements Serializable {

    private final AccountService accountService;
    private final HeroService heroService;

    private final GameService gameService;
    private final StatsService statsService;
    private final BackgroundService backgroundService;
    private final CardService cardService;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Hero(int id, String name, int HP, int Damage, String type, String extra) {}

    @Autowired
    public Controller(AccountService accountService, HeroService heroService, GameService gameService, StatsService statsService, BackgroundService backgroundService, CardService cardService) {
        this.accountService = accountService;
        this.heroService = heroService;
        this.gameService = gameService;
        this.statsService =statsService;
        this.backgroundService = backgroundService;
        this.cardService = cardService;
    }

    @PostConstruct
    public void init() {
        heroService.loadHeroes();
        accountService.loadAccounts();
        backgroundService.loadBackgrounds();
        cardService.loadCardDesigns();
        statsService.loadStats();
        gameService.loadGame();
    }

    @PreDestroy
    public void shutdown() {
        heroService.saveHeroes();
        accountService.saveAccounts();
        backgroundService.saveBackgrounds();
        cardService.saveCardDesigns();
        statsService.saveStats();
        gameService.saveGame();
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> user) {
        return accountService.register(user);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> user) {
        return accountService.login(user);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard() {
        return statsService.getLeaderboard();
    }

    @PostMapping("/del-user")
    public ResponseEntity<String> deleteUser(@RequestBody Map<String, String> payload) {
        return accountService.deleteUser(payload);
    }

    @PostMapping("/getUserStats")
    public ResponseEntity<Map<String, Object>> getUserStats(@RequestBody Map<String, String> request) {
        return statsService.getUserStats(request);
    }

    @PostMapping("/updateStats")
    public ResponseEntity<Map<String, Object>> updateStats(@RequestBody Map<String, Object> statsUpdate) {
            return statsService.updateStats(statsUpdate);
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

    @PostMapping("/hero")
    public ResponseEntity<Hero> createHero(@RequestBody Hero hero) {
        return heroService.createHero(hero);
    }

    @GetMapping("/createProfiles")
    public String createProfiles() {
        return heroService.createProfiles();
    }

    @PutMapping("/heroedit")
    public String editHero(@RequestBody Hero heroe) {
        return heroService.editHero(heroe);
    }

    @DeleteMapping("/herodelete")
    public String delhero(@RequestBody(required = false) Hero herodel, @RequestParam(value = "id", required = false) Integer id) {
        return heroService.delhero(herodel, id);
    }

    @GetMapping("/heroshow")
    public List<Hero> showHero() {
        return heroService.showHero();
    }

    @DeleteMapping("/delall")
    public String deleteAllHeroes() {
        return heroService.deleteAllHeroes();
    }

    @PostMapping("/saveGame")
    public ResponseEntity<Map<String, Object>> saveGame(@RequestBody GameRequest request) {
        return gameService.saveGame(request);
    }

    @PostMapping("/checkGame")
    public ResponseEntity<Map<String, Object>> checkGame(@RequestBody Map<String, String> request) {
        return gameService.checkGame(request);
    }


    @DeleteMapping("/game/{username}")
    public ResponseEntity<String> deleteGame(@PathVariable String username) {
        return gameService.deleteGame(username);
    }

    @PostMapping("/hasBackground")
    public ResponseEntity<Map<String, Object>> hasBackground(@RequestBody Map<String, String> request) {
       return backgroundService.hasBackground(request);
    }

    @PostMapping("/checkUserBackground")
    public ResponseEntity<Map<String, Object>> checkUserBackground(@RequestBody Map<String, String> request) {
        return backgroundService.checkUserBackground(request);
    }

    @PostMapping("/buyBackground")
    public ResponseEntity<Map<String, Object>> buyBackground(@RequestBody Map<String, Object> request) {
        return backgroundService.buyBackground(request);
    }

    @PostMapping("/toggleBackground")
    public ResponseEntity<Map<String, Object>> toggleBackground(@RequestBody Map<String, Object> request) {
        return backgroundService.toggleBackground(request);
    }

    @PostMapping("/getBackground")
    public ResponseEntity<Map<String, Object>> getBackground(@RequestBody Map<String, Object> request) {
        return backgroundService.getBackground(request);
    }

    @PostMapping("/checkUserCardDesign")
    public ResponseEntity<Map<String, Object>> checkUserCardDesign(@RequestBody Map<String, String> request) {
        return cardService.checkUserCardDesign(request);
    }

    @PostMapping("/buyCardDesign")
    public ResponseEntity<Map<String, Object>> buyCardDesign(@RequestBody Map<String, Object> request) {
        return cardService.buyCardDesign(request);
    }

    @PostMapping("/toggleCardDesign")
    public ResponseEntity<Map<String, Object>> toggleCardDesign(@RequestBody Map<String, String> request) {
        return accountService.toggleCardDesign(request);
    }

}
