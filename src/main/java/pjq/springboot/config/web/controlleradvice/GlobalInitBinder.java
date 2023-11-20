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
package pjq.springboot.config.web.controlleradvice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import javax.annotation.PostConstruct;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import pjq.springboot.assembly.annotation.condition.ConditionalOnSpringCommonWebApplication;
import pjq.springboot.assembly.argumentresolve.WebMvcDateCustomEditor;
import pjq.springboot.assembly.argumentresolve.WebMvcDateCustomEditor.WebMvcLocalDateCustomEditor;
import pjq.springboot.assembly.argumentresolve.WebMvcDateCustomEditor.WebMvcLocalDateTimeCustomEditor;

/**
 * 全局数据绑定操作
 *
 * @author pengjianqiang
 * @date 2023-05-10
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnSpringCommonWebApplication
public class GlobalInitBinder {
    @PostConstruct
    public void init() {
        log.info("加载全局的接口参数绑定器");
    }

    /**
     * 执行Controller方法之前的数据绑定操作
     *
     * @param binder
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        //接收日期类型或对象中有日期类型的参数处理(spring.mvc.date-format的配置只能支持配置的格式，其它格式的支持不了，所以用这种方式)
        //另外post json的情况在这里不会生效
        binder.registerCustomEditor(Date.class, new WebMvcDateCustomEditor());
        binder.registerCustomEditor(LocalDate.class, new WebMvcLocalDateCustomEditor());
        binder.registerCustomEditor(LocalDateTime.class, new WebMvcLocalDateTimeCustomEditor());
    }
}