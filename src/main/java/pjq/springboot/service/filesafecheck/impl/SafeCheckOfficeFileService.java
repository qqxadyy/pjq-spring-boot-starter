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

import org.apache.poi.poifs.macros.VBAMacroReader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import pjq.commons.utils.CheckUtils;
import pjq.commons.utils.DefaultValueGetter;
import pjq.commons.utils.FileTypeUtils;
import pjq.springboot.service.filesafecheck.FileSafeCheckService;

/**
 * 检查office文件是否安全的服务<br>
 * 需要依赖POI5
 *
 * @author pengjianqiang
 * @date 2023-11-09
 */
@Service
@ConditionalOnClass(name = "org.apache.poi.poifs.macros.VBAMacroReader")
public class SafeCheckOfficeFileService implements FileSafeCheckService {
    @Override
    public boolean needCheck(File file, String mimeType, String extension) {
        return FileTypeUtils.isCommonOfficeFile(file, mimeType);
    }

    @Override
    public String notSafeMsg(String extension) {
        return DefaultValueGetter.getValue("", extension).toUpperCase() + "文件可能含有不安全的宏";
    }

    @Override
    public boolean isSafe(File file, String mimeType, String extension) {
        try (VBAMacroReader vbaMacroReader = new VBAMacroReader(file)) {
            //有宏表示不安全(没有宏时也可能读出来一个空Map)
            return CheckUtils.isEmpty(vbaMacroReader.readMacros());
        } catch (IOException e) {
            return true; //读取失败按安全处理
        } catch (Exception e) {
            return true; //没有宏或其它报错也按安全处理(没有宏时会报错IllegalArgumentException: No VBA project found)
        }
    }
}