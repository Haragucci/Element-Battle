package com.example.demo;

import com.example.demo.Controller.Hero;
import java.util.List;

public class Game {
    private List<Hero> playerCards;
    private List<Hero> computerCards;

    public Game() {
    }

    public Game(List<Hero> playerCards, List<Hero> computerCards) {
        this.playerCards = playerCards;
        this.computerCards = computerCards;
    }

    public List<Hero> getPlayerCards() {
        return playerCards;
    }

    public List<Hero> getComputerCards() {
        return computerCards;
    }
}
