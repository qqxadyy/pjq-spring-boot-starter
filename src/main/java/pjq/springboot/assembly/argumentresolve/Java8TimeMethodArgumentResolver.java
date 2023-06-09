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
package pjq.springboot.assembly.argumentresolve;

import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import pjq.commons.utils.CheckUtils;
import pjq.commons.utils.DateTimeUtils;

/**
 * Controller接收{@link LocalDateTime}或{@link LocalDate}参数时的处理器(spring mvc默认不支持)<br>
 * post json的情况不经过该处理器<br>
 * 参数用{@link RequestParam}注解的话，也不经过该处理器，这种情况建议直接使用{@link Date}<br>
 *
 * @author pengjianqiangs
 * @date 2019-01-09
 */
public interface Java8TimeMethodArgumentResolver<T> extends HandlerMethodArgumentResolver {
    @Override
    public default boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(getTClass());
    }

    @Override
    public default Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        WebMvcDateCustomEditor dateEditor = new WebMvcDateCustomEditor();
        dateEditor.setAsText(webRequest.getParameter(parameter.getParameterName()));
        Date dateObj = Date.class.cast(dateEditor.getValue());
        if (CheckUtils.isNull(dateObj)) {
            return null;
        }

        Class<T> tClass = getTClass();
        if (LocalDateTime.class.equals(tClass)) {
            return DateTimeUtils.dateToLocalDateTime(dateObj);
        } else if (LocalDate.class.equals(tClass)) {
            return DateTimeUtils.dateToLocalDate(dateObj);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public default Class<T> getTClass() {
        try {
            return (Class<T>) Class
                    .forName(((ParameterizedType) getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0]
                                     .getTypeName());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public class LocalDateMethodArgumentResolver implements Java8TimeMethodArgumentResolver<LocalDate> {
    }

    public class LocalDateTimeMethodArgumentResolver implements Java8TimeMethodArgumentResolver<LocalDateTime> {
    }
}