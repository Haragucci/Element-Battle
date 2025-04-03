package com.example.demo.Services;

import com.example.demo.Classes.Game;
import com.example.demo.Classes.GameRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.demo.Services.HeroService.Hero;

@Service
public class GameService {

    //===============================================VARIABLES===============================================\\

    private final Map<String, Game> games = new HashMap<>();
    private static final String GAME_FILE_PATH = "files/saved-games.json";
    private final ObjectMapper mapper = new ObjectMapper();


    //===============================================REQUEST METHODS===============================================\\

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

    public ResponseEntity<String> deleteGame(@PathVariable String username) {
        if (games.containsKey(username)) {
            games.remove(username);
            saveGame();
            return ResponseEntity.ok("Spielstand für Benutzer '" + username + "' gelöscht.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<Map<String, Object>> checkGame(@RequestBody Map<String, String> request) {
        String username = request.get("username");

        if (username == null || !games.containsKey(username)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Kein gespeichertes Spiel gefunden!"));
        }
        Game game = games.get(username);

        return ResponseEntity.ok(Map.of(
                "Playercards", game.playerCards().stream().map(hero -> Map.of(
                        "id", hero.id(),
                        "name", hero.name(),
                        "HP", hero.HP(),
                        "Damage", hero.Damage(),
                        "type", hero.type(),
                        "extra", hero.extra()
                )).collect(Collectors.toList()),
                "Computercards", game.computerCards().stream().map(hero -> Map.of(
                        "id", hero.id(),
                        "name", hero.name(),
                        "HP", hero.HP(),
                        "Damage", hero.Damage(),
                        "type", hero.type(),
                        "extra", hero.extra()
                )).collect(Collectors.toList()),
                "firstAttack", game.firstAttack(),
                "PHP", game.playerHP(),
                "CHP", game.computerHP()
        ));
    }



    //===============================================FILE MANAGEMENT===============================================\\

    public void loadGame() {
        try {
            File file = new File(GAME_FILE_PATH);
            if (file.exists()) {
                JsonNode rootNode = mapper.readTree(file);

                rootNode.fields().forEachRemaining(entry -> {
                    String username = entry.getKey();
                    JsonNode gameData = entry.getValue();

                    List<Hero> playerCards = mapper.convertValue(gameData.get("playerCards"), new TypeReference<>() {
                    });
                    List<Hero> computerCards = mapper.convertValue(gameData.get("computerCards"), new TypeReference<>() {
                    });
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

    public void saveGame() {
        try {
            mapper.writeValue(new File(GAME_FILE_PATH), games);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
