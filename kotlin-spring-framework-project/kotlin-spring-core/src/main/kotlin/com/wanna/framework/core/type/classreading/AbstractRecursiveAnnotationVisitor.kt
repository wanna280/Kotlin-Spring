package com.wanna.framework.core.type.classreading

import com.wanna.framework.asm.*
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ReflectionUtils
import com.wanna.common.logging.Logger
import com.wanna.common.logging.LoggerFactory
import com.wanna.framework.core.annotation.AnnotationAttributes
import java.security.AccessControlException

/**
 * 支持去进行递归访问注解的AnnotationVisitor
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/18
 *
 * @param classLoader 当涉及到类加载时, 需要使用到的ClassLoader
 * @param attributes 注解的属性信息
 */
abstract class AbstractRecursiveAnnotationVisitor(
    protected val classLoader: ClassLoader,
    protected val attributes: AnnotationAttributes
) : AnnotationVisitor(SpringAsmInfo.ASM_VERSION) {

    /**
     * Logger
     */
    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)

    /**
     * 当访问注解的基础数据类型以及基础数据类型的数组(eg: int/int[])的属性时, 我们需要将它去收集到AnnotationAttributes当中
     *
     * @param name name(当访问数组时, name为null)
     * @param value value
     */
    override fun visit(@Nullable name: String?, value: Any) {
        name ?: return
        attributes[name] = value
    }

    /**
     * 当访问注解当中配置的属性值是一个枚举类型的话
     *
     * @param name name
     * @param descriptor descriptor
     * @param value value
     */
    override fun visitEnum(name: String, descriptor: String, value: String) {
        // 将Value从字符串去解析成为枚举值
        val enumValue = getEnumValue(descriptor, value)
        visit(name, enumValue)
    }

    /**
     * 当访问的注解当中的属性是一个注解的话, 需要提供递归的访问
     *
     * @param name 属性名
     * @param descriptor 注解类型的描述符
     * @return 访问该注解时需要用到的AnnotationVisitor(需要提供递归的访问)
     */
    override fun visitAnnotation(name: String?, descriptor: String): AnnotationVisitor {
        val annotationType = Type.getType(descriptor).className

        // 创建一个嵌套的AnnotationAttributes
        val nestedAttributes = AnnotationAttributes(annotationType, classLoader)

        // 将嵌套的AnnotationAttributes去添加到外层的Attributes当中
        visit(name, nestedAttributes)
        return RecursiveAnnotationAttributesVisitor(annotationType, classLoader, attributes)
    }

    /**
     * 当访问的注解当中的属性是一个数组的话, 需要去对一个数组当中的元素去使用AnnotationVisitor去进行访问
     *
     * @param name 注解的属性名name
     * @return 提供数组的访问时, 需要用到的AnnotationVisitor
     */
    override fun visitArray(name: String): AnnotationVisitor {
        return RecursiveAnnotationArrayVisitor(name, this.classLoader, this.attributes)
    }

    /**
     * 将给定的value从枚举值字符串转换成为枚举值对象
     *
     * @param descriptor descriptor
     * @param value 枚举值字符串value
     */
    protected open fun getEnumValue(descriptor: String, value: String): Any {
        try {
            // 获取该枚举的类Class
            val enumType = classLoader.loadClass(Type.getType(descriptor).className)

            // 从枚举类当中, 根据value去找到对应的字段
            val enumConstantField = ReflectionUtils.findField(enumType, value)

            // 如果找到了该字段的话, 那么使用反射去获取该字段的值
            if (enumConstantField != null) {
                ReflectionUtils.makeAccessible(enumConstantField)
                return ReflectionUtils.getField(enumConstantField, null)!!
            }
        } catch (ex: Throwable) {
            // 如果是ClassNotFoundException/NoClassDefFoundError的话, 那么说明加载不到这个类
            if (ex is ClassNotFoundException || ex is NoClassDefFoundError) {
                logger.error("Failed to classload enum type while reading annotation metadata", ex)

                // 检查是否无法访问?
            } else if (ex is IllegalAccessException || ex is AccessControlException) {
                logger.debug("Could not access enum value while reading annotation metadata", ex)
            }
        }
        return value
    }
}