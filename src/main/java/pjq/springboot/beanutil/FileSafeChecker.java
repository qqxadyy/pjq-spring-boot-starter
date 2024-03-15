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
package pjq.springboot.beanutil;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Resource;
import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import okio.ByteString;
import pjq.commons.utils.CheckUtils;
import pjq.commons.utils.DefaultValueGetter;
import pjq.commons.utils.FileTypeUtils;
import pjq.commons.utils.OkioUtils;
import pjq.commons.utils.UUIDUtil;
import pjq.springboot.service.filesafecheck.FileSafeCheckService;
import pjq.springboot.service.filesafecheck.UploadFileSafeCheckEnableService;

/**
 * 检查文件是否安全的检查器<br>
 * 只能对一些文件类型做简单的检查，且实现不一定准确，自行判断是否使用
 *
 * @author pengjianqiang
 * @date 2023-11-09
 */
@Component
public class FileSafeChecker {
    @Resource
    private ObjectProvider<FileSafeCheckService> fileSafeCheckServiceProvider;

    @Resource
    private ObjectProvider<UploadFileSafeCheckEnableService> uploadFileSafeCheckEnableServiceProvider;

    /**
     * 检查文件是否安全<br>
     * 如果是图片文件，则图片会被重写，注意执行完检查方法后再获取文件流<br>
     * 一般的业务场景也可以使用
     *
     * @param filePath
     */
    public void check(String filePath) {
        check(new File(filePath));
    }

    /**
     * 检查文件是否安全<br>
     * 如果是图片文件，则图片会被重写，注意执行完检查方法后再获取文件流<br>
     * 一般的业务场景也可以使用
     *
     * @param file
     */
    public void check(File file) {
        String mimeType = FileTypeUtils.getMimeType(file);
        String extension = FileTypeUtils.getExtension(file, mimeType);

        if (FileTypeUtils.isImage(file, mimeType)) {
            //如果是图片，则先重写图片文件(因为重写图片时会去除图片中的脚本，重写成功则按文件内容安全处理)
            ByteString sourceBs = OkioUtils.readByteString(file);
            try {
                BufferedImage sourceImage = ImageIO.read(file);
                ImageIO.write(sourceImage, extension, file);
            } catch (Exception e) {
                //重写图片报错的话则重新写入原始图片(避免重写图片过程中报错导致原始图片被破坏的情况)
                OkioUtils.copy(sourceBs, file);
            }
        }

        if (CheckUtils.isNotNull(fileSafeCheckServiceProvider)) {
            fileSafeCheckServiceProvider.forEach(service -> {
                if (service.needCheck(file, mimeType, extension)) {
                    if (!service.isSafe(file, mimeType, extension)) {
                        String errMsg = DefaultValueGetter.getValue("文件可能含有不安全的内容", service.notSafeMsg(extension));
                        throw new FileNotSafeException(errMsg, file.getAbsolutePath());
                    }
                }
            });
        }
    }

    /**
     * 检查上传的文件是否安全
     *
     * @param file
     * @return
     */
    public MultipartFile check(MultipartFile file) {
        UploadFileSafeCheckEnableService enableService;
        if (CheckUtils.isNotNull(enableService = uploadFileSafeCheckEnableServiceProvider.getIfAvailable())
                && !enableService.safeCheckUploadFileEnabled()) {
            //如果UploadFileSafeCheckEnableService.enabled返回false，表示不检查上传文件是否安全，则直接返回MultipartFile对象
            return file;
        }

        String tempFileName = DefaultValueGetter.get(UUIDUtil::genUuid, file.getOriginalFilename());
        File tempFile = new File(FileUtils.getTempDirectoryPath(), tempFileName);
        try {
            try {
                //这里操作完成后，上传的临时文件已经被转成tempFile了
                //即原始MultipartFile已经不能用于处理文件(例如获取流之类的方法会报错文件不存在，但是获取文件名之类的不影响)
                //所以后续需要返回一个新的SafeCheckMultipartFile对象
                file.transferTo(tempFile);
            } catch (Exception e) {
                return file; //写入到临时文件时也报错的话则直接返回，即不做后续检查
            }

            //检查是否可执行文件等
            if (FileTypeUtils.isExecutableOrSysFile(tempFile, false)) {
                throw new FileNotSafeException("不能上传可执行文件或系统关键文件", tempFile.getAbsolutePath());
            }

            //检查文件内容是否合法(注意如果是图片文件的话，会重写图片)
            check(tempFile);

            //检查完成后返回一个SafeCheckMultipartFile对象代替原始MultipartFile对象
            //即检查文件是否安全后，再处理上传文件都是通过SafeCheckMultipartFile对象
            //(另外图片的话会重写文件，所以SafeCheckMultipartFile持有的是重写后的文件相关信息)
            return new SafeCheckMultipartFile(file, OkioUtils.readByteString(tempFile));
        } finally {
            //删除临时文件
            if (null != tempFile && tempFile.exists()) {
                tempFile.delete(); //检查结束后删除临时文件
            }
        }
    }

    @SuppressWarnings("serial")
    public static class FileNotSafeException extends RuntimeException {
        private String filePath;

        public FileNotSafeException(String message, String filePath) {
            super(message);
            this.filePath = filePath;
        }

        @Override
        public String toString() {
            return getMessage() + "[文件=" + filePath + "]";
        }
    }

    @AllArgsConstructor
    public static class SafeCheckMultipartFile implements MultipartFile {
        /**
         * 原始的上传MultipartFile对象
         */
        private MultipartFile originalFile;

        /**
         * 重写上传文件后的新文件字节对象<br>
         * 用ByteString保存是为了方便可以在需要的情况下先删掉原始的文件，不用携带文件流
         */
        private ByteString rewriteFileByteObj;

        @Override
        public String getName() {
            return originalFile.getName();
        }

        @Override
        public String getOriginalFilename() {
            return originalFile.getOriginalFilename();
        }

        @Override
        public String getContentType() {
            return originalFile.getContentType();
        }

        @Override
        public boolean isEmpty() {
            return originalFile.isEmpty();
        }

        @Override
        public long getSize() {
            return rewriteFileByteObj.size();
        }

        @Override
        public byte[] getBytes() throws IOException {
            return rewriteFileByteObj.toByteArray();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(rewriteFileByteObj.toByteArray());
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            OkioUtils.copy(rewriteFileByteObj, dest);
        }
    }
}