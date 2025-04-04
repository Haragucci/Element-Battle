package com.example.demo.controller;

import com.example.demo.classes.GameRequest;
import com.example.demo.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService){
        this.gameService = gameService;
    }


    @PostMapping("/saveGame")
    public ResponseEntity<Map<String, Object>> saveGame(@RequestBody GameRequest request) {
        return gameService.saveGame(request);
    }

    @PostMapping("/checkGame")
    public ResponseEntity<Map<String, Object>> checkGame(@RequestBody Map<String, String> request) {
        return gameService.checkGame(request);
    }

    @DeleteMapping("/deleteGame/{username}")
    public ResponseEntity<String> deleteGame(@PathVariable String username) {
        return gameService.deleteGame(username);
    }

}
