package org.example.staystylish.domain.localweather.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

@Configuration
@EnableCaching
public class RedisConfig {

    // application.yml 또는 Parameter Store 등에서 설정값 주입 받기
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password:#{null}}")
    private String redisPassword;


    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // RedisStandaloneConfiguration 사용하여 호스트, 포트, 비밀번호 설정
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisHost, redisPort);

        // 비밀번호가 설정되어 있을 경우에만 설정
        if (StringUtils.hasText(redisPassword)) {
            redisConfig.setPassword(redisPassword);
        }

        return new LettuceConnectionFactory(redisConfig); // 설정 객체로 LettuceConnectionFactory 생성
    }

    /**
     * RedisTemplate Bean - Redis 연산을 쉽게 수행할 수 있도록 템플릿(template) 제공.
     */

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key(데이터를 구분하는 이름) 직렬화
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value(실제 데이터) 직렬화 (Jackson)
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    // (Refresh Token / 단순 String 캐싱용)
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // 직렬화 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        serializer));

        // 캐시별 만료 시간 설정
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 날씨 API 캐시 ("globalWeather"): 3시간 (해외 날씨 예보 실시간성이 아니고 하루에 몇번 업데이트)
        cacheConfigurations.put("globalWeather", defaultConfig.entryTtl(Duration.ofHours(3)));

        // AI 추천 결과 캐시 ("travelAi"): 24시간(비용/속도 이슈로 날씨 정보가 동일하면 하루 동안 사용)
        cacheConfigurations.put("travelAi", defaultConfig.entryTtl(Duration.ofHours(24)));

        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}

