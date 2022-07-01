package com.wanna.boot.actuate.endpoint.invoke.reflect

import com.wanna.boot.actuate.endpoint.invoke.OperationParameter
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.lang.Nullable
import java.lang.reflect.Parameter
import javax.annotation.Nonnull
import javax.annotation.meta.When

/**
 * Operation的方法参数
 */
open class OperationMethodParameter(private val name: String, private val parameter: Parameter) : OperationParameter {
    companion object {
        private val jsr305Present = ClassUtils.isPresent("javax.annotation.Nonnull")
    }

    override fun getName() = this.name

    override fun getType(): Class<*> = this.parameter.type

    override fun isMandatory(): Boolean {
        // 如果标注了@Nullable，那么return false，不是为强制为空的
        if (parameter.getAnnotation(Nullable::class.java) != null) {
            return false
        }
        // 检查Jsr305的@Nonnull注解
        return if (jsr305Present) Jsr305(parameter).isMandatory() else true
    }

    override fun toString() = "name='$name', parameterType=${parameter.type}"


    /**
     * 使用内部类的方式去进行懒加载，保证在Jsr305缺少的情况下，也不会抛出LinkageError
     */
    class Jsr305(private val parameter: Parameter) {
        fun isMandatory(): Boolean {
            // 如果没有@Nonnull注解，或者when==Always，那么return true，说明不能为空
            val annotation = AnnotatedElementUtils.getMergedAnnotation(parameter, Nonnull::class.java)
            return annotation == null || annotation.`when` == When.ALWAYS
        }
    }


}