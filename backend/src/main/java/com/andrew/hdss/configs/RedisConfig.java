package com.andrew.hdss.configs;

import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return builder -> builder
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        .serializeKeysWith(RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair
                                .fromSerializer(jacksonSerializer())));
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jacksonSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jacksonSerializer());
        template.afterPropertiesSet();
        return template;
    }

    private GenericJacksonJsonRedisSerializer jacksonSerializer() {
        return GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType(Object.class)
                        .build())
                .build();
    }
}