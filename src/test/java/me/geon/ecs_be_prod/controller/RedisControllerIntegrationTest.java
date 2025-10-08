package me.geon.ecs_be_prod.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RedisControllerIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    void setValue_ShouldSetValueSuccessfully() throws Exception {
        mockMvc.perform(post("/api/redis/set")
                .param("key", "testKey")
                .param("value", "testValue")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Value set successfully"))
                .andExpect(jsonPath("$.key").value("testKey"));
    }

    @Test
    void setValue_WithTimeout_ShouldSetValueWithExpiration() throws Exception {
        mockMvc.perform(post("/api/redis/set")
                .param("key", "testKeyWithTimeout")
                .param("value", "testValue")
                .param("timeout", "60")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Value set successfully"))
                .andExpect(jsonPath("$.key").value("testKeyWithTimeout"));
    }

    @Test
    void getValue_ExistingKey_ShouldReturnValue() throws Exception {
        redisTemplate.opsForValue().set("existingKey", "existingValue");

        mockMvc.perform(get("/api/redis/get")
                .param("key", "existingKey")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("existingKey"))
                .andExpect(jsonPath("$.value").value("existingValue"))
                .andExpect(jsonPath("$.found").value(true));
    }

    @Test
    void getValue_NonExistingKey_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/redis/get")
                .param("key", "nonExistingKey")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("nonExistingKey"))
                .andExpect(jsonPath("$.found").value(false))
                .andExpect(jsonPath("$.message").value("Key not found"));
    }

    @Test
    void deleteValue_ExistingKey_ShouldDeleteSuccessfully() throws Exception {
        redisTemplate.opsForValue().set("keyToDelete", "valueToDelete");

        mockMvc.perform(delete("/api/redis/delete")
                .param("key", "keyToDelete")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("keyToDelete"))
                .andExpect(jsonPath("$.deleted").value(true))
                .andExpect(jsonPath("$.message").value("Key deleted successfully"));
    }

    @Test
    void deleteValue_NonExistingKey_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/redis/delete")
                .param("key", "nonExistingKeyToDelete")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("nonExistingKeyToDelete"))
                .andExpect(jsonPath("$.deleted").value(false))
                .andExpect(jsonPath("$.message").value("Key not found"));
    }

    @Test
    void checkKey_ExistingKey_ShouldReturnTrue() throws Exception {
        redisTemplate.opsForValue().set("existingKeyToCheck", "value");

        mockMvc.perform(get("/api/redis/exists")
                .param("key", "existingKeyToCheck")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("existingKeyToCheck"))
                .andExpect(jsonPath("$.exists").value(true));
    }

    @Test
    void checkKey_NonExistingKey_ShouldReturnFalse() throws Exception {
        mockMvc.perform(get("/api/redis/exists")
                .param("key", "nonExistingKeyToCheck")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("nonExistingKeyToCheck"))
                .andExpect(jsonPath("$.exists").value(false));
    }

    @Test
    void getTimeToLive_ExistingKeyWithTTL_ShouldReturnTTL() throws Exception {
        redisTemplate.opsForValue().set("keyWithTTL", "value");
        redisTemplate.expire("keyWithTTL", 60, java.util.concurrent.TimeUnit.SECONDS);

        mockMvc.perform(get("/api/redis/ttl")
                .param("key", "keyWithTTL")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("keyWithTTL"))
                .andExpect(jsonPath("$.ttl").isNumber());
    }

    @Test
    void getTimeToLive_ExistingKeyWithoutTTL_ShouldReturnMinusOne() throws Exception {
        redisTemplate.opsForValue().set("keyWithoutTTL", "value");

        mockMvc.perform(get("/api/redis/ttl")
                .param("key", "keyWithoutTTL")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("keyWithoutTTL"))
                .andExpect(jsonPath("$.ttl").value(-1))
                .andExpect(jsonPath("$.message").value("Key exists but has no expiration"));
    }

    @Test
    void getTimeToLive_NonExistingKey_ShouldReturnMinusTwo() throws Exception {
        mockMvc.perform(get("/api/redis/ttl")
                .param("key", "nonExistingKeyForTTL")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("nonExistingKeyForTTL"))
                .andExpect(jsonPath("$.ttl").value(-2))
                .andExpect(jsonPath("$.message").value("Key does not exist"));
    }
}