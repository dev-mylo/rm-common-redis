package com.rm.common.redis.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
@Component
@RequiredArgsConstructor
public class RedisUtils {

    private final StringRedisTemplate stringRedisTemplate;

    public String getData(String key){ // 레디스에서 데이터 가져오기
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    public void setData(String key, String value){ // 레디스에 데이터 넣기
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.set(key, value);
    }

    public void setDataExpire(String key, String value, long duration){ // 레디스 만료 기간 설정
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(key, value, expireDuration);

    }

    public void deleteData(String key){ // 레디스 데이터 삭제
        stringRedisTemplate.delete(key);
    }

    public boolean isKeyExpired(String key) { // 레디스에 저장된게 만료인지 확인(키가 있는지 확인), 키가 있으면 true 없으면(만료) false
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public void setDataWithTransaction(String key, String value){ // 레디스에 데이터 넣기 트랜젝션
        try {
            stringRedisTemplate.multi(); // 트랜젝션 시작
            ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
            valueOperations.set(key, value);
            stringRedisTemplate.exec(); // 트랜젝션 실행
        } catch (Exception e) { // 에러 전송
            e.printStackTrace();
        }
    }


}




