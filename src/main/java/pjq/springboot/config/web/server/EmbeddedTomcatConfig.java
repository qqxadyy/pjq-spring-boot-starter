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
package pjq.springboot.config.web.server;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import pjq.commons.utils.CheckUtils;

/**
 * 内嵌Tomcat自定义配置<br>
 * spring gateway因为使用Netty所以不会触发配置生效
 *
 * @author pengjianqiang
 * @date 2023-03-09
 */
@Slf4j
@Configuration
@ConditionalOnClass(name = "org.apache.catalina.core.StandardServer")
public class EmbeddedTomcatConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    /**
     * 自定义的tomcat docBase目录
     */
    @Value("${server.tomcat.custom-doc-base-dir:${server.tomcat.basedir:tomcatDir}/webapps}")
    private String customDocBaseDir;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        if (CheckUtils.isNotEmpty(customDocBaseDir)) {
            String baseMsg = "设置自定义Tomcat docBase目录[{}]";
            File dir = new File(customDocBaseDir);
            String dirPath;
            try {
                dirPath = dir.getCanonicalPath();
            } catch (Exception e) {
                dirPath = dir.getAbsolutePath();
            }
            if (!dir.exists()) {
                try {
                    dir.mkdirs(); //保证目录存在
                } catch (Exception e) {
                    log.info(baseMsg + "不成功，用默认值代替", dirPath);
                    return;
                }
            }
            factory.setDocumentRoot(dir);
            log.info(baseMsg, dirPath);
        }
    }
}