package com.example.demo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;



import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class Controller implements Serializable {

    private final Map<String, Game> games = new HashMap<>();

    private static final String HEROES_FILE_PATH = "heros.json";
    private static final String ACCOUNTS_FILE_PATH = "acc.json";
    private static final String CARDS_FILE_PATH = "cards.json";
    private static final String GAME_FILE_PATH = "saved-games.json";
    private static final String STATS_FILE_PATH = "stats.json";
    private final ObjectMapper mapper = new ObjectMapper();
    private final AccountService accountService;
    private final HeroService heroService;

    public static AtomicInteger counter = new AtomicInteger(1);

    public record Account(String username, String password, int coins) {}
    Map<String, Account> accounts = new HashMap<>();

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Hero(int id, String name, int HP, int Damage, String type, String extra) {}

    @Autowired
    public Controller(AccountService accountService, HeroService heroService) {
        this.accountService = accountService;
        this.heroService = heroService;
    }

    @PostConstruct
    public void init() {
        heroService.loadHeroes();
        accountService.loadAccounts();
        accountService.loadBackgrounds();
        accountService.loadCardDesigns();
        accountService.loadStats();
        loadGame();
    }

    @PreDestroy
    public void shutdown() {
        heroService.saveHeroes();
        accountService.saveAccounts();
        accountService.saveBackgrounds();
        accountService.saveCardDesigns();
        accountService.saveStats();
        saveGame();
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
        return accountService.getLeaderboard();
    }

    @PostMapping("/del-user")
    public ResponseEntity<String> deleteUser(@RequestBody Map<String, String> payload) {
        return accountService.deleteUser(payload);
    }


    @PostMapping("/getUserStats")
    public ResponseEntity<Map<String, Object>> getUserStats(@RequestBody Map<String, String> request) {
        return accountService.getUserStats(request);
    }

    @PostMapping("/updateStats")
    public ResponseEntity<Map<String, Object>> updateStats(@RequestBody Map<String, Object> statsUpdate) {
            return accountService.updateStats(statsUpdate);
    }

    @PostMapping("/getUserInfo")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestBody Map<String, String> request) {
        String username = request.get("username");

        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Kein Benutzername angegeben"
            ));
        }

        Account account = accounts.get(username);
        if (account == null) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Benutzer nicht gefunden"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "username", account.username(),
                "password", account.password(),
                "coins", account.coins()
        ));
    }

    @PostMapping("/updateAccount")
    public ResponseEntity<Map<String, Object>> updateAccount(@RequestBody Map<String, String> updateData) {
        return accountService.updateAccount(updateData);
    }

    @PostMapping("/updateCoins")
    public ResponseEntity<Map<String, Object>> updateCoins(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        int coins = (int) request.get("coins");

        Account account = accounts.get(username);
        if (account != null) {
            Account updatedAccount = new Account(
                    account.username(),
                    account.password(),
                    coins
            );
            accounts.put(username, updatedAccount);
            accountService.saveAccounts();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "coins", updatedAccount.coins()
            ));
        }

        return ResponseEntity.ok(Map.of("success", false));
    }


    @PostMapping("/addCoins")
    public ResponseEntity<Map<String, Object>> addCoins(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        int amount = (int) request.get("amount");

        Account account = accounts.get(username);
        if (account != null) {
            Account updatedAccount = new Account(
                    account.username(),
                    account.password(),
                    account.coins() + amount
            );
            accounts.put(username, updatedAccount);
            accountService.saveAccounts();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "coins", updatedAccount.coins()
            ));
        }

        return ResponseEntity.ok(Map.of("success", false));
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
        String username = request.getUsername();
        String firstAttack = request.getFirstAttack();
        int playerHP = request.getPlayerHP();
        int computerHP = request.getComputerHP();
        List<Hero> playerCards = request.getPlayercards();
        List<Hero> computerCards = request.getComputercards();

        if (username == null || playerCards == null || computerCards == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Fehlende Daten!"));
        }

        Game game = new Game(playerCards, computerCards, firstAttack, playerHP, computerHP);
        games.put(username, game);
        saveGame();

        return ResponseEntity.ok(Map.of("message", "Spiel gespeichert!"));
    }

    @PostMapping("/checkGame")
    public ResponseEntity<Map<String, Object>> checkGame(@RequestBody Map<String, String> request) {
        String username = request.get("username");

        if (username == null || !games.containsKey(username)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Kein gespeichertes Spiel gefunden!"));
        }
        Game game = games.get(username);

        return ResponseEntity.ok(Map.of(
                "Playercards", game.getPlayerCards().stream().map(hero -> Map.of(
                        "id", hero.id(),
                        "name", hero.name(),
                        "HP", hero.HP(),
                        "Damage", hero.Damage(),
                        "type", hero.type(),
                        "extra", hero.extra()
                )).collect(Collectors.toList()),
                "Computercards", game.getComputerCards().stream().map(hero -> Map.of(
                        "id", hero.id(),
                        "name", hero.name(),
                        "HP", hero.HP(),
                        "Damage", hero.Damage(),
                        "type", hero.type(),
                        "extra", hero.extra()
                )).collect(Collectors.toList()),
                "firstAttack", game.getFirstAttack(),
                "PHP", game.getPlayerHP(),
                "CHP", game.getComputerHP()
        ));
    }

    private void loadGame() {
        try {
            File file = new File(GAME_FILE_PATH);
            if (file.exists()) {
                JsonNode rootNode = mapper.readTree(file);

                rootNode.fields().forEachRemaining(entry -> {
                    String username = entry.getKey();
                    JsonNode gameData = entry.getValue();

                    List<Hero> playerCards = mapper.convertValue(gameData.get("playerCards"), new TypeReference<List<Hero>>() {});
                    List<Hero> computerCards = mapper.convertValue(gameData.get("computerCards"), new TypeReference<List<Hero>>() {});
                    String firstAttack = gameData.get("firstAttack").asText();
                    int playerHP = gameData.get("playerHP").asInt();
                    int computerHP = gameData.get("computerHP").asInt();
                    Game game = new Game(playerCards, computerCards, firstAttack, playerHP, computerHP);
                    games.put(username, game);
                });
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void saveGame() {
        try {
            mapper.writeValue(new File(GAME_FILE_PATH), games);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @DeleteMapping("/game/{username}")
    public ResponseEntity<String> deleteGame(@PathVariable String username) {
        if (games.containsKey(username)) {
            games.remove(username);
            saveGame();
            return ResponseEntity.ok("Spielstand für Benutzer '" + username + "' gelöscht.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/hasBackground")
    public ResponseEntity<Map<String, Object>> hasBackground(@RequestBody Map<String, String> request) {
       return accountService.hasBackground(request);
    }

    @PostMapping("/checkUserBackground")
    public ResponseEntity<Map<String, Object>> checkUserBackground(@RequestBody Map<String, String> request) {
        return accountService.checkUserBackground(request);
    }

    @PostMapping("/buyBackground")
    public ResponseEntity<Map<String, Object>> buyBackground(@RequestBody Map<String, Object> request) {
        return accountService.buyBackground(request);
    }

    @PostMapping("/toggleBackground")
    public ResponseEntity<Map<String, Object>> toggleBackground(@RequestBody Map<String, Object> request) {
        return accountService.toggleBackground(request);
    }

    @PostMapping("/getBackground")
    public ResponseEntity<Map<String, Object>> getBackground(@RequestBody Map<String, Object> request) {
        return accountService.getBackground(request);
    }


    @PostMapping("/checkUserCardDesign")
    public ResponseEntity<Map<String, Object>> checkUserCardDesign(@RequestBody Map<String, String> request) {
        return accountService.checkUserCardDesign(request);
    }

    @PostMapping("/buyCardDesign")
    public ResponseEntity<Map<String, Object>> buyCardDesign(@RequestBody Map<String, Object> request) {
        return accountService.buyCardDesign(request);
    }

    @PostMapping("/toggleCardDesign")
    public ResponseEntity<Map<String, Object>> toggleCardDesign(@RequestBody Map<String, String> request) {
        return accountService.toggleCardDesign(request);
    }

}
