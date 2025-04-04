package com.example.demo.controller;

import com.example.demo.services.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CardController {

    private final CardService cardService;

    @Autowired
    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/checkUserCardDesign")
    public ResponseEntity<Map<String, Object>> checkUserCardDesign(@RequestBody Map<String, String> request) {
        return cardService.checkUserCardDesign(request);
    }

    @PostMapping("/buyCardDesign")
    public ResponseEntity<Map<String, Object>> buyCardDesign(@RequestBody Map<String, Object> request) {
        return cardService.buyCardDesign(request);
    }

    @PostMapping("/toggleCardDesign")
    public ResponseEntity<Map<String, Object>> toggleCardDesign(@RequestBody Map<String, String> request) {
        return cardService.toggleCardDesign(request);
    }

}
