package com.example.ElementBattle.controller;

import com.example.ElementBattle.services.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class StatsController {

    private final StatsService statsService;

    @Autowired
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @PostMapping("/getUserStats")
    public ResponseEntity<Map<String, Object>> getUserStats(@RequestBody Map<String, String> request) {
        return statsService.getUserStats(request);
    }

    @PostMapping("/updateStats")
    public ResponseEntity<Map<String, Object>> updateStats(@RequestBody Map<String, Object> statsUpdate) {
        return statsService.updateStats(statsUpdate);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard() {
        return statsService.getLeaderboard();
    }

}
