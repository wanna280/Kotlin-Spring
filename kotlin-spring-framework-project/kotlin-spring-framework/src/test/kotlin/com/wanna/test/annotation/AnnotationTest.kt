package com.wanna.test.annotation

import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.core.annotation.SynthesizedMergedAnnotationInvocationHandler
import com.wanna.framework.core.annotation.TypeMappedAnnotation
import java.lang.reflect.Proxy

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/21
 */
class AnnotationTest {

}

fun main() {
    val component = Component::class.java
    val instance = SynthesizedMergedAnnotationInvocationHandler.createProxy(TypeMappedAnnotation(), component)
    println(instance)


}