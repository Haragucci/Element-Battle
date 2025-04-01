package com.example.demo;

import java.util.List;
import com.example.demo.Controller.Hero;

public class GameRequest {
    private String username;
    private List<Hero> Playercards;
    private List<Hero> Computercards;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Hero> getPlayercards() {
        return Playercards;
    }

    public void setPlayercards(List<Hero> playercards) {
        Playercards = playercards;
    }

    public List<Hero> getComputercards() {
        return Computercards;
    }

    public void setComputercards(List<Hero> computercards) {
        Computercards = computercards;
    }
}


