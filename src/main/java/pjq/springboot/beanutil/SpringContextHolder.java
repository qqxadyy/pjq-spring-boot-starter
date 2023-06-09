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
package pjq.springboot.beanutil;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import pjq.commons.utils.CheckUtils;

/**
 * {@link ApplicationContext}对象持有类
 *
 * @author pengjianqiang
 * @date 2021年4月12日
 */
@Component(SpringContextHolder.BEAN_NAME)
public final class SpringContextHolder implements ApplicationContextAware {
    public static final String BEAN_NAME = "springContextHolder";

    private static class InstanceHolder {
        private static ApplicationContext context;
    }

    private SpringContextHolder() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        setContext(applicationContext);
    }

    private static void setContext(ApplicationContext applicationContext) {
        if (CheckUtils.isNull(InstanceHolder.context)) {
            InstanceHolder.context = applicationContext;
        }
    }

    public static ApplicationContext getContext() {
        return InstanceHolder.context;
    }

    public static <T> T getBean(String beanName, Class<T> targetClass) {
        return getContext().getBean(beanName, targetClass);
    }

    public static Object getBean(String beanName) {
        return getContext().getBean(beanName);
    }

    public static <T> T getBean(Class<T> clazz) {
        return getContext().getBean(clazz);
    }

    public static <T> String[] getBeanNamesForType(Class<T> clazz) {
        return getContext().getBeanNamesForType(clazz);
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return getContext().getBeansOfType(clazz);
    }

    public static boolean containsBean(String name) {
        return getContext().containsBean(name);
    }

    public static boolean isSingleton(String name) {
        return getContext().isSingleton(name);
    }
}