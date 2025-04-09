package com.example.demo;

import com.example.demo.repositories.AccountRepository;
import com.example.demo.repositories.HeroRepository;
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
    private final HeroRepository heroRepository;
    private final GameService gameService;
    private final StatsService statsService;
    private final BackgroundService backgroundService;
    private final CardService cardService;
    private final AccountRepository accountRepository;

    @Autowired
    public InitialController(AccountRepository accountRepository, HeroRepository heroRepository, GameService gameService, StatsService statsService, BackgroundService backgroundService, CardService cardService) {
        this.heroRepository = heroRepository;
        this.gameService = gameService;
        this.statsService =statsService;
        this.backgroundService = backgroundService;
        this.cardService = cardService;
        this.accountRepository = accountRepository;
    }


    //===============================================INITIAL AND SHUTDOWN===============================================\\

    @PostConstruct
    public void init() {
        //heroRepository.loadHeroes();
        accountRepository.loadAccounts();
        //backgroundService.loadBackgrounds();
        //cardService.loadCardDesigns();
        //statsService.loadStats();
        gameService.loadGame();
    }

    @PreDestroy
    public void shutdown() {
        //heroRepository.loadHeroes();
        accountRepository.saveAccounts();
        //backgroundService.saveBackgrounds();
        //cardService.saveCardDesigns();
        //statsService.saveStats();
        gameService.saveGame();
    }
}
