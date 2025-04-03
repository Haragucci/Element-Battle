package com.example.demo.Classes;

import com.example.demo.Services.HeroService.Hero;
import java.util.List;

public record Game(List<Hero> playerCards, List<Hero> computerCards, String firstAttack, int playerHP, int computerHP) {
}
