package com.wanna.framework.core.annotation

import com.wanna.framework.lang.Nullable
import java.lang.reflect.Method

/**
 * 从[Annotation]/[TypeMappedAnnotation]/Map当中去提取注解属性的策略接口
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/24
 */
fun interface ValueExtractor {

    /**
     * 从给定的注解的属性方法当中, 根据obj作为属性名, 去提取到对应的属性值
     *
     * @param method 注解的属性方法(比如value属性, 对应的就是value方法)
     * @param obj 属性名(比如"value")
     * @return 属性值
     */
    @Nullable
    fun extract(method: Method, @Nullable obj: Any?): Any?
}