package com.ttkhnvv.rtm.repository.token;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisTokenRepository implements TokenRepository {
    private static final String PREFIX = "refresh:";

    @Value("${jwt.refresh-token-ttl-days}")
    private long refreshTokenTtlDays;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(UUID userId, String refreshToken) {
        redisTemplate.opsForValue().set(buildKey(userId), refreshToken, refreshTokenTtlDays, TimeUnit.DAYS);
    }

    @Override
    public Optional<String> find(UUID userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(buildKey(userId)));
    }

    @Override
    public void delete(UUID userId) {
        redisTemplate.delete(buildKey(userId));
    }

    private String buildKey(UUID userId) {
        return PREFIX + userId.toString();
    }
}
