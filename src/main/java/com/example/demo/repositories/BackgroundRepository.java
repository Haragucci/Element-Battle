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

    public Map<Integer, String> getBackgrounds() {
        return backgrounds;
    }

    @PreDestroy
    public void save() {
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

    public void setBackground(int userId, String background) {
        backgrounds.put(userId, background);
        save();
    }

    public String getBackground(int userId) {
        return backgrounds.get(userId);
    }

    public boolean hasBackground(int userId) {
        return backgrounds.containsKey(userId);
    }

    public void removeBackground(int userId) {
        backgrounds.remove(userId);
        save();
    }
}
