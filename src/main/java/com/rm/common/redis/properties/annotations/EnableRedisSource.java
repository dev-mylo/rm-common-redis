package com.rm.common.redis.properties.annotations;

import com.rm.common.redis.configuration.RedisConfigurationSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(RedisConfigurationSelector.class)
public @interface EnableRedisSource {
}
