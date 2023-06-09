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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.StandardServletEnvironment;

import pjq.commons.utils.CheckUtils;
import pjq.commons.utils.DefaultValueGetter;

/**
 * {@link Environment}对象持有类
 *
 * @author pengjianqiang
 * @date 2021年5月31日
 */
@Component(EnvironmentContextHolder.BEAN_NAME)
public class EnvironmentContextHolder implements EnvironmentAware {
    public static final String BEAN_NAME = "environmentContextHolder";

    private static class InstanceHolder {
        private static Environment environment;
    }

    private static Pattern placeholderPattern = Pattern.compile("^\\$\\{.*\\}$");

    public EnvironmentContextHolder() {
    }

    @Override
    public void setEnvironment(Environment environment) {
        setEnv(environment);
    }

    public static void setEnv(Environment environment) {
        if (CheckUtils.isNull(InstanceHolder.environment)) {
            InstanceHolder.environment = environment;
        }
    }

    public static Environment getEnv() {
        return InstanceHolder.environment;
    }

    /**
     * 获取某个spring.profiles.include对应的配置文件信息<br>
     * 只支持.yml文件
     *
     * @param profileName
     *         配置文件的名,例如application-profileName.yml
     * @return
     */
    @SuppressWarnings("unchecked")
    public static PropertySource<Map<String, Object>> getPropertySource(String profileName) {
        StringBuffer fileName = new StringBuffer("application");
        if (CheckUtils.isNotEmpty(profileName)) {
            fileName.append("-").append(profileName);
            CheckUtils.checkNotFalse(Arrays.asList(getEnv().getActiveProfiles()).contains(profileName),
                                     "配置文件".concat(fileName.toString()).concat("不存在"));
        }
        fileName.append(".yml");
        PropertySource<?> source = ((StandardServletEnvironment) getEnv()).getPropertySources()
                .get("applicationConfig: [classpath:/".concat(fileName.toString()).concat("]"));
        return (PropertySource<Map<String, Object>>) source;
    }

    /**
     * 获取主配置文件(application.yml)的信息<br>
     * 只支持.yml文件
     *
     * @return
     */
    public static PropertySource<?> getMainPropertySource() {
        return getPropertySource(null);
    }

    /**
     * 获取全部spring.profiles.include对应的配置文件信息<br>
     * 只支持.yml文件
     *
     * @return
     */
    public static Map<String, PropertySource<Map<String, Object>>> getActivePropertySources() {
        return Arrays.stream(getEnv().getActiveProfiles())
                .collect(Collectors.toMap(activeProfile -> activeProfile, activeProfile -> {
                    PropertySource<Map<String, Object>> s = EnvironmentContextHolder.getPropertySource(activeProfile);
                    return Optional.ofNullable(s)
                            .orElseGet(() -> new OriginTrackedMapPropertySource(activeProfile, new HashMap<>()));
                }));
    }

    /**
     * 获取配置文件属性
     *
     * @param propertyNameWithPlaceholder
     *         带占位符的属性名，例如${propertyName}
     * @return
     */
    public static String getPropertyWithPalceHolder(String propertyNameWithPlaceholder) {
        return getEnv().resolvePlaceholders(propertyNameWithPlaceholder);
    }

    /**
     * 获取配置文件属性或系统环境属性
     *
     * @param propertyName
     *         普通的属性名或带占位符的属性名(例如${propertyName})
     * @return
     */
    public static String getProperty(String propertyName) {
        if (placeholderPattern.matcher(propertyName).matches()) {
            return getPropertyWithPalceHolder(propertyName);
        } else {
            return getEnv().getProperty(propertyName);
        }
    }

    public static String getProperty(String propertyName, String defaultValue) {
        return DefaultValueGetter.getValue(defaultValue, getProperty(propertyName));
    }

    public static Boolean getPropertyAsBoolean(String propertyName) {
        try {
            String val = getProperty(propertyName);
            return CheckUtils.isNotEmpty(val) ? Boolean.valueOf(val) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Boolean getPropertyAsBoolean(String propertyName, boolean defaultValue) {
        Boolean val = getPropertyAsBoolean(propertyName);
        return CheckUtils.isNotNull(val) ? val : defaultValue;
    }

    public static Integer getPropertyAsInt(String propertyName) {
        try {
            String val = getProperty(propertyName);
            return CheckUtils.isNotEmpty(val) ? Integer.valueOf(val) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer getPropertyAsInt(String propertyName, int defaultValue) {
        Integer val = getPropertyAsInt(propertyName);
        return CheckUtils.isNotNull(val) ? val : defaultValue;
    }

    public static Long getPropertyAsLong(String propertyName) {
        try {
            String val = getProperty(propertyName);
            return CheckUtils.isNotEmpty(val) ? Long.valueOf(val) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Long getPropertyAsLong(String propertyName, long defaultValue) {
        Long val = getPropertyAsLong(propertyName);
        return CheckUtils.isNotNull(val) ? val : defaultValue;
    }

    public static Double getPropertyAsDouble(String propertyName) {
        try {
            String val = getProperty(propertyName);
            return CheckUtils.isNotEmpty(val) ? Double.valueOf(val) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Double getPropertyAsDouble(String propertyName, double defaultValue) {
        Double val = getPropertyAsDouble(propertyName);
        return CheckUtils.isNotNull(val) ? val : defaultValue;
    }
}