package com.wanna.framework.core.type.classreading

import com.wanna.framework.core.annotation.AnnotationAttributes


/**
 * 支持去进行递归访问注解的AnnotationVisitor
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/18
 */
open class RecursiveAnnotationAttributesVisitor(
    val annotationType: String,
    classLoader: ClassLoader,
    attributes: AnnotationAttributes
) : AbstractRecursiveAnnotationVisitor(classLoader, attributes)