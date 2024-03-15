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
package pjq.springboot.config.cache;

import java.lang.annotation.Annotation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.AnnotationUtils;
import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;
import pjq.commons.utils.CheckUtils;
import pjq.commons.utils.DefaultValueGetter;
import pjq.springboot.config.multitenant.MultiTenantInfoHolder;
import pjq.springboot.constant.CacheConstants;

/**
 * spring集成缓存的自定义key生成器
 *
 * @author pengjianqiang
 * @date 2021-06-13
 */
@Slf4j
@Configuration
public class CacheKeyGeneratorConfig {
    /**
     * 加载多租户缓存key前缀修饰器<br>
     * 这里的beanName也要配置，因为{@link CacheKeyPrefixDecorator#WITH_PREFIX}等要用到固定的beanName
     *
     * @return
     */
    @Bean(CacheKeyPrefixDecorator.BEAN_NAME)
    @ConditionalOnMissingBean
    @ConditionalOnBean(MultiTenantInfoHolder.class)
    @Primary
    @RefreshScope
    public CacheKeyPrefixDecorator multiTenantCacheKeyPrefixDecorator(MultiTenantInfoHolder multiTenantInfoHolder) {
        log.info("加载多租户缓存Key前缀修饰器");
        return new CacheKeyPrefixDecorator() {
            @Override
            public String getPrefix() {
                //@Cacheable等注解生成的key也会用到该方法获取前缀
                //super.getPrefix()获取到的值不会为空
                return DefaultValueGetter.getValue(MultiTenantInfoHolder.UNKNOWN_TENANT_NAME,
                        multiTenantInfoHolder.getContextTenantName())
                        + CacheConstants.DEFAULT_SEPARATOR + super.getPrefix();
            }
        };
    }

    @Bean(CacheKeyPrefixDecorator.BEAN_NAME)
    @ConditionalOnMissingBean
    @RefreshScope
    public CacheKeyPrefixDecorator cacheKeyPrefixDecorator() {
        log.info("加载缓存Key前缀修饰器");
        return new CacheKeyPrefixDecorator();
    }

    /**
     * 该key生成器会对所有参数转成json串，如果不需要这种方式的话则自行指定key<br>
     * 例如如果参数中有复杂对象的集合，建议还是自行指定key
     *
     * @return
     */
    @Bean(CacheConstants.CUSTOM_KEY_GENERATOR_NAME)
    public KeyGenerator customKeyGenerator(CacheKeyPrefixDecorator cacheKeyPrefixDecorator) {
        //先引用一次该bean以提早注册，否则会在应用启动完成后再注册
        //不先引用也不影响bean生成，只是这里的log日志输出顺序会比CacheKeyPrefixDecorator的早，阅读上容易理解错误
        cacheKeyPrefixDecorator.getPrefix();

        log.info("加载@Cacheable等注解的缓存key生成器");
        return (target, method, params) -> {
            StringBuilder key = new StringBuilder(cacheKeyPrefixDecorator.getPrefix());

            //从方法获取缓存相关注解
            Class<?> targetClass = target.getClass();
            Class<? extends Annotation> cacheAnnoClass = null;
            if (method.isAnnotationPresent(CacheEvict.class)) {
                cacheAnnoClass = CacheEvict.class;
            } else if (method.isAnnotationPresent(CachePut.class)) {
                cacheAnnoClass = CachePut.class;
            } else {
                cacheAnnoClass = Cacheable.class;
            }

            //通过cacheNames属性获取其值，因为@CacheConfig没有value属性，而4个注解都有的是cacheNames属性
            String attrName = "cacheNames";
            String[] cacheNames = (String[]) AnnotationUtils.getValue(
                    AnnotationUtils.findAnnotation(method, cacheAnnoClass), attrName);
            String targetName = CheckUtils.isNotEmpty(cacheNames) ? cacheNames[0] : null;

            if (CheckUtils.isEmpty(targetName)) {
                //尝试从类上获取缓存相关注解
                if (targetClass.isAnnotationPresent(CacheEvict.class)) {
                    cacheAnnoClass = CacheEvict.class;
                } else if (targetClass.isAnnotationPresent(CachePut.class)) {
                    cacheAnnoClass = CachePut.class;
                } else if (targetClass.isAnnotationPresent(Cacheable.class)) {
                    cacheAnnoClass = Cacheable.class;
                } else {
                    cacheAnnoClass = CacheConfig.class;
                }

                String[] cacheNamesInClass = (String[]) AnnotationUtils.getValue(
                        AnnotationUtils.findAnnotation(targetClass, cacheAnnoClass), attrName);
                targetName = CheckUtils.isNotEmpty(cacheNamesInClass) ? cacheNamesInClass[0] : null;
                if (CheckUtils.isEmpty(targetName)) {
                    targetName = targetClass.getName().toLowerCase();
                }
            }
            key.append(targetName + CacheConstants.DEFAULT_SEPARATOR);
            key.append(method.getName());
            if (CheckUtils.isNotEmpty(params)) {
                key.append(CacheConstants.DEFAULT_SEPARATOR + JSON.toJSONString(params));
            }
            return key.toString();
        };
    }
}