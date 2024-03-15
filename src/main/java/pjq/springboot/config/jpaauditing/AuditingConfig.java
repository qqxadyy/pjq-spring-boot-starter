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

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import lombok.extern.slf4j.Slf4j;
import pjq.springboot.assembly.annotation.condition.ConditionalOnSpringCommonWebApplication;
import pjq.springboot.config.web.login.LoginUserInfoAware;

/**
 * JPA审计中自动获取审计人的名称、id等
 *
 * @author pengjianqiang
 * @date 2021-05-28
 * @see AuditingEntityExtendListener
 */
@Slf4j
@Configuration
@ConditionalOnSpringCommonWebApplication
@ConditionalOnClass({ EnableJpaAuditing.class, AuditorAware.class })
public class AuditingConfig {
    @Bean
    @ConditionalOnMissingBean
    public <T, ID> AuditorExtendAware<T, ID> auditorExtendAware(LoginUserInfoAware<T, ID> loginUserInfoAware) {
        log.info("加载用于自动保存数据记录操作人唯一标识等的扩展AuditorAware");
        return new AuditorExtendAware<T, ID>() {
            @Override
            public Optional<T> getCurrentAuditor() {
                return Optional.ofNullable(loginUserInfoAware.getUserName());
            }

            @Override
            public Optional<ID> getCurrentAuditorId() {
                return Optional.ofNullable(loginUserInfoAware.getUserId());
            }

            @Override
            public Optional<String> getCurrentAuditorName() {
                return Optional.ofNullable(loginUserInfoAware.getName());
            }
        };
    }
}