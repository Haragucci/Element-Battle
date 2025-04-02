package com.example.demo.Classes;

import com.example.demo.Controller.Hero;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GameRequest {
    private String username;
    private String firstAttack;
    private int playerHP;
    private int computerHP;
    private List<Hero> playerCards;
    private List<Hero> computerCards;

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

    public GameRequest() {
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

    public void setFirstAttack(String firstAttack){
        this.firstAttack = firstAttack;
    }

    public int getPlayerHP() {
        return playerHP;
    }

    public void setPlayerHP(int playerHP) {
        this.playerHP = playerHP;
    }

    public int getComputerHP() {
        return computerHP;
    }

    public void setComputerHP(int computerHP) {
        this.computerHP = computerHP;
    }

    public List<Hero> getPlayercards() {
        return playerCards;
    }

    public void setPlayercards(List<Hero> playerCards) {
        this.playerCards = playerCards;
    }

    public List<Hero> getComputercards() {
        return computerCards;
    }

    public void setComputercards(List<Hero> computerCards) {
        this.computerCards = computerCards;
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