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
package pjq.springboot.config.swagger.gateway;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import pjq.commons.utils.collection.CollectionUtils;
import pjq.springboot.assembly.annotation.condition.ConditionalOnSpringGatewayWebApplication;
import pjq.springboot.constant.SwaggerConstants;
import springfox.documentation.swagger.configuration.SwaggerCommonConfiguration;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

/**
 * swagger3配置<br>
 * 用于gateway聚合各个子模块的接口文档后，获取子模块对应的接口文档地址
 *
 * @author pengjianqiang
 * @date 2021-05-14
 */
@Primary //覆盖InMemorySwaggerResourcesProvider
@ConditionalOnSpringGatewayWebApplication
@ConditionalOnClass(SwaggerCommonConfiguration.class)
@ConditionalOnProperty(value = SwaggerConstants.SWAGGER_ENABLED, havingValue = "true", matchIfMissing = true)
@Component
public class SwaggerResourceProvider implements SwaggerResourcesProvider {
    public static final String API_URI = "/v3/api-docs";

    public static final String API_V2_URI = "/v2/api-docs";

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Resource
    private DiscoveryClient discoveryClient;

    @Resource
    private ObjectProvider<SwaggerResourceProviderHelper> swaggerResourceProviderHelperProvider;

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();

        swaggerResourceProviderHelperProvider.ifAvailable(swaggerResourceProviderHelper -> {
            //不从RouteLocator里面解析，直接从配置文件中解析
            Collection<String> needGatherServices = CollectionUtils.filter(
                    swaggerResourceProviderHelper.getServices(discoveryClient),
                    swaggerResourceProviderHelper.needGatherServiceApi());

            // 记录已经添加过的应用名(因为一个应用可以有多个实例)
            Set<String> dealed = new HashSet<>();
            needGatherServices.forEach(serviceName -> {
                String url = swaggerResourceProviderHelper.genApiUri(contextPath, serviceName);
                if (!dealed.contains(url)) {
                    dealed.add(url);
                    SwaggerResource swaggerResource = new SwaggerResource();
                    swaggerResource.setUrl(url);
                    swaggerResource.setName(swaggerResourceProviderHelper.getServiceDesc(serviceName));
                    resources.add(swaggerResource);
                }
            });
        });

        return resources;
    }
}