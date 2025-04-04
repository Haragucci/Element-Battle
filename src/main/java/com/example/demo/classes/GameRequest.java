package com.example.demo.classes;

import com.example.demo.services.HeroService.Hero;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GameRequest {
    private String username;
    private final String firstAttack;
    private final int playerHP;
    private final int computerHP;
    private final List<Hero> playerCards;
    private final List<Hero> computerCards;

    @JsonCreator
    public GameRequest(
            @JsonProperty("username") String username,
            @JsonProperty("firstAttack") String firstAttack,
            @JsonProperty("PHP") int playerHP,
            @JsonProperty("CHP") int computerHP,
            @JsonProperty("playerCards") List<Hero> playerCards,
            @JsonProperty("computerCards") List<Hero> computerCards) {
        this.username = username;
        this.firstAttack = firstAttack;
        this.playerHP = playerHP;
        this.computerHP = computerHP;
        this.playerCards = playerCards;
        this.computerCards = computerCards;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstAttack(){
        return firstAttack;
    }

    public int getPlayerHP() {
        return playerHP;
    }

    public int getComputerHP() {
        return computerHP;
    }

    public List<Hero> getPlayercards() {
        return playerCards;
    }

    public List<Hero> getComputercards() {
        return computerCards;
    }

    @Override
    public String toString() {
        return "GameRequest{" +
                "username='" + username + '\'' +
                ", playerHP=" + playerHP +
                ", computerHP=" + computerHP +
                ", playerCards=" + playerCards +
                ", computerCards=" + computerCards +
                '}';
    }
}