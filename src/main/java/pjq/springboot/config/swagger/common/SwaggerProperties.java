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
package pjq.springboot.config.swagger.common;

import java.util.Set;
import java.util.function.Predicate;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import lombok.Data;
import pjq.commons.utils.CheckUtils;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.RequestHandlerSelectors;

/**
 * swagger配置文件类
 *
 * @author pengjianqiang
 * @date 2021年5月14日
 */
@Data
@ConfigurationProperties("pjq.swagger")
@RefreshScope
public class SwaggerProperties {
    /**
     * 是否开启swagger，生产环境一般关闭，所以这里定义一个变量
     */
    private boolean enabled = true;

    /**
     * 如果接口文档有聚合网关，则配置网关的context-path<br>
     * 默认为空字符串
     */
    private String gatewayContextPath = "";

    /**
     * 项目应用名
     */
    private String applicationTitle;

    /**
     * 项目版本信息
     */
    private String applicationVersion;

    /**
     * 项目描述信息
     */
    private String applicationDescription;

    /**
     * 接口调试地址
     */
    private String host;

    /**
     * 要显示API的Controller包路径
     */
    private Set<String> basePackages;

    /**
     * 排除部分不显示API的Controller
     */
    private Set<String> excludeControllers;

    /**
     * 返回swagger使用的api选择器
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    Predicate<RequestHandler> apiSelectors() {
        Predicate<RequestHandler> basePackagesPredicate = RequestHandlerSelectors.none();
        for (String basePackage : basePackages) {
            basePackagesPredicate = basePackagesPredicate.or(RequestHandlerSelectors.basePackage(basePackage));
        }

        if (CheckUtils.isNotEmpty(excludeControllers)) {
            Predicate<RequestHandler> excludeControllersPredicate = requestHandler -> !excludeControllers
                    .contains(requestHandler.declaringClass().getName());
            return basePackagesPredicate.and(excludeControllersPredicate);
        } else {
            return basePackagesPredicate;
        }
    }
}