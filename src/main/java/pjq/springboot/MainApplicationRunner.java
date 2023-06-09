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
package pjq.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import lombok.Builder;
import pjq.commons.utils.CheckUtils;
import pjq.springboot.utils.othersupport.MainStarterSysPropSetter;

/**
 * 简单封装的启动类<br>
 * 不需要以下内容的应用，直接用回Spring的启动方式即可<br>
 * 1.会配置log4j2日志相关信息<br>
 * 2.会设置允许循环依赖
 *
 * @author pengjianqiang
 * @date 2022-09-04
 */
@Builder
public class MainApplicationRunner {
    /**
     * 应用执行run方法前需要的操作
     */
    private CustomConsumer<SpringApplication, Class<?>, Logger> beforeRun;

    /**
     * 应用启动完成后需要的操作<br>
     * 通常是一些不需要在bean的注解方法中执行的操作，例如打印一些日志
     */
    private CustomConsumer<SpringApplication, Class<?>, Logger> finishRun;

    public void run(Class<?> starterClass, String... args) {
        MainStarterSysPropSetter.doSet(starterClass);

        //因为MainStarterSysPropSetter.doSet里面会自动设置日志文件相关属性
        //而在这之前如果使用了@Slf4j注解的话，会报错日志文件路径错误，所以要doSet之后，再在要使用时获取log对象
        Logger log = LoggerFactory.getLogger(starterClass);

        SpringApplication app = new SpringApplication(starterClass);

        try {
            //SpringBoot2.6.0后默认不自动处理循环依赖，需要设为true
            //因为依赖的其它框架中可能不少地方都有循环依赖，框架代码还是不方便都修改，需要用配置的方式
            //另外配置文件中的spring.main.allow-circular-references=true不生效，原因不明，所以在这里设置
            app.setAllowCircularReferences(true);
        } catch (NoSuchMethodError e) {
            //旧版本没有setAllowCircularReferences方法，则不需要处理
        }

        //启动
        if (CheckUtils.isNotNull(beforeRun)) {
            beforeRun.accept(app, starterClass, log);
        }
        app.run(args);
        if (CheckUtils.isNotNull(finishRun)) {
            finishRun.accept(app, starterClass, log);
        }
    }

    @FunctionalInterface
    public static interface CustomConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }
}