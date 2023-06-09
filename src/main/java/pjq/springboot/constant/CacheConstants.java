/*
 * Copyright © 2023 pengjianqiang
 * All rights reserved.
 * 项目名称：pjq-spring-boot-starter
 * 项目描述：个人常用的Spring Boot应用配置代码
 * 项目地址：https://github.com/qqxadyy/pjq-spring-boot-starter
 * 许可证信息：见下文
 *
 * ======================================================================
 *
 * The MIT License
 * Copyright © 2023 pengjianqiang
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
package pjq.springboot.constant;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * spring集成缓存相关常量
 *
 * @author pengjianqiang
 * @date 2021-06-12
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CacheConstants {
    public static final String DEFAULT_SEPARATOR = ":";

    /**
     * 基础缓存key前缀，所有系统的缓存key均以该前缀开头<br>
     * 需要使用spring的@{@link Value}引入方式引入该值
     */
    public static final String CACHE_KEY_BASE_PREFIX = "${pjq.cache.base-key-prefix:baseCache}" + DEFAULT_SEPARATOR;

    /**
     * 该key生成器会对所有参数转成json串，如果不需要这种方式的话则自行指定key<br>
     * 例如如果参数中有复杂对象的集合，建议还是自行指定key
     */
    public static final String CUSTOM_KEY_GENERATOR_NAME = "customKeyGenerator";

    /**
     * 1小时后失效
     */
    public static final int EXPIRE_IN_ONE_HOUR_INT = 3600;

    /**
     * 1小时后失效
     */
    public static final String EXPIRE_IN_ONE_HOUR = "#" + EXPIRE_IN_ONE_HOUR_INT;

    /**
     * 2小时后失效
     */
    public static final int EXPIRE_IN_TWO_HOUR_INT = 7200;

    /**
     * 2小时后失效
     */
    public static final String EXPIRE_IN_TWO_HOUR = "#" + EXPIRE_IN_TWO_HOUR_INT;

    /**
     * 默认缓存失效时间(2小时)
     */
    public static final Duration DEFAULT_EXPIRE = Duration.ofSeconds(EXPIRE_IN_TWO_HOUR_INT);

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class CacheManagerNames {
        /**
         * 默认Caffeine cacheManager
         */
        public static final String CAFFEINE_CACHE_DEFAULT_MANAGER = "defaultCaffeineCacheManager";

        /**
         * 根据cacheName动态配置的Caffeine cacheManager
         */
        public static final String CAFFEINE_CACHE_DYNAMIC_CONFIG_MANAGER = "dynamicConfigCaffeineCacheManager";

        /**
         * 默认Redis cacheManager
         */
        public static final String REDIS_CACHE_DEFAULT_MANAGER = "defaultRedisCacheManager";

        /**
         * 根据cacheName动态配置的Redis cacheManager
         */
        public static final String REDIS_CACHE_DYNAMIC_CONFIG_MANAGER = "dynamicConfigRedisCacheManager";
    }
}