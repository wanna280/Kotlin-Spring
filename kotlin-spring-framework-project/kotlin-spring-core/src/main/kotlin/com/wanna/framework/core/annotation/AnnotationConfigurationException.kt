package com.wanna.framework.core.annotation

import com.wanna.framework.core.NestedRuntimeException
import com.wanna.framework.lang.Nullable

/**
 * 如果注解当中的属性被使用不合法的方式去进行配置时, 会抛出的异常.
 *
 * 这个异常, 有可能会被[AnnotationUtils]和合成注解抛出
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/31
 *
 * @param message error message
 * @param cause cause
 *
 * @see AnnotationUtils
 * @see SynthesizedAnnotation
 */
open class AnnotationConfigurationException @JvmOverloads constructor(
    @Nullable message: String?, @Nullable cause: Throwable? = null
) : NestedRuntimeException(message, cause)