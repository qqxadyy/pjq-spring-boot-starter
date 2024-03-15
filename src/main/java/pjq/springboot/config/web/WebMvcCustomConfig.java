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
package pjq.springboot.config.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Clob;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import lombok.extern.slf4j.Slf4j;
import pjq.commons.constant.DateTimePattern;
import pjq.commons.utils.CheckUtils;
import pjq.commons.utils.DateTimeUtils;
import pjq.commons.utils.StreamUtils;
import pjq.springboot.assembly.annotation.condition.ConditionalOnSpringCommonWebApplication;
import pjq.springboot.assembly.argumentresolve.Java8TimeMethodArgumentResolver.LocalDateMethodArgumentResolver;
import pjq.springboot.assembly.argumentresolve.Java8TimeMethodArgumentResolver.LocalDateTimeMethodArgumentResolver;
import pjq.springboot.beanutil.EnvironmentContextHolder;

/**
 * 扩展webMvc自定义配置<br>
 * 不用继承WebMvcConfigurerAdapter，这个已经被弃用
 *
 * @author pengjianqiang
 * @date 2018-11-22
 */
@Slf4j
@Configuration
@ConditionalOnSpringCommonWebApplication
@DependsOn(EnvironmentContextHolder.BEAN_NAME)
public class WebMvcCustomConfig implements WebMvcConfigurer {
    /**
     * 添加自定义Controller参数处理器
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        logAddResolver(LocalDateMethodArgumentResolver.class);
        resolvers.add(new LocalDateMethodArgumentResolver());
        logAddResolver(LocalDateTimeMethodArgumentResolver.class);
        resolvers.add(new LocalDateTimeMethodArgumentResolver());
    }

    private void logAddResolver(Class<?> resolverClass) {
        log.info("添加HandlerMethodArgumentResolver[{}]", resolverClass.getName());
    }

    /**
     * 保留框架jackson objectMapper原有设置的情况下扩展自定义设置
     *
     * @return
     */
    @SuppressWarnings("serial")
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        log.info("加载自定义的JSON类型接口参数及返回数据处理器");
        String dateTimePattern = EnvironmentContextHolder
                .getProperty("spring.jackson.date-format", DateTimePattern.PATTERN_DATETIME.value());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
        DateTimeFormatter timeFormatter = DateTimePattern.PATTERN_TIME.getFormatter();

        //返回对象是转成json时的LocalDateTime和LocalTime属性转换(LocalDate默认已支持格式yyyy-MM-dd)
        //LocalDateTime使用配置文件中的配置，保持统一;LocalTime去掉毫秒单位
        Map<Class<?>, JsonSerializer<?>> serializers = new HashMap<>();
        serializers.put(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        serializers.put(LocalTime.class, new LocalTimeSerializer(timeFormatter));

        //增加处理java.sql.Clob对象的序列化处理
        serializers.put(Clob.class, new ClobSerializer());

        //post json时对象中有LocalDateTime和LocalTime属性的转换(LocalDate默认已支持格式yyyy-MM-dd)
        //Date类型的属性不处理，即需要按要求传入yyyy-MM-dd HH:mm:ss格式的参数
        Map<Class<?>, JsonDeserializer<?>> deserializers = new HashMap<>();
        deserializers.put(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter) {
            @Override
            public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
                //重写deserialize方法，使其抛异常时尝试用其它格式的字符串转成日期
                try {
                    return super.deserialize(parser, context);
                } catch (Exception e) {
                    try {
                        //尝试转换yyyy-MM-dd格式
                        return DateTimeUtils.dateToLocalDateTime(DateTimeUtils.localDateToDate(
                                new LocalDateDeserializer(DateTimePattern.PATTERN_DEFAULT.getFormatter())
                                        .deserialize(parser, context)));
                    } catch (Exception e1) {
                        return new LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME).deserialize(parser,
                                context);
                    }
                }
            }
        });
        deserializers.put(LocalTime.class, new LocalTimeDeserializer(timeFormatter));

        return builder -> builder.serializersByType(serializers).deserializersByType(deserializers);
    }

    /**
     * java.sql.Clob的jackson序列化
     *
     * @author pengjianqiang
     * @date 2020-02-22
     */
    private static class ClobSerializer extends JsonSerializer<Clob> {
        @Override
        public void serialize(Clob clob, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            try {
                if (CheckUtils.isNotNull(clob)) {
                    try (BufferedReader br = new BufferedReader(clob.getCharacterStream())) {
                        gen.writeString(StreamUtils.joinString(br.lines()));
                    }
                } else {
                    gen.writeNull();
                }
            } catch (Exception e) {
                gen.writeNull();
            }
        }
    }
}