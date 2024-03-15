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
package pjq.springboot.assembly.annotation.validate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.constraints.Digits;

import pjq.commons.utils.CheckUtils;
import pjq.springboot.assembly.annotation.validate.NullableDigits.NullableDigitsValidator;

/**
 * {@link Digits}不支持忽略空字符串，所以自定义验证
 *
 * @author pengjianqiang
 * @date 2018-10-09
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Constraint(validatedBy = { NullableDigitsValidator.class })
public @interface NullableDigits {
    String message() default "{javax.validation.constraints.Digits.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * @return maximum number of integral digits accepted for this number
     */
    int integer();

    /**
     * @return maximum number of fractional digits accepted for this number
     */
    int fraction();

    public class NullableDigitsValidator implements ConstraintValidator<NullableDigits, String> {
        private int maxIntegerLength;
        private int maxFractionLength;

        @Override
        public void initialize(NullableDigits constraintAnnotation) {
            this.maxIntegerLength = constraintAnnotation.integer();
            this.maxFractionLength = constraintAnnotation.fraction();
            validateParameters();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (CheckUtils.isEmpty(value)) {
                return true;
            }

            BigDecimal bigNum = getBigDecimalValue(value.subSequence(0, value.length()));
            if (null == bigNum && CheckUtils.isNotEmpty(value)) {
                return false; //和DigitsValidatorForCharSequence不一样，空字符串时也不返回验证失败
            }

            int integerPartLength = bigNum.precision() - bigNum.scale();
            int fractionPartLength = bigNum.scale() < 0 ? 0 : bigNum.scale();

            return (maxIntegerLength >= integerPartLength && maxFractionLength >= fractionPartLength);
        }

        private BigDecimal getBigDecimalValue(CharSequence charSequence) {
            BigDecimal bd;
            try {
                bd = new BigDecimal(charSequence.toString());
            } catch (NumberFormatException nfe) {
                return null;
            }
            return bd;
        }

        private void validateParameters() {
            CheckUtils.checkNotFalse(maxIntegerLength >= 0, "NullableDigits注解的integer参数不能小于0");
            CheckUtils.checkNotFalse(maxFractionLength >= 0, "NullableDigits注解的fraction参数不能小于0");
        }
    }
}