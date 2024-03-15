/*
 * Copyright © 2024 pengjianqiang
 * All rights reserved.
 * 项目名称：pjq-spring-boot-starter
 * 项目描述：个人常用的Spring Boot应用配置代码
 * 项目地址：https://github.com/qqxadyy/pjq-spring-boot-starter
 * 许可证信息：见下文
 *
 * ======================================================================
 *
 * The MIT License
 * Copyright © 2024 pengjianqiang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package pjq.springboot.config.cache.redis;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import lombok.extern.slf4j.Slf4j;
import pjq.springboot.constant.CacheConstants;
import pjq.springboot.constant.CacheConstants.CacheManagerNames;

/**
 * spring集成的Redis缓存配置<br>
 * 使用时根据具体模块判断是否需要显式指定本cacheManager；如果不确定的话，每次使用本缓存配置时都指定cacheManager<br>
 * 低版本SpringBoot引入的spring-data-redis包没有{@link BatchStrategies}类，此时不注册本Bean
 *
 * @author pengjianqiang
 * @date 2018-09-28
 */
@Slf4j
@Configuration
@ConditionalOnClass({ RedisCacheConfiguration.class, CacheManager.class, BatchStrategies.class })
public class RedisCacheConfig {
    /**
     * 不要命名成jsonSerializer，可能和springfox的冲突
     *
     * @param builder
     * @return
     */
    @Bean
    public Jackson2JsonRedisSerializer<Object> jsonSerializer4Redis(Jackson2ObjectMapperBuilder builder) {
        //JacksonAutoConfiguration.Jackson2ObjectMapperBuilderCustomizerConfiguration
        //builder生成的objectMapper会使用配置文件的spring.jackson的配置
        //即redis的json序列化和http返回对象的json序列号使用相同的jackson配置
        //activateDefaultTyping用于redis序列化时带上类信息，用于@Cacheable等缓存从redis反序列化为对应的对象
        //SpringBoot高版本后，jsonSerializer的objectMapper需要指定PolymorphicTypeValidator，及配置Object的子类为安全类型
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        jsonSerializer.setObjectMapper(
                builder.build().setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
                        .activateDefaultTyping(
                                BasicPolymorphicTypeValidator.builder().allowIfSubType(Object.class).build(),
                                ObjectMapper.DefaultTyping.NON_FINAL));
        return jsonSerializer;
    }

    @Bean
    public RedisCacheConfiguration defaultRedisCacheConfiguration(
            Jackson2JsonRedisSerializer<Object> jsonSerializer4Redis) {
        return genDefaultRedisCacheConfiguration(jsonSerializer4Redis, CacheConstants.DEFAULT_EXPIRE);
    }

    private RedisCacheConfiguration genDefaultRedisCacheConfiguration(
            Jackson2JsonRedisSerializer<Object> jsonSerializer4Redis, Duration ttl) {
        //禁用redisKey的前缀(这里配了前缀的话会自动在生成的key前面加上CacheName::)，直接在KeyGenerator中处理
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        return RedisCacheConfiguration.defaultCacheConfig().entryTtl(ttl).disableKeyPrefix()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer4Redis));
    }

    @Bean(CacheManagerNames.REDIS_CACHE_DEFAULT_MANAGER)
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory,
            RedisCacheConfiguration defaultRedisCacheConfiguration) {
        log.info("加载默认的Redis缓存管理器:{}", CacheManagerNames.REDIS_CACHE_DEFAULT_MANAGER);
        return RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(defaultRedisCacheConfiguration).build();
    }

    /**
     * 根据cacheName实现动态缓存配置的manager<br>
     * 1.需要@{@link Cacheable}等显式指定cacheManager={@link CacheManagerNames#REDIS_CACHE_DYNAMIC_CONFIG_MANAGER}<br>
     * 2.目前只实现了动态失效时间，有需要可再扩展
     *
     * @return
     */
    @Bean(CacheManagerNames.REDIS_CACHE_DYNAMIC_CONFIG_MANAGER)
    public CacheManager dynamicConfigRedisCacheManager(RedisConnectionFactory redisConnectionFactory,
            RedisCacheConfiguration defaultRedisCacheConfiguration,
            Jackson2JsonRedisSerializer<Object> jsonSerializer4Redis) {
        log.info("加载可动态配置的Redis缓存管理器:{}", CacheManagerNames.REDIS_CACHE_DYNAMIC_CONFIG_MANAGER);
        RedisCacheWriter cacheWriter = new AccessableDefaultRedisCacheWriter(redisConnectionFactory,
                BatchStrategies.keys());
        RedisCacheManager redisCacheManager = new RedisCacheManager(cacheWriter, defaultRedisCacheConfiguration) {
            private Map<String, RedisCache> cacheConfigurationMap = new HashMap<>();

            @Override
            protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
                try {
                    //根据cacheName后面的'#'号分隔的秒数，动态创建对应的cache对象
                    if (cacheConfigurationMap.containsKey(name)) {
                        return cacheConfigurationMap.get(name);
                    }

                    if (!name.contains("#")) {
                        return createWithConfig(name, defaultRedisCacheConfiguration); //没有Duration则返回默认的
                    }

                    Long expireDurationSeconds = Long.valueOf(name.substring(name.indexOf("#") + 1));
                    if (expireDurationSeconds <= 0) {
                        return createWithConfig(name, defaultRedisCacheConfiguration); //Duration解析错误则返回默认的
                    }

                    return createWithConfig(name, genDefaultRedisCacheConfiguration(jsonSerializer4Redis,
                            Duration.ofSeconds(expireDurationSeconds)));
                } catch (Exception e) {
                    return createWithConfig(name, defaultRedisCacheConfiguration);
                }
            }

            private RedisCache createWithConfig(String name, RedisCacheConfiguration cacheConfiguration) {
                RedisCache redisCache = super.createRedisCache(name, cacheConfiguration);
                cacheConfigurationMap.put(name, redisCache);
                return redisCache;
            }
        };
        return redisCacheManager;
    }
}