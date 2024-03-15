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
package pjq.springboot.config.jpaauditing;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.domain.AuditorAware;

/**
 * 扩展{@link AuditorAware}用于获取当前审计人的id
 *
 * @param <T>
 *         审计人名称的类型
 * @param <ID>
 *         审计人ID的类型
 * @author pengjianqiang
 * @date 2021-05-28
 */
public interface AuditorExtendAware<T, ID> extends AuditorAware<T> {
    /**
     * 获取当前审计人的唯一标识，类似{@link #getCurrentAuditor()}
     *
     * @return
     */
    Optional<ID> getCurrentAuditorId();

    /**
     * 获取当前审计人的姓名，类似{@link #getCurrentAuditor()}
     *
     * @return
     */
    Optional<String> getCurrentAuditorName();

    /**
     * 获取其它需要在创建时自动插入的属性值<br>
     * 具体工程可重写该方法
     *
     * @return map的key是一个注解类型，类似{@link CreatedBy}的作用<br>
     * map的value是具体的属性值，没有或报错时返回null即可
     */
    default Map<Class<? extends Annotation>, Object> getOtherValuesWhileCreate() {
        return new HashMap<>();
    }

    /**
     * 获取其它需要在更新时自动插入的属性值<br>
     * 具体工程可重写该方法
     *
     * @return map的key是一个注解类型，类似{@link CreatedBy}的作用<br>
     * map的value是具体的属性值，没有或报错时返回null即可
     */
    default Map<Class<? extends Annotation>, Object> getOtherValuesWhileUpdate() {
        return new HashMap<>();
    }
}