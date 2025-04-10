package com.example.demo.services;

import com.example.demo.classes.Account;
import com.example.demo.classes.Game;
import com.example.demo.classes.GameRequest;
import com.example.demo.classes.Hero;
import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameService {

    //===============================================SERVICE===============================================\\

    private final AccountRepository accountRepository;
    private final GameRepository gameRepository;

    //===============================================VARIABLES===============================================\\


    @Autowired
    public GameService(AccountRepository accountRepository, GameRepository gameRepository) {
        this.accountRepository = accountRepository;
        this.gameRepository = gameRepository;
    }

    //===============================================REQUEST METHODS===============================================\\

    public ResponseEntity<Map<String, Object>> saveGame(GameRequest request) {
        String username = request.getUsername();
        try {
            if(accountRepository.accountExistsByUsername(username)){
                Account account = accountRepository.getAccountByUsername(username);

                int userId = account.id();

                Game game = new Game(
                        request.getPlayercards(),
                        request.getComputercards(),
                        request.getFirstAttack(),
                        request.getPlayerHP(),
                        request.getComputerHP(),
                        request.getTotalDamageDealt(),
                        request.getTotalDirectDamageDealt(),
                        request.getBattlelogs()
                );

                if (!gameRepository.gameExistsByUserId(account.id())){
                    gameRepository.createGame(account.id(), game);
                }
                else {
                    gameRepository.updateGame(userId, game);
                }
                return ResponseEntity.ok(Map.of("message", "Spiel gespeichert!"));
            }
            else {
                return ResponseEntity.badRequest().body(Map.of("message", "Benutzer nicht gefunden!"));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }


    public ResponseEntity<String> deleteGame(String username) {
        Account account = accountRepository.getAccountByUsername(username);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }

        int userId = account.id();
        if (gameRepository.gameExistsByUserId(userId)) {
            gameRepository.deleteGame(userId);
            return ResponseEntity.ok("Spielstand für Benutzer '" + username + "' gelöscht.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<Map<String, Object>> checkGame(Map<String, String> request) {
        String username = request.get("username");
        try {
            if(accountRepository.accountExistsByUsername(username)){
                Account account = accountRepository.getAccountByUsername(username);
                int userId = account.id();

                if (!gameRepository.gameExistsByUserId(userId)) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Kein gespeichertes Spiel gefunden!"));
                }

                Game game = gameRepository.getGame(userId);

                return ResponseEntity.ok(Map.of(
                        "Playercards", toHeroMap(game.playerCards()),
                        "Computercards", toHeroMap(game.computerCards()),
                        "firstAttack", game.firstAttack(),
                        "PHP", game.playerHP(),
                        "CHP", game.computerHP(),
                        "totalDamageDealt", game.totalDamageDealt(),
                        "totalDirectDamageDealt", game.totalDirectDamageDealt(),
                        "battlelogs", game.battlelogs()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Benutzer nicht gefunden!"));
            }
        } catch (Exception e){
            return ResponseEntity.ok(Map.of("message", e.getMessage()));
        }
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
}
