package com.example.demo.classes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Hero(int id, String name, int HP, int Damage, String type, String extra) {}
