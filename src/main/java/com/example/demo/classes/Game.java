package com.example.demo.classes;

import java.util.List;

public record Game(
        List<Hero> playerCards,
        List<Hero> computerCards,
        String firstAttack,
        int playerHP,
        int computerHP,
        int totalDamageDealt,
        int totalDirectDamageDealt,
        List<Battlelog> battlelogs
) {
}
