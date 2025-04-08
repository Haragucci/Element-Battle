package com.example.demo.services;

import com.example.demo.classes.Account;
import com.example.demo.classes.Game;
import com.example.demo.classes.GameRequest;
import com.example.demo.classes.Hero;
import com.example.demo.repositories.AccountRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameService {

    //===============================================SERVICE===============================================\\

    private final AccountRepository accountRepository;

    @Autowired
    public GameService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    //===============================================VARIABLES===============================================\\

    public final Map<Integer, Game> games = new HashMap<>();
    private static final String GAME_FILE_PATH = "files/saved-games.json";
    private final ObjectMapper mapper = new ObjectMapper();

    //===============================================REQUEST METHODS===============================================\\

    public ResponseEntity<Map<String, Object>> saveGame(@RequestBody GameRequest request) {
        String username = request.getUsername();
        Account account = accountRepository.getAccountByUsername(username);
        if (account == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Benutzer nicht gefunden!"));
        }

        int userId = account.id();
        Game game = new Game(
                request.getPlayercards(),
                request.getComputercards(),
                request.getFirstAttack(),
                request.getPlayerHP(),
                request.getComputerHP()
        );

        games.put(userId, game);
        saveGame();
        return ResponseEntity.ok(Map.of("message", "Spiel gespeichert!"));
    }

    public ResponseEntity<String> deleteGame(@PathVariable String username) {
        Account account = accountRepository.getAccountByUsername(username);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }

        int userId = account.id();
        if (games.containsKey(userId)) {
            games.remove(userId);
            saveGame();
            return ResponseEntity.ok("Spielstand für Benutzer '" + username + "' gelöscht.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<Map<String, Object>> checkGame(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        Account account = accountRepository.getAccountByUsername(username);
        if (account == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Benutzer nicht gefunden!"));
        }

        int userId = account.id();
        if (!games.containsKey(userId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Kein gespeichertes Spiel gefunden!"));
        }

        Game game = games.get(userId);

        return ResponseEntity.ok(Map.of(
                "Playercards", toHeroMap(game.playerCards()),
                "Computercards", toHeroMap(game.computerCards()),
                "firstAttack", game.firstAttack(),
                "PHP", game.playerHP(),
                "CHP", game.computerHP()
        ));
    }

    //===============================================HELPING METHODS===============================================\\

    private List<Map<String, Object>> toHeroMap(List<Hero> heroes) {
        return heroes.stream().map(hero -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", hero.id());
            map.put("name", hero.name());
            map.put("HP", hero.HP());
            map.put("Damage", hero.Damage());
            map.put("type", hero.type());
            map.put("extra", hero.extra());
            return map;
        }).collect(Collectors.toList());
    }


    public boolean checkGames(int userId){
        return games.containsKey(userId);
    }

    public void removeGameAndSave(int userId){
        games.remove(userId);
        saveGame();
    }

    public void putGame(int userId, Game game) {
        games.put(userId, game);
    }

    public Game removeGame(int userId){
        return games.remove(userId);
    }

    //===============================================FILE MANAGEMENT===============================================\\

    public void loadGame() {
        try {
            File file = new File(GAME_FILE_PATH);
            if (file.exists()) {
                JsonNode rootNode = mapper.readTree(file);

                rootNode.fields().forEachRemaining(entry -> {
                    int userId = Integer.parseInt(entry.getKey());
                    JsonNode gameData = entry.getValue();

                    List<Hero> playerCards = mapper.convertValue(gameData.get("playerCards"), new TypeReference<>() {});
                    List<Hero> computerCards = mapper.convertValue(gameData.get("computerCards"), new TypeReference<>() {});
                    String firstAttack = gameData.get("firstAttack").asText();
                    int playerHP = gameData.get("playerHP").asInt();
                    int computerHP = gameData.get("computerHP").asInt();

                    Game game = new Game(playerCards, computerCards, firstAttack, playerHP, computerHP);
                    games.put(userId, game);
                });
            }
        } catch (IOException e) {
            System.out.println("Fehler beim Laden der Spiele: " + e.getMessage());
        }
    }

    public void saveGame() {
        try {
            mapper.writeValue(new File(GAME_FILE_PATH), games);
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern der Spiele: " + e.getMessage());
        }
    }
}
