package com.example.demo;

import com.example.demo.Controller.Hero;
import java.util.List;

public class Game {
    private List<Hero> playerCards;
    private List<Hero> computerCards;
    private int playerHP;
    private int computerHP;

    public Game(List<Hero> playerCards, List<Hero> computerCards, int playerHP, int computerHP) {
        this.playerCards = playerCards;
        this.computerCards = computerCards;
        this.playerHP = playerHP;
        this.computerHP = computerHP;
    }

    public List<Hero> getPlayerCards() {
        return playerCards;
    }

    public List<Hero> getComputerCards() {
        return computerCards;
    }

    public int getPlayerHP() {
        return playerHP;
    }

    public int getComputerHP() {
        return computerHP;
    }
}
