package me.geon.ecs_be_prod;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EcsBeProdApplication {

    private static final Logger log = LoggerFactory.getLogger(EcsBeProdApplication.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        
        log.info("Loading environment variables from .env file:");
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
            log.info("Loaded: {} = {}", entry.getKey(), entry.getValue());
        });
        
        SpringApplication.run(EcsBeProdApplication.class, args);
    }


}
