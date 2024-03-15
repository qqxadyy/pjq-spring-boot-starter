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
package pjq.springboot.assembly.argumentresolve;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import pjq.commons.constant.DateTimePattern;
import pjq.commons.utils.CheckUtils;
import pjq.commons.utils.DateTimeUtils;

/**
 * 自定义的Controller方法接收{@link Date}类型参数的转换器
 *
 * @author pengjianqiangs
 * @date 2018-09-25
 */
public class WebMvcDateCustomEditor extends PropertyEditorSupport {
    @Getter
    private static List<DateTimePattern> patterns = DateTimePattern.usuallyUseDateTimePattern(); //需要用静态属性去注入，否则不能直接在方法中使用

    @Getter
    @Setter
    private Date dateValue;

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        Object value = null;
        if (CheckUtils.isNotEmpty(text)) {
            String trimText = text.trim();
            try {
                value = DateTimeUtils.parseLocalDateTime(trimText);
            } catch (Exception e1) {
                value = null;
            }
            if (CheckUtils.isNotNull(value)) {
                //value不为空表示日期字符串为UTC格式的
                value = DateTimeUtils.localDateTimeToDate(LocalDateTime.class.cast(value));
            } else {
                for (int i = 0, size = patterns.size(); i < size; i++) {
                    try {
                        value = DateTimeUtils.parseDate(trimText, patterns.get(i));
                        break;
                    } catch (Exception e) {
                        CheckUtils.checkNotFalse(i < size - 1, "参数验证失败：参数值[".concat(trimText).concat("]不能转换为日期类型"));
                    }
                }
            }
        }
        setValue(value);
        setDateValue(Date.class.cast(value));
    }

    @Override
    public String getAsText() {
        String strValue = "";
        Date value = getDateValue();
        if (CheckUtils.isNotNull(value)) {
            for (int i = 0, size = patterns.size(); i < size; i++) {
                try {
                    strValue = DateTimeUtils.format(value, patterns.get(i));
                } catch (Exception e) {
                    CheckUtils.checkNotFalse(i < size - 1, "参数验证失败：日期[".concat(value.toString()).concat("]不能转换为字符串类型"));
                }
            }
        }
        return strValue;
    }

    public interface CustomJava8TimeEditor<T> {
        public default Object resolveDateValue(Date dateObj) {
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
    }

    /**
     * 自定义的controllor方法接收{@link LocalDate}类型参数的转换器
     *
     * @date 2019年1月9日
     */
    public static class WebMvcLocalDateCustomEditor extends WebMvcDateCustomEditor
            implements CustomJava8TimeEditor<LocalDate> {
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            super.setAsText(text);
            setValue(resolveDateValue(getDateValue()));
        }

        @Override
        public String getAsText() {
            return DateTimeUtils.format(getDateValue());
        }
    }

    /**
     * 自定义的controllor方法接收{@link LocalDateTime}类型参数的转换器
     *
     * @date 2019年1月9日
     */
    public static class WebMvcLocalDateTimeCustomEditor extends WebMvcDateCustomEditor
            implements CustomJava8TimeEditor<LocalDateTime> {
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            super.setAsText(text);
            setValue(resolveDateValue(getDateValue()));
        }

        @Override
        public String getAsText() {
            return DateTimeUtils.format(getDateValue(), DateTimePattern.PATTERN_DATETIME);
        }
    }
}