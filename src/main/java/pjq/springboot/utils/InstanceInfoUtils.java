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
package pjq.springboot.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pjq.commons.utils.DefaultValueGetter;

/**
 * 获取服务实例相关信息的工具类
 *
 * @author pengjianqiang
 * @date 2022-09-10
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InstanceInfoUtils {
    public static final String DEFAULT_LOCAL_IP = "127.0.0.1";

    /**
     * 获取服务实例的IP
     *
     * @return
     */
    public static String getInstanceIp() {
        return DefaultValueGetter.get(InstanceInfoUtils::getLocalIp, getEnvHost());
    }

    private static String getEnvHost() {
        //获取HOST的环境变量值(docker容器内使用)
        return System.getenv("HOST");
    }

    private static String getLocalIp() {
        // 根据网卡取本机配置的IP
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface current = interfaces.nextElement();
                if (!current.isUp() || current.isLoopback() || current.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = current.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr.isLoopbackAddress()) {
                        continue; // 去掉还回和虚拟地址
                    }
                    if (addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
            return DEFAULT_LOCAL_IP;
        } catch (Exception e) {
            return DEFAULT_LOCAL_IP;
        }
    }
}