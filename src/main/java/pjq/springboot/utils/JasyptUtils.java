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
package pjq.springboot.utils;

import java.util.regex.Pattern;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Jasypt加解密简单工具类<br>
 * 暂时只处理{@link StringEncryptor}相关
 *
 * @author pengjianqiang
 * @date 2023-07-11
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JasyptUtils {
    private static final String PREFIX = "ENC(";
    private static final String SUFFIX = ")";
    private static final Pattern pattern = Pattern.compile("^ENC\\(.*\\)$");

    public static String wrapEncryptedText(String text) {
        return PREFIX + text + SUFFIX;
    }

    public static String unwrapEncryptedText(String text) {
        return isEncryptedText(text) ? (text.substring(PREFIX.length(), text.length() - 1)) : text;
    }

    public static boolean isEncryptedText(String text) {
        return pattern.matcher(text).matches();
    }

    /**
     * 根据jasypt密码构造加密器
     *
     * @param password
     * @return
     */
    public static StringEncryptor buildStringEncryptor(String password) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig(); //只设置必须项，其它用默认的
        config.setPassword(password);
        config.setPoolSize("1");
        encryptor.setConfig(config);
        return encryptor;
    }

    /**
     * 调试用方法，只用于生成一个加密值并复制到配置文件中
     *
     * @param password
     * @param text
     */
    public static void testEncrypt(String password, String text) {
        testEncrypt(buildStringEncryptor(password), text);
    }

    /**
     * 调试用方法，只用于生成一个加密值并复制到配置文件中
     *
     * @param encryptor
     * @param text
     */
    public static void testEncrypt(StringEncryptor encryptor, String text) {
        String encryptedText = wrapEncryptedText(encryptor.encrypt(text));
        System.out.println("可直接复制到配置文件的配置值 = " + encryptedText);
        System.out.println("解密值 = " + encryptor.decrypt(unwrapEncryptedText(encryptedText)));
    }

    /**
     * 根据提供的加密器加密字符串<br>
     * 加密出的字符串格式：ENC(XXX)，如果不需要这种格式，则可以直接使用加密器的encrypt方法
     *
     * @param encryptor
     * @param text
     * @return
     */
    public static String encrypt(StringEncryptor encryptor, String text) {
        return wrapEncryptedText(encryptor.encrypt(text));
    }

    /**
     * 根据提供的加密器解密字符串
     *
     * @param encryptor
     * @param text
     * @return
     */
    public static String decrypt(StringEncryptor encryptor, String text) {
        return encryptor.decrypt(unwrapEncryptedText(text));
    }
}
