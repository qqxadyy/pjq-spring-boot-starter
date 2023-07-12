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
package pjq.springboot.config.jpaauditing;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Date;
import javax.annotation.Resource;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Auditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.util.Assert;

import pjq.commons.constant.CommonEnumConstant.YesOrNoInt;
import pjq.commons.utils.CheckUtils;
import pjq.commons.utils.collection.CollectionUtils;
import pjq.springboot.assembly.annotation.jpaauditing.CreatedById;
import pjq.springboot.assembly.annotation.jpaauditing.CreatedTimestamp;
import pjq.springboot.assembly.annotation.jpaauditing.LastModifiedById;
import pjq.springboot.assembly.annotation.jpaauditing.LastModifiedTimestamp;
import pjq.springboot.assembly.annotation.jpaauditing.LogicallyDeleteFlag;
import pjq.springboot.assembly.annotation.jpaauditing.LogicallyDeletedDate;
import pjq.springboot.assembly.annotation.jpaauditing.LogicallyDeletedTimestamp;
import pjq.springboot.beanutil.SpringContextHolder;

/**
 * 扩展{@link AuditingEntityListener}，设置审计人ID及其它公共的字段<br>
 * 和{@link AuditorExtendAware}的实现Bean配合使用
 *
 * 其它说明：<br>
 * 创建时间：{@link CreatedDate}<br>
 * 创建时间戳：{@link CreatedTimestamp}<br>
 * 创建人；{@link CreatedBy}<br>
 * 创建人ID：{@link CreatedById}<br>
 * 修改时间；{@link LastModifiedDate}<br>
 * 修改时间戳：{@link LastModifiedTimestamp}<br>
 * 修改人；{@link LastModifiedBy}<br>
 * 修改人ID：{@link LastModifiedById}<br>
 * 逻辑删除标志：{@link LogicallyDeleteFlag}
 *
 * @author pengjianqiang
 * @date 2021-05-28
 */
@Configurable
public class AuditingEntityExtendListener {
    @Resource
    private ObjectProvider<AuditorExtendAware> auditorConfigProvider;

    /**
     * Sets modification and creation date and auditor on the target object in case it implements {@link Auditable} on
     * persist events.
     *
     * @param target
     */
    @SuppressWarnings("unchecked")
    @PrePersist
    public void touchForCreate(Object target) {
        Assert.notNull(target, "Entity must not be null!");
        AuditorExtendAware awareObj = findAwareObj();
        Object auditorId = awareObj.getCurrentAuditorId().get();
        long currentTimestamp = System.currentTimeMillis();

        Field[] fields = FieldUtils.getAllFields(target.getClass()); //要获取target类及父类的属性
        setFieldValue(target, fields, CreatedById.class, auditorId);
        setFieldValue(target, fields, LastModifiedById.class, auditorId);
        setFieldValue(target, fields, CreatedTimestamp.class, currentTimestamp);
        setFieldValue(target, fields, LastModifiedTimestamp.class, currentTimestamp);
        setFieldValue(target, fields, LogicallyDeleteFlag.class, YesOrNoInt.NO.valueOfInt());

        //设置其它值
        CollectionUtils.forEach(awareObj.getOtherValuesWhileCreate(),
                e -> setFieldValue(target, fields, (Class<Annotation>) e.getKey(), e.getValue()));
    }

    /**
     * Sets modification and creation date and auditor on the target object in case it implements {@link Auditable} on
     * update events.
     *
     * @param target
     */
    @PreUpdate
    public void touchForUpdate(Object target) {
        Assert.notNull(target, "Entity must not be null!");
        AuditorExtendAware awareObj = findAwareObj();
        Object auditorId = awareObj.getCurrentAuditorId().get();
        long currentTimestamp = System.currentTimeMillis();

        Field[] fields = FieldUtils.getAllFields(target.getClass()); //要获取target类及父类的属性
        setFieldValue(target, fields, LastModifiedById.class, auditorId);
        setFieldValue(target, fields, LastModifiedTimestamp.class, currentTimestamp);

        //如果逻辑删除标志为1，则同时更新删除时间和时间戳
        Object logicallyDeleteFlag = getFieldValue(target, fields, LogicallyDeleteFlag.class);
        if (YesOrNoInt.YES.valueOfInt().equals(logicallyDeleteFlag)) {
            setFieldValue(target, fields, LogicallyDeletedDate.class, new Date());
            setFieldValue(target, fields, LogicallyDeletedTimestamp.class, currentTimestamp);
        }

        //设置其它值
        CollectionUtils.forEach(awareObj.getOtherValuesWhileUpdate(),
                e -> setFieldValue(target, fields, (Class<Annotation>) e.getKey(), e.getValue()));
    }

    private AuditorExtendAware findAwareObj() {
        //直接对象注入bean的话可能会出现注入的bean对象为空，但是实际getBean能获取到bean的情况
        //所以用ObjectProvider去获取bean，如果bean不存在则通过getBean获取
        return auditorConfigProvider.getIfAvailable(() -> SpringContextHolder.getBean(AuditorExtendAware.class));
    }

    private void setFieldValue(Object target, Field[] fields, Class<? extends Annotation> targetAnno,
            Object targetValue) {
        Field targetField = CollectionUtils.filterOne(fields, field -> field.isAnnotationPresent(targetAnno));
        if (CheckUtils.isNull(targetField)) {
            return;
        }
        try {
            targetField.setAccessible(true);
            targetField.set(target, targetValue);
        } catch (Exception e) {
        }
    }

    private Object getFieldValue(Object target, Field[] fields, Class<? extends Annotation> targetAnno) {
        Field targetField = CollectionUtils.filterOne(fields, field -> field.isAnnotationPresent(targetAnno));
        if (CheckUtils.isNull(targetField)) {
            return null;
        }
        try {
            targetField.setAccessible(true);
            return targetField.get(target);
        } catch (Exception e) {
            return null;
        }
    }
}