package com.example.demo;

import org.springframework.web.bind.annotation.CrossOrigin;
import com.example.demo.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class InitialController {

    //===============================================SERVICE INTEGRATION===============================================\\

    private final AccountService accountService;
    private final HeroService heroService;
    private final GameService gameService;
    private final StatsService statsService;
    private final BackgroundService backgroundService;
    private final CardService cardService;

    @Autowired
    public InitialController(AccountService accountService, HeroService heroService, GameService gameService, StatsService statsService, BackgroundService backgroundService, CardService cardService) {
        this.accountService = accountService;
        this.heroService = heroService;
        this.gameService = gameService;
        this.statsService =statsService;
        this.backgroundService = backgroundService;
        this.cardService = cardService;
    }


    //===============================================INITIAL AND SHUTDOWN===============================================\\

    @PostConstruct
    public void init() {
        heroService.loadHeroes();
        accountService.loadAccounts();
        backgroundService.loadBackgrounds();
        cardService.loadCardDesigns();
        statsService.loadStats();
        gameService.loadGame();
    }

    @PreDestroy
    public void shutdown() {
        heroService.saveHeroes();
        accountService.saveAccounts();
        backgroundService.saveBackgrounds();
        cardService.saveCardDesigns();
        statsService.saveStats();
        gameService.saveGame();
    }
}
