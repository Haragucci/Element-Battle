package com.example.demo.controller;

import com.example.demo.services.BackgroundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BackgroundController {

    private final BackgroundService backgroundService;

    @Autowired
    public BackgroundController(BackgroundService backgroundService){
        this.backgroundService = backgroundService;
    }

    @PostMapping("/hasBackground")
    public ResponseEntity<Map<String, Object>> hasBackground(@RequestBody Map<String, String> request) {
        return backgroundService.hasBackground(request);
    }

    @PostMapping("/checkUserBackground")
    public ResponseEntity<Map<String, Object>> checkUserBackground(@RequestBody Map<String, String> request) {
        return backgroundService.checkUserBackground(request);
    }

    @PostMapping("/buyBackground")
    public ResponseEntity<Map<String, Object>> buyBackground(@RequestBody Map<String, Object> request) {
        return backgroundService.buyBackground(request);
    }

    @PostMapping("/toggleBackground")
    public ResponseEntity<Map<String, Object>> toggleBackground(@RequestBody Map<String, Object> request) {
        return backgroundService.toggleBackground(request);
    }

    @PostMapping("/getBackground")
    public ResponseEntity<Map<String, Object>> getBackground(@RequestBody Map<String, Object> request) {
        return backgroundService.getBackground(request);
    }

}
