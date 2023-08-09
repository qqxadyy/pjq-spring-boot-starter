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
package pjq.springboot.config.multitenant;

/**
 * 多租户信息获取器<br>
 * 由具体工程实现该Bean中的方法
 *
 * @author pengjianqiang
 * @date 2023-06-07
 */
public abstract class MultiTenantInfoHolder {
    private static boolean IS_MULTI_TENANT_ENABLED;
    public static final String UNKNOWN_TENANT_NAME = "unknownTenant";

    static {
        //用静态方法设置默认值，不在变量定义时使用final，即必须显式使用本类才表示开启多租户模式
        IS_MULTI_TENANT_ENABLED = true;
    }

    /**
     * 是否已启用多租户模式
     *
     * @return
     */
    public static boolean isMultiTenantEnabled() {
        return IS_MULTI_TENANT_ENABLED;
    }

    /**
     * 获取当前线程的租户名称
     */
    public String getContextTenantName() {
        return UNKNOWN_TENANT_NAME;
    }
}