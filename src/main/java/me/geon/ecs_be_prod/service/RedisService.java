package me.geon.ecs_be_prod.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ValueOperations<String, Object> valueOperations;
    
    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
    }
    
    public void setValue(String key, Object value) {
        valueOperations.set(key, value);
    }
    
    public void setValueWithTimeout(String key, Object value, long timeout, TimeUnit timeUnit) {
        valueOperations.set(key, value, timeout, timeUnit);
    }
    
    public void setValueWithDuration(String key, Object value, Duration duration) {
        valueOperations.set(key, value, duration);
    }
    
    public Object getValue(String key) {
        return valueOperations.get(key);
    }
    
    public Boolean deleteValue(String key) {
        return redisTemplate.delete(key);
    }
    
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
    
    public Boolean setExpire(String key, long timeout, TimeUnit timeUnit) {
        return redisTemplate.expire(key, timeout, timeUnit);
    }
    
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }
}