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
package pjq.springboot.config.cache.caffeine;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.slf4j.Slf4j;
import pjq.springboot.constant.CacheConstants;
import pjq.springboot.constant.CacheConstants.CacheManagerNames;

/**
 * spring集成的Caffeine缓存配置
 *
 * @author pengjianqiang
 * @date 2018-09-28
 */
@Slf4j
@Configuration
@ConditionalOnClass({ Caffeine.class, CacheManager.class })
public class CaffeineCacheConfig {
    private static final int MAXIMUM_SIZE = 1000;
    private static final int MAXIMUM_SIZE_100 = 100;

    @Bean
    public Caffeine<Object, Object> defaultCaffeineConfig() {
        return Caffeine.newBuilder().maximumSize(MAXIMUM_SIZE).expireAfterWrite(CacheConstants.DEFAULT_EXPIRE);
    }

    /**
     * 模块默认cacheManager
     *
     * @param defaultCaffeineConfig
     * @return
     */
    @Primary
    @Bean(CacheManagerNames.CAFFEINE_CACHE_DEFAULT_MANAGER)
    public CacheManager caffeineCacheManager(Caffeine<Object, Object> defaultCaffeineConfig) {
        log.info("加载默认的Caffeine缓存管理器(同时作为应用的默认缓存管理器):{}", CacheManagerNames.CAFFEINE_CACHE_DEFAULT_MANAGER);
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(defaultCaffeineConfig);
        return caffeineCacheManager;
    }

    /**
     * 根据cacheName实现动态缓存配置的manager<br>
     * 1.需要@{@link Cacheable}等显式指定cacheManager={@link CacheManagerNames#CAFFEINE_CACHE_DYNAMIC_CONFIG_MANAGER}<br>
     * 2.目前只实现了动态失效时间，有需要可再扩展
     *
     * @param defaultCaffeineConfig
     * @return
     */
    @Bean(CacheManagerNames.CAFFEINE_CACHE_DYNAMIC_CONFIG_MANAGER)
    public CacheManager dynamicConfigCaffeineCacheManager(Caffeine<Object, Object> defaultCaffeineConfig) {
        log.info("加载可动态配置的Caffeine缓存管理器:{}", CacheManagerNames.CAFFEINE_CACHE_DYNAMIC_CONFIG_MANAGER);
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager() {
            private Map<String, Caffeine<Object, Object>> cacheBuilderMap = new HashMap<>();

            @Override
            protected Cache<Object, Object> createNativeCaffeineCache(String name) {
                try {
                    //根据cacheName后面的'#'号分隔的秒数，动态创建对应的cache对象
                    if (cacheBuilderMap.containsKey(name)) {
                        return cacheBuilderMap.get(name).build();
                    }

                    if (!name.contains("#")) {
                        return super.createNativeCaffeineCache(name); //没有Duration则返回默认的
                    }

                    Long expireDurationSeconds = Long.valueOf(name.substring(name.indexOf("#") + 1));
                    if (expireDurationSeconds <= 0) {
                        return super.createNativeCaffeineCache(name); //Duration解析错误则返回默认的
                    }

                    return createWithBuilder(name, Caffeine.newBuilder().maximumSize(MAXIMUM_SIZE_100)
                            .expireAfterWrite(Duration.ofSeconds(expireDurationSeconds)));
                } catch (Exception e) {
                    return super.createNativeCaffeineCache(name);
                }
            }

            private Cache<Object, Object> createWithBuilder(String name, Caffeine<Object, Object> builder) {
                cacheBuilderMap.put(name, builder);
                return builder.build();
            }
        };
        caffeineCacheManager.setCaffeine(defaultCaffeineConfig);
        return caffeineCacheManager;
    }
}