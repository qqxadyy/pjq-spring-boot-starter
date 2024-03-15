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
package pjq.springboot.service.filesafecheck;

import java.io.File;

/**
 * 文件安全检查服务Service<br>
 * 具体工程可按需实现多个实现类，以检查不同的文件类型
 *
 * @author pengjianqiang
 * @date 2023-11-09
 */
public interface FileSafeCheckService {
    /**
     * 判断文件是否需要做安全检查
     *
     * @param file
     *         文件对象
     * @param mimeType
     *         文件mimeType值
     * @param extension
     *         文件后缀名
     * @return
     */
    boolean needCheck(File file, String mimeType, String extension);

    /**
     * 当文件不安全时提示的文字内容
     *
     * @param extension
     *         文件后缀名
     * @return
     */
    default String notSafeMsg(String extension) {
        return null;
    }

    /**
     * 检查文件是否安全
     *
     * @param file
     *         文件对象
     * @param mimeType
     *         文件mimeType值
     * @param extension
     *         文件后缀名
     * @return
     */
    boolean isSafe(File file, String mimeType, String extension);
}