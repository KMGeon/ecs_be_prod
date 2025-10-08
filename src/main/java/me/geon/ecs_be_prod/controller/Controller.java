package me.geon.ecs_be_prod.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private ApplicationContext applicationContext;

    @GetMapping("/")
    public String index() {
        return "Hello World!";
    }


    @GetMapping("/test")
    public String test() {
        try {
            redisTemplate.opsForValue().set("ping", "pong");
            String result = redisTemplate.opsForValue().get("ping");
            return "Redis ping successful: " + result;
        } catch (Exception e) {
            return "Redis ping failed: " + e.getMessage();
        }
    }

    // todo : for ha infra test api
    @GetMapping("/crash")
    public String crash() {
        System.out.println("crashing server!");
        System.exit(0);
        return "Server is shutting down...";
    }

}
