package com.example.demo.repositories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Repository
public class StatsRepository {

    private static final String STATS_FILE_PATH = "files/stats.json";
    private final ObjectMapper mapper = new ObjectMapper();
    private Map<Integer, Map<String, Object>> stats = new HashMap<>();

    public StatsRepository() {
        loadStats();
    }

    public Map<String, Object> getStatsByUserId(int userId) {
        return new HashMap<>(stats.getOrDefault(userId, new HashMap<>()));
    }

    public void saveUserStats(int userId, Map<String, Object> userStats) {
        stats.put(userId, new HashMap<>(userStats));
        saveStats();
    }

    public void deleteStatsByUserId(int userId) {
        stats.remove(userId);
        saveStats();
    }

    public boolean exists(int userId) {
        return stats.containsKey(userId);
    }

    public void putStats(int userId, Map<String, Object> statsMap) {
        stats.put(userId, statsMap);
    }

    public Map<Integer, Map<String, Object>> getAllStats() {
        return stats;
    }

    @PreDestroy
    public void saveStats() {
        try {
            mapper.writeValue(new File(STATS_FILE_PATH), stats);
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern der Statistiken: " + e.getMessage());
        }
    }

    public void loadStats() {
        File file = new File(STATS_FILE_PATH);
        if (file.exists()) {
            try {
                stats = mapper.readValue(file, new TypeReference<>() {});
            } catch (IOException e) {
                stats = new HashMap<>();
            }
        } else {
            stats = new HashMap<>();
        }
    }
}
