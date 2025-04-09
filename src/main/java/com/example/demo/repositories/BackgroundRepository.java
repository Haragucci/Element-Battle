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
public class BackgroundRepository {

    private static final String BACKGROUNDS_FILE_PATH = "files/back.json";
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<Integer, String> backgrounds;

    public BackgroundRepository() {
        this.backgrounds = new HashMap<>();
        loadBackgrounds();
    }

    @PreDestroy
    private void save() {
        try {
            mapper.writeValue(new File(BACKGROUNDS_FILE_PATH), backgrounds);
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern der Hintergründe!");
        }
    }

    private void loadBackgrounds() {
        try {
            File file = new File(BACKGROUNDS_FILE_PATH);
            if (file.exists()) {
                backgrounds.putAll(mapper.readValue(file, new TypeReference<>() {}));
            }
        } catch (IOException e) {
            System.out.println("Fehler beim Laden der Hintergründe!");
        }
    }

    public String updateBackground(int userId, String background) {
        if(!backgrounds.containsKey(userId)) {
            throw new IllegalArgumentException("User already Exists!");
        }
        else {
            backgrounds.put(userId, background);
            save();
            return background;
        }
    }
    public String createBackground(int userId, String background) {
        if (backgrounds.containsKey(userId)) {
            throw new IllegalArgumentException("User " + userId + " already exists");
        }
        else {
            backgrounds.put(userId, background);
            save();
            return background;
        }
    }

    public String getBackground(int userId) {
        if(!backgrounds.containsKey(userId)) {
            throw new IllegalArgumentException("User does not exist");
        }
        else {
            return backgrounds.get(userId);
        }
    }

    public boolean backgroundExistsById(int userId) {
        return backgrounds.containsKey(userId);
    }

    public String deleteBackground(int userId) {
        if(!backgroundExistsById(userId)) {
            throw new IllegalArgumentException("User does not exist");
        }
        else {
            String background = backgrounds.remove(userId);
            save();
            return background;
        }
    }
}
