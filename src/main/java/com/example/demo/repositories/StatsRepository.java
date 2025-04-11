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

    public StatsRepository() throws IOException {
        saveStats();
        loadStats();
    }

    public Map<String, Object> getStatsByUserId(int userId) {
        if(stats.containsKey(userId)) {
            return new HashMap<>(stats.get(userId));
        }
        else {
            throw new IllegalArgumentException("User id " + userId + " does not exist");
        }
    }

    public Map<String, Object> updateUserStats(int userId, Map<String, Object> userStats) {
        if (!statsExistsByUserId(userId)) {
            throw new IllegalArgumentException("User not found");
        }
        else {
            Map<String, Object> statlist = stats.put(userId, userStats);
            saveStats();
            return statlist;
        }
    }

    public Map<String, Object> deleteStatsByUserId(int userId) {
        if(!statsExistsByUserId(userId)) {
            throw new IllegalArgumentException("User not found");
        }
        else {
            Map<String, Object> statlist = stats.remove(userId);
            saveStats();
            return statlist;
        }
    }

    public boolean statsExistsByUserId(int userId) {
        return stats.containsKey(userId);
    }

    public Map<String, Object> createStats(int userId, Map<String, Object> statsMap) {
        if(statsExistsByUserId(userId)) {
            throw new RuntimeException("User already exists");
        }
        else {
            stats.put(userId, statsMap);
            saveStats();
            return statsMap;
        }
    }

    public Map<Integer, Map<String, Object>> getAllStats() {
        return stats;
    }

    @PreDestroy
    private void saveStats() {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(STATS_FILE_PATH), stats);
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern der Statistiken: " + e.getMessage());
        }
    }


    private void loadStats() throws IOException {
        File file = new File(STATS_FILE_PATH);
        if (file.exists()) {
            try {
                stats = mapper.readValue(file, new TypeReference<>() {});
            } catch (IOException e) {
                stats = new HashMap<>();
            }
        } else {
            if(file.createNewFile()){
            loadStats();
        }
        else {
            System.out.println("Error creating file");
        }
        }
    }
}
