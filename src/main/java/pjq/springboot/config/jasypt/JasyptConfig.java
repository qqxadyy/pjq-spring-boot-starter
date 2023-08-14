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

import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.ulisesbocchio.jasyptspringboot.configuration.EnableEncryptablePropertiesConfiguration;
import com.ulisesbocchio.jasyptspringboot.configuration.EncryptablePropertyResolverConfiguration;
import com.ulisesbocchio.jasyptspringboot.configuration.EnvCopy;
import com.ulisesbocchio.jasyptspringbootstarter.JasyptSpringBootAutoConfiguration;

import pjq.springboot.config.jasypt.JasyptPasswordGetter.DefaultJasyptPasswordGetter;
import pjq.springboot.utils.JasyptUtils;

/**
 * Jasypt配置文件加密配置类<br>
 * {@link #BEAN_NAME}对象也可用于简单的加解密逻辑
 *
 * @author pengjianqiang
 * @date 2021-08-09
 */
@Configuration
@ConditionalOnClass({ JasyptSpringBootAutoConfiguration.class, EnableEncryptablePropertiesConfiguration.class,
        StringEncryptor.class })
public class JasyptConfig {
    public static final String BEAN_NAME = "jasyptStringEncryptor";
    public static final String BEAN_NAME_FOR_SM4 = "sm4StringEncryptor";

    @Bean
    @ConditionalOnMissingBean
    public JasyptPasswordGetter jasyptPasswordGetter() {
        return new DefaultJasyptPasswordGetter();
    }

    /**
     * 自定义{@link StringEncryptor}<br>
     * bean名必须用jasyptStringEncryptor<br>
     * 本bean必须创建，不能使用@ConditionalOnXXX的注解限制创建条件，否则会直接创建jasypt默认的对象<br>
     * 如果本bean的加密配置不符合需求，则通过配置jasypt.encryptor.bean指定其它的bean名称，再创建对应的bean即可
     *
     * @return
     * @see EncryptablePropertyResolverConfiguration#stringEncryptor(EnvCopy, BeanFactory)
     */
    @Bean(BEAN_NAME)
    public StringEncryptor stringEncryptor(JasyptPasswordGetter jasyptPasswordGetter) {
        return JasyptUtils.buildStringEncryptor(jasyptPasswordGetter.getPassword());
    }

    /**
     * 创建使用国密SM4算法的Jasypt加密器并作为Bean对象<br>
     * 可以配置jasypt.encryptor.bean=sm4StringEncryptor，则配置文件的加解密使用本Bean处理
     *
     * @param jasyptPasswordGetter
     * @return
     */
    @Bean(BEAN_NAME_FOR_SM4)
    public StringEncryptor sm4StringEncryptor(JasyptPasswordGetter jasyptPasswordGetter) {
        return JasyptUtils.buildSM4StringEncryptor(jasyptPasswordGetter.getPassword());
    }
}