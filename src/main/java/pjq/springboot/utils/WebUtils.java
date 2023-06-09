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

import java.util.Optional;
import java.util.function.Predicate;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pjq.commons.utils.CheckUtils;

/**
 * 简单的Web应用工具类
 *
 * @author pengjianqiang
 * @date 2023-05-10
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WebUtils {
    private static final String[] IP_HEADER_NAMES = new String[]{"x-forwarded-for", "Proxy-Client-IP",
            "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
    private static final Predicate<String> IS_BLANK_IP = ip -> CheckUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip);

    /**
     * 获取访问应用的ip
     */
    public static String getClientIp() {
        return getClientIp(getRequest());
    }

    /**
     * 获取访问应用的ip
     *
     * @param request
     */
    public static String getClientIp(HttpServletRequest request) {
        if (CheckUtils.isNull(request)) {
            return null;
        }

        String remoteIp = null;
        for (String ipHeader : IP_HEADER_NAMES) {
            remoteIp = request.getHeader(ipHeader);
            if (!IS_BLANK_IP.test(remoteIp)) {
                break;
            }
        }

        if (IS_BLANK_IP.test(remoteIp)) {
            remoteIp = request.getRemoteAddr();
            if (InstanceInfoUtils.DEFAULT_LOCAL_IP.equals(remoteIp)) {
                return getLocalIp();
            }
        }

        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (!IS_BLANK_IP.test(remoteIp) && remoteIp.length() > 15) {
            //"***.***.***.***".length() = 15
            if (remoteIp.indexOf(",") > 0) {
                remoteIp = remoteIp.substring(0, remoteIp.indexOf(","));
            }
        }
        return remoteIp;
    }

    /**
     * 获取应用所在当前服务器的ip
     *
     * @return
     */
    public static String getLocalIp() {
        return InstanceInfoUtils.getInstanceIp();
    }

    private static Optional<ServletRequestAttributes> getThreadRequestAttributes() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(attributes -> attributes instanceof ServletRequestAttributes)
                .map(ServletRequestAttributes.class::cast);
    }

    /**
     * 获取当前线程的HttpServletRequest
     *
     * @return
     */
    public static HttpServletRequest getRequest() {
        return getThreadRequestAttributes().map(ServletRequestAttributes::getRequest).orElse(null);
    }

    /**
     * 获取当前线程的HttpServletResponse
     */
    public static HttpServletResponse getResponse() {
        return getThreadRequestAttributes().map(ServletRequestAttributes::getResponse).orElse(null);
    }

    /**
     * 获取当前线程的HttpSession
     */
    public static HttpSession getSession() {
        return Optional.ofNullable(getRequest()).map(HttpServletRequest::getSession).orElse(null);
    }
}