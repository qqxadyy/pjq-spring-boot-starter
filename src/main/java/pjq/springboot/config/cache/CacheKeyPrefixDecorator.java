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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;

import pjq.commons.utils.CheckUtils;
import pjq.commons.utils.DefaultValueGetter;
import pjq.springboot.constant.CacheConstants;

/**
 * 缓存key前缀修饰器<br>
 * 用于为缓存key加上统一的前缀
 *
 * @author pengjianqiang
 * @date 2021-09-18
 */
public class CacheKeyPrefixDecorator {
    public static final String BEAN_NAME = "cacheKeyPrefixDecorator";
    private static final String BEAN_REF = "@" + BEAN_NAME;

    /**
     * 用于SPEL表达式中对参数key加上基础缓存key前缀<br>
     * 1.生成的key需要后续带上参数值时的用法：<br>
     * {@code key = CacheKeyPrefixDecorator.WITH_PREFIX + "('一般填方法名') + #参数名"}<br>
     * 2.生成的key不需要后续带上参数值时的用法：<br>
     * {@code key = CacheKeyPrefixDecorator.WITH_PREFIX + "('一般填方法名', false)"}
     */
    public static final String WITH_PREFIX = BEAN_REF + ".withPrefix";

    /**
     * 基础缓存key前缀<br>
     * {@link CacheConstants#CACHE_KEY_BASE_PREFIX}中已有配置冒号，获取前缀后不用再加冒号
     */
    @Value(CacheConstants.CACHE_KEY_BASE_PREFIX)
    private String basePrefix;

    /**
     * 获取缓存key前缀
     *
     * @return
     */
    public String getPrefix() {
        //要去掉可能存在的"/"号
        return DefaultValueGetter.getValue("baseCache", basePrefix.replaceAll("/", ""));
    }

    /**
     * 为缓存key添加基础缓存key前缀<br>
     * 另外会在最后自动补上{@link CacheConstants#DEFAULT_SEPARATOR}，这个主要是用于{@link Cacheable}等注解中使用{@link #WITH_PREFIX}时，可以和参数值分隔开<br>
     * 如果不需要自动在key后补上分隔符，则直接使用{@link #getPrefix()}方法获取前缀后再拼上后续内容即可<br>
     * 或使用{@link #withPrefix(String, boolean)}
     *
     * @param key
     * @return
     */
    public String withPrefix(String key) {
        return withPrefix(key, true);
    }

    /**
     * 为缓存key添加基础缓存key前缀
     *
     * @param key
     * @param autoAppend
     *         是否在最后自动补上{@link CacheConstants#DEFAULT_SEPARATOR}
     * @return
     */
    public String withPrefix(String key, boolean autoAppend) {
        if (CheckUtils.isEmpty(key)) {
            key = "unknownKey";
        }

        StringBuilder resultKey = new StringBuilder();
        String prefix = getPrefix();
        if (!key.startsWith(prefix)) {
            resultKey.append(prefix);
        }
        resultKey.append(key);
        if (autoAppend && !key.endsWith(CacheConstants.DEFAULT_SEPARATOR)) {
            resultKey.append(CacheConstants.DEFAULT_SEPARATOR);
        }
        return resultKey.toString();
    }
}