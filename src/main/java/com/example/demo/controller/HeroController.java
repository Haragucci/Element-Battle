package com.example.demo.controller;

import com.example.demo.services.HeroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.classes.Hero;
import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class HeroController {

    private final HeroService heroService;

    @Autowired
    public HeroController(HeroService heroService) {
        this.heroService = heroService;
    }

    @PostMapping("/hero")
    public ResponseEntity<Hero> createHero(@RequestBody Hero hero) {
        return heroService.createHero(hero);
    }

    @GetMapping("/createProfiles")
    public String createProfiles() {
        return heroService.createProfiles();
    }

    @PutMapping("/heroedit")
    public String editHero(@RequestBody Hero heroe) {
        return heroService.editHero(heroe);
    }

    @DeleteMapping("/herodelete")
    public String delhero(@RequestBody(required = false) Hero herodel, @RequestParam(value = "id", required = false) Integer id) {
        return heroService.delhero(herodel, id);
    }

    @GetMapping("/heroshow")
    public List<Hero> showHero() {
        return heroService.showHero();
    }

    @DeleteMapping("/delall")
    public String deleteAllHeroes() {
        return heroService.deleteAllHeroes();
    }

}
