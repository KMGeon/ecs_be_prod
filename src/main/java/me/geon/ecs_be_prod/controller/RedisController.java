package me.geon.ecs_be_prod.controller;

import me.geon.ecs_be_prod.service.RedisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/redis")
public class RedisController {
    
    private final RedisService redisService;
    
    public RedisController(RedisService redisService) {
        this.redisService = redisService;
    }
    
    @PostMapping("/set")
    public ResponseEntity<Map<String, String>> setValue(
            @RequestParam String key, 
            @RequestParam String value,
            @RequestParam(required = false) Long timeout) {
        
        if (timeout != null && timeout > 0) {
            redisService.setValueWithTimeout(key, value, timeout, TimeUnit.SECONDS);
        } else {
            redisService.setValue(key, value);
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Value set successfully");
        response.put("key", key);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> getValue(@RequestParam String key) {
        Object value = redisService.getValue(key);
        
        Map<String, Object> response = new HashMap<>();
        if (value != null) {
            response.put("key", key);
            response.put("value", value);
            response.put("found", true);
        } else {
            response.put("key", key);
            response.put("found", false);
            response.put("message", "Key not found");
        }
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteValue(@RequestParam String key) {
        Boolean deleted = redisService.deleteValue(key);
        
        Map<String, Object> response = new HashMap<>();
        response.put("key", key);
        response.put("deleted", deleted);
        response.put("message", deleted ? "Key deleted successfully" : "Key not found");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/exists")
    public ResponseEntity<Map<String, Object>> checkKey(@RequestParam String key) {
        Boolean exists = redisService.hasKey(key);
        
        Map<String, Object> response = new HashMap<>();
        response.put("key", key);
        response.put("exists", exists);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/ttl")
    public ResponseEntity<Map<String, Object>> getTimeToLive(@RequestParam String key) {
        Long ttl = redisService.getExpire(key);
        
        Map<String, Object> response = new HashMap<>();
        response.put("key", key);
        response.put("ttl", ttl);
        response.put("message", ttl == -2 ? "Key does not exist" : 
                                ttl == -1 ? "Key exists but has no expiration" : 
                                "TTL in seconds: " + ttl);
        
        return ResponseEntity.ok(response);
    }
}