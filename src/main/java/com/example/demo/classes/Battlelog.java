package com.example.demo.classes;

public record Battlelog(Hero attackerCard, Hero defenderCard, int damage, int directDamage, String attacker, int roundNumber, int isEffectiv) {
}
