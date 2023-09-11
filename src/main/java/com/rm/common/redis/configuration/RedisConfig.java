package com.rm.common.redis.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rm.common.redis.properties.RedisClusterConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableRedisRepositories
@RequiredArgsConstructor
@ComponentScan(
        basePackages = {
                "com.rm.common.redis",
        }
)

public class RedisConfig {
    // resource-env안에 xml파일에 적혀있는 레디스 정보를 가져옴
    @Value("${spring.redis.host}")
    private String RedisHost;
    @Value("${spring.redis.port}")
    private int RedisPort;

    private final RedisClusterConfigurationProperties clusterProperties;
    private RedissonClient redisson; // 동시성 락을 위해서 설정
//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        RedisClusterConfiguration redisConfig = new RedisClusterConfiguration ();
//        clusterProperties.getNodes().forEach(s ->{
//            String[] url = s.split(":");
//            redisConfig.clusterNode(url[0],Integer.parseInt(url[1]));
//        });
//        return new LettuceConnectionFactory(redisConfig);
//    }
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration();
        redisConfiguration.setHostName(RedisHost);
        redisConfiguration.setPort(RedisPort);
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisConfiguration);
        return lettuceConnectionFactory;
    }
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModules(new JavaTimeModule(), new Jdk8Module());

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(mapper));
        return redisTemplate;
    }
    @Bean
    public RedissonClient redisConnection() {
        Config config = new Config();
        // 레디스 서버 주소 넣기
        config.useSingleServer().setAddress("redis://" + RedisHost + ":" + RedisPort); // 레디스 접속

        // Redisson 클라이언트 생성
        return redisson = Redisson.create(config);

    }
}