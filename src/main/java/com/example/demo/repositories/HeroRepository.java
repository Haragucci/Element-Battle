package com.example.demo.repositories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.classes.Hero;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class HeroRepository {

    private static final String HEROES_FILE_PATH = "files/heros.json";
    private List<Hero> heroes;
    public static int nextId = 1;
    private final ObjectMapper objectMapper;

    public HeroRepository() {
        objectMapper = new ObjectMapper();
        loadData();
    }

    private void loadData() {
        File file = new File(HEROES_FILE_PATH);
        if (file.exists()) {
            try {
                heroes = objectMapper.readValue(file, new TypeReference<>() {});
                nextId = heroes.stream().mapToInt(Hero::id).max().orElse(0) + 1;
            } catch (IOException e) {
                System.out.println(e.getMessage());
                heroes = new ArrayList<>();
            }
        } else {
            heroes = new ArrayList<>();
        }
    }

    @PreDestroy
    private void saveData() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(HEROES_FILE_PATH), heroes);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public Hero add(Hero hero) {
        try {
            Hero newHero = new Hero(nextId++, hero.name(), hero.HP(), hero.Damage(), hero.type(), hero.extra());
            heroes.add(newHero);
            saveData();
            return newHero;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to add hero: " + e.getMessage(), e);
        }
    }

    public List<Hero> getAll() {
        try {
            return new ArrayList<>(heroes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to retrieve hero list: " + e.getMessage(), e);
        }
    }

    public Hero update(Hero updatedHero) {
        for (int i = 0; i < heroes.size(); i++) {
            if (heroes.get(i).id() == updatedHero.id()) {
                heroes.set(i, updatedHero);
                saveData();
                return updatedHero;
            }
        }
        throw new NoSuchElementException("Hero not found with id: " + updatedHero.id());
    }

    public Hero findById(int id) {
        return heroes.stream()
                .filter(hero -> hero.id() == id)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Hero not found with id: " + id));
    }

    public Hero delete(int id) {
        Hero heroToDelete = findById(id); // throws if not found
        try {
            heroes.remove(heroToDelete);
            saveData();
            return heroToDelete;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to delete hero with id: " + id, e);
        }
    }

    public boolean deleteAll() {
        try {
            if (heroes.isEmpty()) {
                saveData();
                throw new UnsupportedOperationException("No heroes to delete.");
            } else {
                heroes.clear();
                saveData();
                return true;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to delete all heroes: " + e.getMessage(), e);
        }
    }

    public boolean resetId() {
        if (nextId == 1) {
            throw new UnsupportedOperationException("ID is already at initial state.");
        } else {
            nextId = 1;
            return true;
        }
    }
}
