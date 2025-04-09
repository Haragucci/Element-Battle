package com.example.demo.classes;

import com.example.demo.classes.Hero;
import java.util.List;

public record Game(
        List<Hero> playerCards,
        List<Hero> computerCards,
        String firstAttack,
        int playerHP,
        int computerHP,
        int totalDamageDealt,
        int totalDirectDamageDealt
) {
}
