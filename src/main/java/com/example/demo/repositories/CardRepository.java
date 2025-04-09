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
public class CardRepository {

    private static final String CARDS_FILE_PATH = "files/cards.json";
    private final Map<Integer, String> cardDesigns = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public CardRepository() {
        loadCardDesigns();
    }

    public boolean cardDesignExistsByUserId(int userId) {
        return cardDesigns.containsKey(userId);
    }

    public String getCardDesign(int userId) {
        if (!cardDesignExistsByUserId(userId)) {
            throw new IllegalArgumentException("User does not have a card design");
        }
        else {
            return cardDesigns.get(userId);
        }
    }

    public String setCardDesign(int userId, String designId) {
        if(!cardDesignExistsByUserId(userId)) {
            throw new RuntimeException("Card Design dont exists");
        }
        else {
            cardDesigns.put(userId, designId);
            saveCardDesigns();
            return designId;
        }
    }
    public String createCardDesign(int userId, String designId) {
        if(cardDesignExistsByUserId(userId)) {
            throw new RuntimeException("Card Design already exists");
        }
        else {
            cardDesigns.put(userId, designId);
            saveCardDesigns();
            return designId;
        }
    }

    public String deleteCardDesign(int userId) {
        if(!cardDesignExistsByUserId(userId)) {
            throw new RuntimeException("Card Design does not exist");
        }
        else{
            String card = cardDesigns.remove(userId);
            saveCardDesigns();
            return card;
        }
    }

    private void loadCardDesigns() {
        try {
            File file = new File(CARDS_FILE_PATH);
            if (file.exists()) {
                Map<String, String> temp = mapper.readValue(file, new TypeReference<>() {});
                temp.forEach((key, value) -> cardDesigns.put(Integer.parseInt(key), value));
            }
        } catch (IOException e) {
            System.out.println("Fehler beim Laden der Kartendesigns");
        }
    }

    @PreDestroy
    private void saveCardDesigns() {
        try {
            Map<String, String> stringKeyMap = new HashMap<>();
            cardDesigns.forEach((key, value) -> stringKeyMap.put(String.valueOf(key), value));
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(CARDS_FILE_PATH), stringKeyMap);
        } catch (IOException e) {
            System.out.println("Fehler beim Speichern der Kartendesigns");
        }
    }
}
