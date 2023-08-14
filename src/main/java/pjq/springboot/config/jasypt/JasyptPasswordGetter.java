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
package pjq.springboot.config.jasypt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import pjq.commons.utils.CheckUtils;
import pjq.commons.utils.StringUtils;

/**
 * Jasypt加解密密码获取器<br>
 * 根据不同工程实现对应的对象
 *
 * @author pengjianqiang
 * @date 2023-07-05
 */
@FunctionalInterface
public interface JasyptPasswordGetter {
    /**
     * 实现对象必须显式实现本方法，但是只需要实现成空方法即可<br>
     * 实际的密码值通过@{@link JasyptPassword}注解进行配置，使用注解是因为需要限制配置的密码值必须为常量，而不是动态值<br>
     * 密码值可用找一些在线生成RSA公私钥的网站，生成后把去除头尾部分的公钥作为密码；也可找一些生成随机密码的网站生成<br>
     * <br>
     * 密码要求：<br>
     * <ul>
     *     <li>不包含中文</li>
     *     <li>长度不小于128</li>
     * </ul>
     *
     * @see DefaultJasyptPasswordGetter
     */
    void emptyMethod();

    /**
     * 获取配置的密码值
     *
     * @return
     */
    default String getPassword() {
        try {
            Method method = this.getClass().getMethod("emptyMethod");
            JasyptPassword anno = method.getAnnotation(JasyptPassword.class);
            String password = anno.value();
            if (CheckUtils.isEmpty(password)) {
                throw new RuntimeException();
            }
            if (StringUtils.containChinese(password)) {
                throw new RuntimeException();
            }
            password = password.trim();
            if (password.length() < 128) {
                throw new RuntimeException(); //密码要求有一定长度
            }
            if (CustomAlgorithm.SM4.equals(anno.algorithm())) {
                //如果使用国密SM4算法，则截断配置的密码值(原因查看SMUtils.sm4CheckKey)
                password = password.substring(0, 16);
            }
            return password;
        } catch (Exception e) {
            throw new RuntimeException("获取Jasypt的密码失败，请检查是否有按要求实现JasyptPasswordGetter对象");
        }
    }

    /**
     * 支持解析Jasypt加解密密码的算法
     */
    enum CustomAlgorithm {
        /**
         * Jasypt默认算法
         */
        JASYPT_DEFAULT,

        /**
         * 国密SM4算法
         */
        SM4
    }

    /**
     * Jasypt加解密的密码配置注解
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface JasyptPassword {
        String value();

        CustomAlgorithm algorithm() default CustomAlgorithm.JASYPT_DEFAULT;
    }

    /**
     * 默认Jasypt加解密密码获取器
     */
    class DefaultJasyptPasswordGetter implements JasyptPasswordGetter {
        @Override
        @JasyptPassword("MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAOWa8GVukzLLkAMHBkb/hjWbnyz8pEOnP8J2kd171k3F0GH8a4wujdwPqHRQHUKAclUgJP5h0P7IZJ96ePqzxJkCAwEAAQ==")
        public void emptyMethod() {
        }
    }
}