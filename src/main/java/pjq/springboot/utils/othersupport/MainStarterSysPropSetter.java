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
package pjq.springboot.utils.othersupport;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pjq.commons.utils.CheckUtils;
import pjq.springboot.utils.InstanceInfoUtils;

/**
 * 用于启动类启动前设置一些固定的系统变量<br>
 * 只能在启动类的run方法前调用
 *
 * @author pengjianqiang
 * @date 2021年5月14日
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MainStarterSysPropSetter {
    public static void doSet(Class<?> startClass) {
        String starterClassName = startClass.getName();
        doSet(starterClassName);
    }

    public static void doSet(String starterClassName) {
        //系统变量不存在时才设置
        if (CheckUtils.isEmpty(System.getProperty("sys.log.application.name"))) {
            System.setProperty("sys.log.application.name", getApplicationName(starterClassName));
        }
        if (CheckUtils.isEmpty(System.getProperty("sys.log.ip"))) {
            System.setProperty("sys.log.ip", InstanceInfoUtils.getInstanceIp());
        }
    }

    private static String getApplicationName(String starterClassName) {
        String simpleName = starterClassName.substring(starterClassName.lastIndexOf(".") + 1);
        simpleName = simpleName.endsWith("Application")
                ? simpleName.substring(0, simpleName.length() - "Application".length()) : simpleName;

        String applicationName = "";
        for (int i = 0, size = simpleName.length(); i < size; i++) {
            char s = simpleName.charAt(i);
            if (String.valueOf(s).toLowerCase().equals(String.valueOf(s))) {
                applicationName += s;
            } else {
                applicationName += (i > 0 ? "_" : "") + String.valueOf(s).toLowerCase();
            }
        }
        return applicationName;
    }
}