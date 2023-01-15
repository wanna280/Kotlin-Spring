package com.wanna.boot.actuate.endpoint.invoke.reflect

import com.wanna.boot.actuate.endpoint.invoke.OperationParameter
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.lang.Nullable
import java.lang.reflect.Parameter
import javax.annotation.Nonnull
import javax.annotation.meta.When

/**
 * 封装一个Endpoint当中的一个Operation(Read/Write/Delete)的方法参数
 *
 * @param name 该参数的参数名
 * @param parameter 该参数对应的JDK当中的Parameter对象
 */
open class OperationMethodParameter(private val name: String, private val parameter: Parameter) : OperationParameter {
    companion object {
        // 标识jsr305是否存在
        private val jsr305Present = ClassUtils.isPresent("javax.annotation.Nonnull")
    }

    override fun getName() = this.name

    override fun getType() = this.parameter.type

    /**
     * 判断该属性是否是强制的？如果是强制的, 那么return true;
     * 对于一个强制的方法参数, 如果用户很可能没给的话, 那么很可能会丢出异常
     */
    override fun isMandatory(): Boolean {
        // 如果标注了@Nullable, 那么return false, 不是为强制为空的
        if (parameter.getAnnotation(Nullable::class.java) != null) {
            return false
        }
        // 检查Jsr305的@Nonnull注解
        return if (jsr305Present) Jsr305(parameter).isMandatory() else true
    }

    override fun toString() = "name='$name', parameterType=${parameter.type}"


    /**
     * 使用内部类的方式去进行懒加载, 保证在Jsr305缺少的情况下, 也不会抛出LinkageError;
     * 我们直接使用到compileOnly的方式, 保证我们在编译期间确实可以访问到该类
     *
     * @param parameter 要去匹配@Nonnull注解的方法参数
     */
    class Jsr305(private val parameter: Parameter) {
        fun isMandatory(): Boolean {
            // 如果没有@Nonnull注解, 或者when==Always, 那么return true, 说明不能为空
            val annotation = AnnotatedElementUtils.getMergedAnnotation(parameter, Nonnull::class.java)
            return annotation == null || annotation.`when` == When.ALWAYS
        }
    }


}