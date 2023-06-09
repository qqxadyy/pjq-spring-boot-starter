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
package pjq.springboot.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Configuration;

/**
 * 本组件的旧版兼容自动配置类<br>
 * 用于兼容旧版本SpringBoot使用spring.factorise加载自动配置类的方式<br>
 * 旧版本没有{@link AutoConfiguration}注解
 *
 * @author pengjianqiang
 * @date 2023-05-09
 */
@Configuration
@ConditionalOnMissingClass("org.springframework.boot.autoconfigure.AutoConfiguration")
public class PjqOldVersionAutoConfiguration extends PjqAutoConfigurationBase {
}