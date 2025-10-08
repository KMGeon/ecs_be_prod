package me.geon.ecs_be_prod.controller;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

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

    @GetMapping("/env")
    public Map<String, Object> getEnvironmentInfo() {
        Map<String, Object> envInfo = new HashMap<>();
        
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            Map<String, String> dotenvVars = new HashMap<>();
            
            dotenv.entries().forEach(entry -> {
                dotenvVars.put(entry.getKey(), entry.getValue());
            });
            
            envInfo.put("dotenv_file_loaded", true);
            envInfo.put("dotenv_variables", dotenvVars);
            envInfo.put("total_variables", dotenvVars.size());
        } catch (Exception e) {
            envInfo.put("dotenv_file_loaded", false);
            envInfo.put("error", e.getMessage());
            envInfo.put("total_variables", 0);
        }
        
        return envInfo;
    }

//    // todo : for ha infra test api
//    @GetMapping("/crash")
//    public String crash() {
//        System.out.println("crashing server!");
//        System.exit(0);
//        return "Server is shutting down...";
//    }




}
