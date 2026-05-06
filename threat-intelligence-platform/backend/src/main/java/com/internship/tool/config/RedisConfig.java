package com.internship.tool.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;

@Configuration
@EnableCaching
@Profile("!dev")
public class RedisConfig {

    // This sets up how Redis stores cached data
    @Bean
    public RedisCacheManager cacheManager(@NonNull RedisConnectionFactory connectionFactory) {

        @SuppressWarnings("null")
        RedisCacheConfiguration config = RedisCacheConfiguration
                .defaultCacheConfig()
                // Cache expires after 10 minutes
                .entryTtl(Duration.ofMinutes(10))
                // Store cache values as JSON (readable)
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer())
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}