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
package pjq.springboot.config.web.filesafecheck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import pjq.commons.utils.CheckUtils;
import pjq.springboot.assembly.annotation.condition.ConditionalOnSpringCommonWebApplication;
import pjq.springboot.beanutil.FileSafeChecker;
import pjq.springboot.constant.UploadFileSafeCheckConstants;

/**
 * 上传文件安全检查服务的切面
 *
 * @author pengjianqiang
 * @date 2023-11-16
 */
@Component
@ConditionalOnSpringCommonWebApplication
@ConditionalOnProperty(value = UploadFileSafeCheckConstants.UPLOAD_FILE_SAFE_CHECK_ENABLED, havingValue = "true", matchIfMissing = true)
@Aspect
@Slf4j
public class UploadFileSafeCheckAspect {
    @Resource
    private FileSafeChecker fileSafeChecker;

    @PostConstruct
    public void init() {
        //本方法只用于加载bean后输出日志，暂时不需要初始化逻辑
        log.info("加载上传文件安全检查服务的切面Bean");
    }

    /**
     * 基于注解织入
     */
    @Pointcut("@annotation(pjq.springboot.assembly.annotation.filesafecheck.SafeCheckUploadFile)")
    public void safeCheckUploadFile() {
    }

    @SuppressWarnings("unchecked")
    @Around("safeCheckUploadFile()")
    public Object aroundUploadFile(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] methodParams = joinPoint.getArgs();
        if (CheckUtils.isEmpty(methodParams)) {
            //目标方法为无参方法上，则直接触发调用并返回
            return joinPoint.proceed(methodParams);
        }

        //判断目标方法是否有MultipartFile类型的相关参数
        int multipartFileIndex = -1;
        int multipartFileType = -1;
        for (int i = 0, size = methodParams.length; i < size; i++) {
            Object param = methodParams[i];
            if (CheckUtils.isNotNull(param) && param instanceof MultipartFile) {
                multipartFileIndex = i;
                multipartFileType = 1;
                break;
            } else if (param instanceof MultipartFile[] && CheckUtils.isNotEmpty((MultipartFile[]) param)) {
                multipartFileIndex = i;
                multipartFileType = 2;
                break;
            } else if ((param instanceof List || param instanceof Set)
                    && CheckUtils.isNotEmpty((Collection<?>) param)) {
                for (Object subParam : (Collection<?>) param) {
                    if (!(subParam instanceof MultipartFile)) {
                        break; //只要集合中有一个元素不是MultipartFile，就按不是MultipartFile集合处理
                    }
                }
                multipartFileIndex = i;
                multipartFileType = ((param instanceof List) ? 3 : 4);
                break;
            }
        }
        if (multipartFileIndex == -1) {
            //目标方法没有MultipartFile类型的相关参数，则直接触发调用并返回
            return joinPoint.proceed(methodParams);
        }

        //检查上传文件是否安全，检查完成后替换对应的参数对象用于后续处理
        if (multipartFileType == 1) {
            //单个MultipartFile参数
            methodParams[multipartFileIndex] = fileSafeChecker.check((MultipartFile) methodParams[multipartFileIndex]);
        } else if (multipartFileType == 2) {
            //MultipartFile数组的参数
            MultipartFile[] oriFiles = (MultipartFile[]) methodParams[multipartFileIndex];
            MultipartFile[] checkedFiles = new MultipartFile[oriFiles.length];
            for (int i = 0, size = oriFiles.length; i < size; i++) {
                checkedFiles[i] = fileSafeChecker.check(oriFiles[i]);
            }
            methodParams[multipartFileIndex] = checkedFiles;
        } else if (multipartFileType == 3 || multipartFileType == 4) {
            //List<MultipartFile>的参数
            Collection<MultipartFile> oriFiles = (Collection<MultipartFile>) methodParams[multipartFileIndex];
            Collection<MultipartFile> checkedFiles = (multipartFileType == 3 ? new ArrayList<>() : new LinkedHashSet<>());
            for (MultipartFile file : oriFiles) {
                checkedFiles.add(fileSafeChecker.check(file));
            }
            methodParams[multipartFileIndex] = checkedFiles;
        }

        //直接业务方法
        return joinPoint.proceed(methodParams);
    }
}