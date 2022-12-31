package com.wanna.framework.core.type.classreading

import com.wanna.framework.core.annotation.MergedAnnotation
import com.wanna.framework.core.asm.SpringAsmInfo
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Type

/**
 * MergedAnnotation的读取的AnnotationVisitor
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
    private val annotations: MutableSet<MergedAnnotation<A>>
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
     * 在访问结束的时候, 将收集到的一个注解的相关信息去merge到annotations当中去
     *
     * @see MergedAnnotation
     */
    override fun visitEnd() {
        val className = Type.getType(descriptor).className
        val annotationType = ClassUtils.forName<A>(className, classLoader)

        annotations += MergedAnnotation.of(classLoader, source, annotationType, attributes)
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
            annotations: MutableSet<MergedAnnotation<A>>
        ): AnnotationVisitor {
            return MergedAnnotationReadingVisitor(classLoader, source, descriptor, annotations)
        }
    }

}