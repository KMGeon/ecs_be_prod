package me.geon.ecs_be_prod.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class RedisServiceTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    void setValue_ShouldSetValue() {
        String key = "testKey";
        String value = "testValue";

        redisService.setValue(key, value);

        Object retrievedValue = redisTemplate.opsForValue().get(key);
        assertEquals(value, retrievedValue);
    }

    @Test
    void setValue_WithComplexObject_ShouldSetValue() {
        String key = "complexKey";
        TestObject testObject = new TestObject("name", 123);

        redisService.setValue(key, testObject);

        Object retrievedValue = redisTemplate.opsForValue().get(key);
        assertNotNull(retrievedValue);
    }

    @Test
    void setValueWithTimeout_ShouldSetValueWithExpiration() throws InterruptedException {
        String key = "timeoutKey";
        String value = "timeoutValue";
        long timeout = 1;

        redisService.setValueWithTimeout(key, value, timeout, TimeUnit.SECONDS);

        Object retrievedValue = redisService.getValue(key);
        assertEquals(value, retrievedValue);

        Thread.sleep(1100);

        Object expiredValue = redisService.getValue(key);
        assertNull(expiredValue);
    }

    @Test
    void setValueWithDuration_ShouldSetValueWithExpiration() throws InterruptedException {
        String key = "durationKey";
        String value = "durationValue";
        Duration duration = Duration.ofSeconds(1);

        redisService.setValueWithDuration(key, value, duration);

        Object retrievedValue = redisService.getValue(key);
        assertEquals(value, retrievedValue);

        Thread.sleep(1100);

        Object expiredValue = redisService.getValue(key);
        assertNull(expiredValue);
    }

    @Test
    void getValue_ExistingKey_ShouldReturnValue() {
        String key = "existingKey";
        String value = "existingValue";
        redisTemplate.opsForValue().set(key, value);

        Object retrievedValue = redisService.getValue(key);

        assertEquals(value, retrievedValue);
    }

    @Test
    void getValue_NonExistingKey_ShouldReturnNull() {
        String key = "nonExistingKey";

        Object retrievedValue = redisService.getValue(key);

        assertNull(retrievedValue);
    }

    @Test
    void deleteValue_ExistingKey_ShouldReturnTrueAndRemoveKey() {
        String key = "keyToDelete";
        String value = "valueToDelete";
        redisTemplate.opsForValue().set(key, value);

        Boolean deleted = redisService.deleteValue(key);

        assertTrue(deleted);
        assertNull(redisService.getValue(key));
    }

    @Test
    void deleteValue_NonExistingKey_ShouldReturnFalse() {
        String key = "nonExistingKeyToDelete";

        Boolean deleted = redisService.deleteValue(key);

        assertFalse(deleted);
    }

    @Test
    void hasKey_ExistingKey_ShouldReturnTrue() {
        String key = "existingKeyToCheck";
        String value = "value";
        redisTemplate.opsForValue().set(key, value);

        Boolean exists = redisService.hasKey(key);

        assertTrue(exists);
    }

    @Test
    void hasKey_NonExistingKey_ShouldReturnFalse() {
        String key = "nonExistingKeyToCheck";

        Boolean exists = redisService.hasKey(key);

        assertFalse(exists);
    }

    @Test
    void setExpire_ExistingKey_ShouldSetExpiration() throws InterruptedException {
        String key = "keyToExpire";
        String value = "value";
        redisTemplate.opsForValue().set(key, value);

        Boolean expireSet = redisService.setExpire(key, 1, TimeUnit.SECONDS);

        assertTrue(expireSet);
        assertEquals(value, redisService.getValue(key));

        Thread.sleep(1100);

        assertNull(redisService.getValue(key));
    }

    @Test
    void setExpire_NonExistingKey_ShouldReturnFalse() {
        String key = "nonExistingKeyToExpire";

        Boolean expireSet = redisService.setExpire(key, 1, TimeUnit.SECONDS);

        assertFalse(expireSet);
    }

    @Test
    void getExpire_KeyWithTTL_ShouldReturnPositiveValue() {
        String key = "keyWithTTL";
        String value = "value";
        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expire(key, 60, TimeUnit.SECONDS);

        Long ttl = redisService.getExpire(key);

        assertNotNull(ttl);
        assertTrue(ttl > 0);
        assertTrue(ttl <= 60);
    }

    @Test
    void getExpire_KeyWithoutTTL_ShouldReturnMinusOne() {
        String key = "keyWithoutTTL";
        String value = "value";
        redisTemplate.opsForValue().set(key, value);

        Long ttl = redisService.getExpire(key);

        assertEquals(-1L, ttl);
    }

    @Test
    void getExpire_NonExistingKey_ShouldReturnMinusTwo() {
        String key = "nonExistingKeyForTTL";

        Long ttl = redisService.getExpire(key);

        assertEquals(-2L, ttl);
    }

    @Test
    void multipleOperations_ShouldWorkCorrectly() {
        String key1 = "key1";
        String key2 = "key2";
        String value1 = "value1";
        String value2 = "value2";

        redisService.setValue(key1, value1);
        redisService.setValueWithTimeout(key2, value2, 60, TimeUnit.SECONDS);

        assertTrue(redisService.hasKey(key1));
        assertTrue(redisService.hasKey(key2));
        assertEquals(value1, redisService.getValue(key1));
        assertEquals(value2, redisService.getValue(key2));

        redisService.deleteValue(key1);

        assertFalse(redisService.hasKey(key1));
        assertTrue(redisService.hasKey(key2));
        assertNull(redisService.getValue(key1));
        assertEquals(value2, redisService.getValue(key2));
    }

    private static class TestObject {
        private String name;
        private Integer number;

        public TestObject() {}

        public TestObject(String name, Integer number) {
            this.name = name;
            this.number = number;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestObject that = (TestObject) o;
            return name.equals(that.name) && number.equals(that.number);
        }

        @Override
        public int hashCode() {
            return name.hashCode() + number.hashCode();
        }
    }
}