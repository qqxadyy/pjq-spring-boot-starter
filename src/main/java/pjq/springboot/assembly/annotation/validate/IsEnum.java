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
package pjq.springboot.assembly.annotation.validate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.AnnotationUtils;

import lombok.Data;
import pjq.commons.annos.EnhanceEnum.DefaultEnhanceEnum;
import pjq.commons.utils.CheckUtils;
import pjq.commons.utils.CommonTypeJudger;
import pjq.commons.utils.collection.CollectionUtils;
import pjq.springboot.assembly.annotation.validate.IsEnum.IsEnumValidator;
import pjq.springboot.assembly.annotation.validate.IsEnum.IsEnumValidator4Byte;
import pjq.springboot.assembly.annotation.validate.IsEnum.IsEnumValidator4ByteList;
import pjq.springboot.assembly.annotation.validate.IsEnum.IsEnumValidator4Integer;
import pjq.springboot.assembly.annotation.validate.IsEnum.IsEnumValidator4IntegerList;
import pjq.springboot.assembly.annotation.validate.IsEnum.IsEnumValidator4List;

/**
 * 验证是否属于给定枚举的注解
 *
 * @author pengjianqiang
 * @date 2018-10-09
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Constraint(validatedBy = { IsEnumValidator.class, IsEnumValidator4List.class,
        IsEnumValidator4Integer.class, IsEnumValidator4IntegerList.class,
        IsEnumValidator4Byte.class, IsEnumValidator4ByteList.class
})
public @interface IsEnum {
    String message() default "参数值未定义";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @AliasFor("target")
    Class<? extends Enum<?>> value() default NullEnum.class;

    @AliasFor("value")
    Class<? extends Enum<?>> target() default NullEnum.class;

    /**
     * 限制可支持的枚举value值，为空表示不限制
     *
     * @return
     */
    String[] allowEnumValues() default {};

    public enum NullEnum {
    }

    @Data
    @SuppressWarnings("rawtypes")
    public abstract class BaseIsEnumValidator<T> implements ConstraintValidator<IsEnum, T> {
        private Class targetEnum;
        private String[] allowEnumValues;

        @Override
        public void initialize(IsEnum isEnumAnno) {
            targetEnum = (Class) AnnotationUtils.getValue(AnnotationUtils.getAnnotation(isEnumAnno, IsEnum.class));
            allowEnumValues = isEnumAnno.allowEnumValues();
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean isValid(T value, ConstraintValidatorContext context) {
            boolean isValid = check(value, context);
            if (isValid && CheckUtils.isNotEmpty(allowEnumValues)) {
                //类型符合则验证allowEnums
                if (CheckUtils.isNull(value)) {
                    return true;
                }

                try {
                    if (value instanceof String || value instanceof Integer) {
                        if (value instanceof String && CheckUtils.isEmpty((String) value)) {
                            return true;
                        }

                        for (String allowEnumValue : allowEnumValues) {
                            if (String.valueOf(value).equals(allowEnumValue)) {
                                return true;
                            }
                        }
                        return false;
                    } else if (CommonTypeJudger.isListType(value.getClass())) {
                        if (CheckUtils.isEmpty((List) value)) {
                            return true;
                        }

                        List<String> listValue = CollectionUtils.transformToList((List) value, String::valueOf);
                        Collection<String> notAllowValues = org.apache.commons.collections4.CollectionUtils
                                .removeAll(listValue, Arrays.asList(allowEnumValues)); //参数列表存在allowEnumValues中没有的值
                        if (CheckUtils.isEmpty(notAllowValues)) {
                            return true;
                        }
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
            return isValid;
        }

        public abstract boolean check(T value, ConstraintValidatorContext context);
    }

    @SuppressWarnings("unchecked")
    public class IsEnumValidator extends BaseIsEnumValidator<String> {
        @Override
        public boolean check(String value, ConstraintValidatorContext context) {
            try {
                return !NullEnum.class.equals(getTargetEnum())
                        && (CheckUtils.isEmpty(value) || DefaultEnhanceEnum.isEnumOf(getTargetEnum(), value));
            } catch (Exception e) {
                return false;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public class IsEnumValidator4List extends BaseIsEnumValidator<List<String>> {
        @Override
        public boolean check(List<String> value, ConstraintValidatorContext context) {
            try {
                if (NullEnum.class.equals(getTargetEnum())) {
                    return false;
                } else if (CheckUtils.isEmpty(value.toArray(new String[]{}))) {
                    return true;
                } else {
                    for (String v : value) {
                        if (CheckUtils.isEmpty(v)) {
                            continue; //其中一个或多个为空时跳过
                        }
                        if (!DefaultEnhanceEnum.isEnumOf(getTargetEnum(), v)) {
                            return false;
                        }
                    }
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public class IsEnumValidator4Integer extends BaseIsEnumValidator<Integer> {
        @Override
        public boolean check(Integer value, ConstraintValidatorContext context) {
            try {
                return !NullEnum.class.equals(getTargetEnum())
                        && (CheckUtils.isNull(value)
                        || DefaultEnhanceEnum.isEnumOf(getTargetEnum(), String.valueOf(value)));
            } catch (Exception e) {
                return false;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public class IsEnumValidator4IntegerList extends BaseIsEnumValidator<List<Integer>> {
        @Override
        public boolean check(List<Integer> value, ConstraintValidatorContext context) {
            try {
                if (NullEnum.class.equals(getTargetEnum())) {
                    return false;
                } else if (CheckUtils.isEmpty(value)) {
                    return true;
                } else {
                    for (Integer v : value) {
                        if (CheckUtils.isNull(v)) {
                            continue; //其中一个或多个为空时跳过
                        }
                        if (!DefaultEnhanceEnum.isEnumOf(getTargetEnum(), String.valueOf(v))) {
                            return false;
                        }
                    }
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public class IsEnumValidator4Byte extends BaseIsEnumValidator<Byte> {
        @Override
        public boolean check(Byte value, ConstraintValidatorContext context) {
            try {
                return !NullEnum.class.equals(getTargetEnum())
                        && (CheckUtils.isNull(value)
                        || DefaultEnhanceEnum.isEnumOf(getTargetEnum(), String.valueOf(value)));
            } catch (Exception e) {
                return false;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public class IsEnumValidator4ByteList extends BaseIsEnumValidator<List<Byte>> {
        @Override
        public boolean check(List<Byte> value, ConstraintValidatorContext context) {
            try {
                if (NullEnum.class.equals(getTargetEnum())) {
                    return false;
                } else if (CheckUtils.isEmpty(value)) {
                    return true;
                } else {
                    for (Byte v : value) {
                        if (CheckUtils.isNull(v)) {
                            continue; //其中一个或多个为空时跳过
                        }
                        if (!DefaultEnhanceEnum.isEnumOf(getTargetEnum(), String.valueOf(v))) {
                            return false;
                        }
                    }
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
        }
    }
}