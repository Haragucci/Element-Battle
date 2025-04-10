package com.example.demo.repositories;

import com.example.demo.classes.Battlelog;
import com.example.demo.classes.Game;
import com.example.demo.classes.Hero;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Repository
public class GameRepository {

    private static final String GAME_FILE_PATH = "files/saved-games.json";
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<Integer, Game> games;

    public GameRepository() {
       games = loadAllGames();
    }

    public boolean gameExistsByUserId(int userId) {
        return games.containsKey(userId);
    }

    public Game createGame(int userId, Game game) {
        if(gameExistsByUserId(userId)) {
            throw new IllegalArgumentException("Game already exists");
        }
        else {
            games.put(userId, game);
            saveAllGames();
            return game;
        }
    }

    public Game updateGame(int userId, Game game) {
        if(!gameExistsByUserId(userId)) {
            throw new IllegalArgumentException("Game does not exist");
        }
        else {
            games.put(userId, game);
            saveAllGames();
            return game;
        }
    }

    public Game deleteGame(int userId) {
        if(!gameExistsByUserId(userId)) {
            return null;
        }
        else {
            Game game = games.remove(userId);
            saveAllGames();
            return game;
        }
    }

    public Game getGame(int userId) {
        if(!gameExistsByUserId(userId)) {
            throw new IllegalArgumentException("Game does not exist");
        }
        else {
            return games.get(userId);
        }
    }

    private Map<Integer, Game> loadAllGames() {
        Map<Integer, Game> games = new HashMap<>();
        try {
            File file = new File(GAME_FILE_PATH);
            if (!file.exists()) return games;

            JsonNode rootNode = mapper.readTree(file);
            rootNode.fields().forEachRemaining(entry -> {
                int userId = Integer.parseInt(entry.getKey());
                JsonNode gameData = entry.getValue();

                List<Hero> playerCards = mapper.convertValue(gameData.get("playerCards"), new TypeReference<>() {});
                List<Hero> computerCards = mapper.convertValue(gameData.get("computerCards"), new TypeReference<>() {});
                String firstAttack = gameData.get("firstAttack").asText();
                int playerHP = gameData.get("playerHP").asInt();
                int computerHP = gameData.get("computerHP").asInt();
                int totalDamageDealt = gameData.get("totalDamageDealt").asInt();
                int totalDirectDamageDealt = gameData.get("totalDirectDamageDealt").asInt();
                List<Battlelog> battlelogs = mapper.convertValue(gameData.get("battlelogs"), new TypeReference<>() {});

                Game game = new Game(playerCards, computerCards, firstAttack, playerHP, computerHP, totalDamageDealt, totalDirectDamageDealt, battlelogs);
                games.put(userId, game);
            });
        } catch (IOException e) {
            System.out.println("Fehler beim Laden der Spiele: " + e.getMessage());
        }
        return games;
    }

    @PreDestroy
    private void saveAllGames() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(GAME_FILE_PATH), games);
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern der Spiele: " + e.getMessage());
        }
    }
}
