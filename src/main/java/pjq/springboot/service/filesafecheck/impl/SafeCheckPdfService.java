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
package pjq.springboot.service.filesafecheck.impl;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import pjq.springboot.service.filesafecheck.FileSafeCheckService;

/**
 * 检查pdf文件是否安全的服务<br>
 * 需要依赖pdfbox3.0.0<br>
 * 如果是依赖了pdfbox2.0.X的版本，则参考本类创建一个新的FileSafeCheckService实现，<br>
 * 并把加载pdf的方法改成{@code PDDocument.load(file)}<br>
 *
 * @author pengjianqiang
 * @date 2023-11-09
 */
@Service
@ConditionalOnClass(name = "org.apache.pdfbox.Loader")
public class SafeCheckPdfService implements FileSafeCheckService {
    @Override
    public boolean needCheck(File file, String mimeType, String extension) {
        return "pdf".equals(extension);
    }

    @Override
    public String notSafeMsg(String extension) {
        return "PDF文件可能含有不安全的JavaScript脚本";
    }

    @Override
    public boolean isSafe(File file, String mimeType, String extension) {
        try (PDDocument document = Loader.loadPDF(file)) {
            for (PDPage page : document.getPages()) {
                String cosObjectStr = Optional.ofNullable(page.getCOSObject())
                        .map(cosBase -> cosBase.toString().toLowerCase())
                        .orElse("");
                if (cosObjectStr.contains("javascript") || cosObjectStr.contains("cosname{js}")) {
                    return false;
                }
            }
        } catch (InvalidPasswordException e) {
            return true; //文件有加密按安全处理(因为浏览器打开也不能直接执行脚本)
        } catch (IOException e) {
            return true; //读取失败按安全处理
        }
        return true;
    }
}