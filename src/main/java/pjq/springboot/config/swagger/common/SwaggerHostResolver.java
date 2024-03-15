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
package pjq.springboot.config.swagger.common;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import pjq.commons.utils.collection.CollectionUtils;
import pjq.springboot.assembly.annotation.condition.ConditionalOnSpringCommonWebApplication;
import pjq.springboot.constant.SwaggerConstants;
import springfox.documentation.oas.web.OpenApiTransformationContext;
import springfox.documentation.oas.web.WebMvcOpenApiTransformationFilter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.swagger.configuration.SwaggerCommonConfiguration;

/**
 * 处理springfox3.0.0设置了servers(swagger-ui文档页面中的下拉请求地址)不生效的问题
 * https://github.com/springfox/springfox/issues/3483
 */
@Component
@ConditionalOnSpringCommonWebApplication
@ConditionalOnClass(SwaggerCommonConfiguration.class)
@ConditionalOnProperty(value = SwaggerConstants.SWAGGER_ENABLED, havingValue = "true", matchIfMissing = true)
public class SwaggerHostResolver implements WebMvcOpenApiTransformationFilter {
    @Resource
    private SwaggerProperties swaggerProperties;

    @Override
    public OpenAPI transform(OpenApiTransformationContext<HttpServletRequest> context) {
        OpenAPI swagger = context.getSpecification();
        List<Server> servers4Gateway = new ArrayList<>(); //网关用的server地址
        List<Server> servers = swagger.getServers();
        servers.forEach(server -> {
            Server server4Gateway = new Server();
            server4Gateway.setUrl(server.getUrl() + swaggerProperties.getGatewayContextPath());
            server4Gateway.setDescription("网关用");
            servers4Gateway.add(server4Gateway);
        });

        //保留原配置的基础上增加网关用的地址
        swagger.setServers(CollectionUtils.transformToList(
                org.apache.commons.collections4.CollectionUtils.union(servers, servers4Gateway), server -> server));
        return swagger;
    }

    @Override
    public boolean supports(DocumentationType docType) {
        return docType.equals(DocumentationType.OAS_30);
    }
}