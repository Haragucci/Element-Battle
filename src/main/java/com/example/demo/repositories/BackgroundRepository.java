package com.example.demo.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class BackgroundRepository {

    public static final String BACKGROUNDS_FILE_PATH = "files/back.json";
    public final Map<String, String> backgrounds = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

}
