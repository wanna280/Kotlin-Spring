package com.wanna.framework.core.type.classreading

import com.wanna.framework.core.annotation.MergedAnnotation
import com.wanna.framework.core.asm.SpringAsmInfo
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Type
import java.util.function.Consumer

/**
 * MergedAnnotation的读取的AnnotationVisitor
 *
 * // TODO 这里还有很多方法需要完善
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/19
 *
 * @see AnnotationVisitor
 */
open class MergedAnnotationReadingVisitor<A : Annotation>(
    private val classLoader: ClassLoader,
    private val source: Any?,
    private val descriptor: String,
    private val annotations: MutableCollection<MergedAnnotation<A>>
) : AnnotationVisitor(SpringAsmInfo.ASM_VERSION) {

    /**
     * attributes, 负责维护当前要进行处理的注解的相关属性信息
     */
    private val attributes = LinkedHashMap<String, Any>()

    /**
     * 访问注解当中的一个属性时, 我们需要去将该属性去收集起来
     *
     * @param name name
     * @param value value
     */
    override fun visit(name: String?, value: Any) {
        name ?: return
        attributes[name] = value
    }

    /**
     * 访问Array
     */
    override fun visitArray(name: String): AnnotationVisitor? {
        return ArrayVisitor { attributes[name] = it }
    }

    /**
     * 访问枚举值
     */
    override fun visitEnum(name: String, descriptor: String?, value: String) {
        val className = Type.getType(descriptor).className
        val enumClass = ClassUtils.forName<Enum<*>>(className, classLoader)
        attributes[name] = java.lang.Enum.valueOf(enumClass, value)
    }

    /**
     * 在访问结束的时候, 将收集到的一个注解的相关信息去merge到annotations当中去
     *
     * @see MergedAnnotation
     */
    override fun visitEnd() {
        val className = Type.getType(descriptor).className
        val annotationType = ClassUtils.forName<A>(className, classLoader)

        annotations += MergedAnnotation.of(classLoader, source, annotationType, attributes)
    }

    class ArrayVisitor(private val consumer: Consumer<Array<Any>>) : AnnotationVisitor(SpringAsmInfo.ASM_VERSION) {

        private val elements = ArrayList<Any>()

        override fun visit(name: String?, value: Any) {
            if (value is Type) {
                elements.add(value.className)
            } else {
                elements.add(value)
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun visitEnd() {
            val array = java.lang.reflect.Array.newInstance(getComponentType(), elements.size) as Array<Any>
            for (i in 0 until elements.size) {
                array[i] = elements[i]
            }
            consumer.accept(array)
        }

        private fun getComponentType(): Class<*> {
            if (elements.isEmpty()) {
                return Any::class.java
            }
            return elements[0].javaClass
        }
    }

    companion object {
        /**
         * 构建MergedAnnotationReadingVisitor的AnnotationVisitor工厂方法
         *
         * @param classLoader ClassLoader
         * @param source source
         * @param descriptor descriptor
         * @param annotations Annotations
         * @return AnnotationVisitor
         */
        @JvmStatic
        fun <A : Annotation> get(
            classLoader: ClassLoader,
            @Nullable source: Any?,
            descriptor: String,
            annotations: MutableCollection<MergedAnnotation<A>>
        ): AnnotationVisitor {
            return MergedAnnotationReadingVisitor(classLoader, source, descriptor, annotations)
        }
    }

}