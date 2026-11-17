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
package pjq.springboot.config.jasypt;

import org.jasypt.encryption.StringEncryptor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pjq.commons.utils.crypto.SMUtils;

/**
 * 使用国密SM4算法(CTR模式)的Jasypt加密器<br>
 * CTR模式无完整性校验，只适用于不需要防篡改的场景；每次加密都会随机生成IV，相同明文的密文每次都不同
 *
 * @author pengjianqiang
 * @date 2023-08-13
 */
@Getter
@AllArgsConstructor
public class SM4CTRStringEncryptor implements StringEncryptor {
    /**
     * SM4密钥
     */
    private String key;

    @Override
    public String encrypt(String message) {
        return SMUtils.sm4EncryptByCTR(message, key);
    }

    @Override
    public String decrypt(String encryptedMessage) {
        return SMUtils.sm4DecryptByCTR(encryptedMessage, key);
    }
}