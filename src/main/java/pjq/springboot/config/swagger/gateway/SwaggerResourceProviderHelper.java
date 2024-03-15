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
package pjq.springboot.config.swagger.gateway;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.cloud.client.discovery.DiscoveryClient;

import pjq.commons.utils.CheckUtils;

/**
 * 网关聚合swagger文档的辅助对象<br>
 * 如果应用本身有特定要求，可以自行定义Bean并重写相关方法<br>
 * 当swagger没有开启时，本对象Bean不会创建
 *
 * @author pengjianqiang
 * @date 2023-05-17
 */
public class SwaggerResourceProviderHelper {
    /**
     * 获取全部的应用名<br>
     * 默认是通过{@link DiscoveryClient#getServices()}获取已注册的应用名集合<br>
     * 默认情况下存在问题：如果启动网关应用时某个应用还没有注册到注册中心中，那到该应用的接口文档url路由将不会被处理，即接口文档聚合失败<br>
     * 不过只是接口文档的聚合，不影响实际业务且生产环境会禁用swagger，所以问题可忽略<br>
     * 如果不想使用默认方式，则自行定义Bean并重写本方法，获取全部的应用名
     *
     * @param discoveryClient
     * @return
     */
    public List<String> getServices(DiscoveryClient discoveryClient) {
        return discoveryClient.getServices();
    }

    /**
     * 判断是否为需要聚合显示接口文档的应用<br>
     * 1.默认是应用名带"registry"、"config"和"gateway"的不需要聚合<br>
     * 2.如果应用本身有特殊要求，可以自行定义Bean并重写本方法。实现时可用以下方式<br>
     * {@code super.needGatherServiceApi().and(serviceName->{
     * //应用需要实现的逻辑
     * })}<br>
     * 使用super.needGatherServiceApi()可以直接不聚合应用名带有gateway和config的应用，减少实现代码
     *
     * @return
     */
    public Predicate<String> needGatherServiceApi() {
        return serviceName -> {
            if (CheckUtils.isEmpty(serviceName)) {
                return false;
            }
            String lowerServiceName = serviceName.toLowerCase();
            return !lowerServiceName.contains("registry")
                    && !lowerServiceName.contains("config")
                    && !lowerServiceName.contains("gateway");
        };
    }

    /**
     * 获取聚合后每个模块的接口文档地址<br>
     * 默认为：http://ip:port/contextPath/front/swagger/serviceX/v2/api-docs<br>
     * 如果应用本身有特殊要求，例如对特定前缀会鉴权之类的，可以自行定义Bean并重写本方法
     *
     * @param contextPath
     * @param serviceName
     * @return
     */
    public String genApiUri(String contextPath, String serviceName) {
        return contextPath + "/front/swagger/"
                + (CheckUtils.isEmpty(serviceName) ? "service" : serviceName.toLowerCase())
                + SwaggerResourceProvider.API_V2_URI;
    }

    /**
     * 返回服务对应的描述(用于显示到接口文档页面中)<br>
     * 默认服务名用作描述
     *
     * @param serviceName
     * @return
     */
    public String getServiceDesc(String serviceName) {
        return CheckUtils.isEmpty(serviceName) ? serviceName : serviceName.toLowerCase();
    }
}