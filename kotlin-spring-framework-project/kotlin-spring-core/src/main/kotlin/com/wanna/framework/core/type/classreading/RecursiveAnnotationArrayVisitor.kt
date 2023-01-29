package com.wanna.framework.core.type.classreading

import com.wanna.framework.core.annotation.AnnotationAttributes
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Type
import java.lang.reflect.Array
import java.util.*

/**
 * 提供递归访问注解当中的一个数组的AnnotationVisitor
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/18
 */
open class RecursiveAnnotationArrayVisitor(
    private val attributeName: String,
    classLoader: ClassLoader,
    attributes: AnnotationAttributes
) : AbstractRecursiveAnnotationVisitor(classLoader, attributes) {

    /**
     * 所有的嵌套的注解属性
     */
    private val allNestedAttributes = ArrayList<AnnotationAttributes>()

    /**
     * 当访问数组当中的一个元素时, 需要将该元素收集到Attributes当中去
     *
     * @param name 因为是访问数组的元素, 所以name=null
     * @param value 数组当中的其中一个元素的值
     */
    @Suppress("UNCHECKED_CAST")
    override fun visit(name: String?, value: Any) {
        var newValue = value
        val oldValue = attributes[this.attributeName]

        // 如果之前已经存在有数组了, 那么把当前元素加到数组的最后面去
        if (oldValue != null) {
            val type = newValue::class.java
            val newArray = Array.newInstance(type, (oldValue as kotlin.Array<Any>).size + 1) as kotlin.Array<Any>
            // copy old
            System.arraycopy(oldValue, 0, newArray, 0, oldValue.size)
            // add new value
            newArray[newArray.size - 1] = value
            newValue = newArray

            // 如果之前还不包含数组的话, 那么先创建一个新的数组, 把元素给它塞进去
        } else {
            val type = newValue::class.java
            val newArray = Array.newInstance(type, 1) as kotlin.Array<Any>
            newArray[newArray.size - 1] = value
            newValue = newArray
        }
        this.attributes[this.attributeName] = newValue
    }

    /**
     * 当访问数组的元素, 还是一个注解时, 需要把该注解去进行递归的处理
     *
     * @param name 因为是访问数组的元素, name=null
     * @param descriptor 描述符
     * @return 提供对于一个注解的元素的访问, 去执行递归的访问
     */
    override fun visitAnnotation(name: String?, descriptor: String): AnnotationVisitor {
        val className = Type.getType(descriptor).className

        // 创建一个嵌套的AnnotationAttributes, 对于其中一个注解元素的属性值, 就会被收集到这里
        val attributes = AnnotationAttributes(className, classLoader)
        this.allNestedAttributes += attributes
        return RecursiveAnnotationAttributesVisitor(className, classLoader, attributes)
    }

    /**
     * 在visitEnd时, 需要将嵌套的全部的注解元素信息的merge结果, 去添加到Attributes当中去
     */
    override fun visitEnd() {
        if (this.allNestedAttributes.isNotEmpty()) {
            attributes[this.attributeName] = allNestedAttributes
        }
    }
}