package com.wanna.framework.core.type.classreading

import com.wanna.framework.core.annotation.AnnotationFilter
import com.wanna.framework.core.annotation.MergedAnnotation
import com.wanna.framework.asm.*
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import java.util.function.Consumer

/**
 * MergedAnnotation的读取的AnnotationVisitor, 将一个注解信息转换成为一个MergedAnnotation对象
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/19
 *
 * @see AnnotationVisitor
 * @see MergedAnnotation
 *
 * @param classLoader ClassLoader
 * @param source source
 * @param annotationType annotationType
 * @param consumer 当收集得到一个注解MergedAnnotation时, 需要执行操作Callback
 */
open class MergedAnnotationReadingVisitor<A : Annotation>(
    private val classLoader: ClassLoader,
    @Nullable private val source: Any?,
    private val annotationType: Class<A>,
    private val consumer: Consumer<MergedAnnotation<A>>
) : AnnotationVisitor(SpringAsmInfo.ASM_VERSION) {

    /**
     * attributes, 负责维护当前要进行处理的注解的相关属性信息, Key-属性名, Value-属性值
     */
    private val attributes = LinkedHashMap<String, Any>()

    /**
     * 访问注解当中的一个普通的属性时, 我们需要去将该属性去收集起来
     *
     * @param name 属性名
     * @param value 属性值
     */
    override fun visit(@Nullable name: String?, value: Any) {
        name ?: return
        attributes[name] = value
    }

    /**
     * 访问注解当中的一个Array的属性时, 我们需要返回一个数组的AnnotationVisitor去提供数组当中的元素的访问
     *
     * @param name 属性名
     * @return 提供注解当中的数组类型当中的属性的访问的AnnotationVisitor
     */
    @Nullable
    override fun visitArray(name: String): AnnotationVisitor? {
        return ArrayVisitor { attributes[name] = it }
    }

    /**
     * 当访问注解当中的一个枚举值类型时, 我们将它转换成为一个Enum对象并收集到Attributes当中去
     *
     * @param name 属性名
     * @param descriptor Enum类型的描述符
     * @param value 枚举值的字符串(可以根据descriptor去获取到枚举类型从而去转换为枚举对象)
     */
    override fun visitEnum(name: String, descriptor: String, value: String) {
        visitEnum(descriptor, value) { attributes[name] = it }
    }

    /**
     * 访问Enum
     *
     * @param descriptor 枚举类型的描述符
     * @param value 枚举值字符串
     * @param consumer 对于获取到的枚举值需要执行的操作Consumer
     */
    private fun <E : Enum<E>> visitEnum(descriptor: String, value: String, consumer: Consumer<E>) {
        val className = Type.getType(descriptor).className
        val enumClass = ClassUtils.forName<E>(className, classLoader)
        consumer.accept(java.lang.Enum.valueOf(enumClass, value))
    }

    /**
     * 当访问注解当中的注解类型的属性值时, 我们需要提供一个AnnotationVisitor去merge其中的信息
     *
     * @param name 属性名
     * @param descriptor 注解类型的Descriptor
     */
    @Nullable
    override fun visitAnnotation(name: String, descriptor: String): AnnotationVisitor? {
        return visitAnnotation<Annotation>(descriptor) { this.attributes[name] = it }
    }

    /**
     * 访问注解当中的注解(Annotation)属性时
     *
     * @param descriptor 注解类型的描述信息descriptor
     * @param consumer 对于收集得到的注解需要去执行的操作
     * @return 提供对于内部的注解的访问的AnnotationVisitor
     */
    @Nullable
    private fun <A : Annotation> visitAnnotation(
        descriptor: String, consumer: Consumer<MergedAnnotation<A>>
    ): AnnotationVisitor? {
        val className = Type.getType(descriptor).className
        if (AnnotationFilter.PLAIN.matches(className)) {
            return null
        }
        val clazz = ClassUtils.forName<A>(className, this.classLoader)
        return get(classLoader, this.source, clazz, consumer)
    }

    /**
     * 在访问结束的时候, 将收集到的一个注解的相关信息去merge到annotations当中去
     *
     * @see MergedAnnotation
     */
    override fun visitEnd() {
        // 执行consumer, 将MergedAnnotation去收集起来
        consumer.accept(MergedAnnotation.of(classLoader, source, annotationType, attributes))
    }

    /**
     * 提供对于注解的数组属性的访问的AnnotationVisitor
     *
     * @param consumer 当访问完该数组属性时, 需要去进行执行的操作
     */
    private inner class ArrayVisitor(private val consumer: Consumer<Array<Any>>) :
        AnnotationVisitor(SpringAsmInfo.ASM_VERSION) {
        /**
         * 访问数组的属性过程当中的结果, 在访问数组当中的元素时, 需要收集到这里
         */
        private val elements = ArrayList<Any>()

        /**
         * 当访问简单类型的componentType的情况
         *
         * @param name null(访问数组属性内部时, 属性名不存在, 都是null)
         * @param value 属性值
         */
        override fun visit(@Nullable name: String?, value: Any) {
            if (value is Type) {
                elements.add(value.className)
            } else {
                elements.add(value)
            }
        }

        /**
         * 当访问Enum[]类型的注解属性的情况, 对于它的component(Enum), 我们直接沿用外部类的收集方式
         *
         * @param name null(访问数组属性内部时, 属性名不存在, 都是null)
         * @param descriptor 类型描述符
         * @param value 枚举值字符串
         */
        override fun visitEnum(@Nullable name: String?, descriptor: String, value: String) {
            this@MergedAnnotationReadingVisitor.visitEnum<Enum<*>>(descriptor, value) { this.elements.add(it) }
        }

        /**
         * 当访问Annotation[]类型的注解属性的情况, 对于它的component(Annotation), 我们直接沿用外部类的收集方式
         *
         * @param name null(访问数组属性内部时, 属性名不存在, 都是null)
         * @param descriptor Annotation类型描述符
         * @return 提供对于Annotation的属性访问的AnnotationVisitor
         */
        @Nullable
        override fun visitAnnotation(@Nullable name: String?, descriptor: String): AnnotationVisitor? {
            return this@MergedAnnotationReadingVisitor.visitAnnotation<Annotation>(descriptor) { this.elements.add(it) }
        }

        /**
         * 注解属性不允许出现多重数组的情况, 这种情况我们无需收集
         *
         * @param name null
         * @return null
         */
        @Nullable
        override fun visitArray(@Nullable name: String?): AnnotationVisitor? = null

        /**
         * 当访问完数组当中的所有的元素时, 我们需要去进行收集起来这些元素
         */
        @Suppress("UNCHECKED_CAST")
        override fun visitEnd() {
            // 先根据componentType去创建出来数组对象, 再将elements当中的元素copy过来,
            // 因为elements的类型是Object[], 因此类型不匹配, 不能直接apply
            val array = java.lang.reflect.Array.newInstance(getComponentType(), elements.size) as Array<Any>
            for (i in 0 until elements.size) {
                array[i] = elements[i]
            }

            // 获取consumer, 将array去收集起来
            consumer.accept(array)
        }

        /**
         * 获取到数组的元素类型componentType
         *
         * @return componentType
         */
        private fun getComponentType(): Class<*> {
            // 如果数组当中没有元素的话, 那么直接返回Object
            if (elements.isEmpty()) {
                return Any::class.java
            }
            // 如果数组当中有元素的话, 那么我们根据数组当中的元素类型去进行类型的推断
            return elements[0].javaClass
        }
    }

    companion object {
        /**
         * 构建MergedAnnotationReadingVisitor的AnnotationVisitor工厂方法
         *
         * @param classLoader ClassLoader(当需要用到类加载时, 使用的ClassLoader)
         * @param source source
         * @param descriptor 注解类型的描述信息descriptor
         * @return AnnotationVisitor
         */
        @Nullable
        @JvmStatic
        fun <A : Annotation> get(
            classLoader: ClassLoader,
            @Nullable source: Any?,
            descriptor: String,
            consumer: Consumer<MergedAnnotation<A>>
        ): AnnotationVisitor? {
            val className = Type.getType(descriptor).className
            if (AnnotationFilter.PLAIN.matches(className)) {
                return null
            }
            try {
                val annotationType = ClassUtils.forName<A>(className, classLoader)
                return get(classLoader, source, annotationType, consumer)
            } catch (ex: Throwable) {
                if (ex is ClassNotFoundException || ex is LinkageError) {
                    return null
                }
                throw ex
            }
        }

        /**
         * 构建MergedAnnotationReadingVisitor的AnnotationVisitor工厂方法
         *
         * @param classLoader ClassLoader
         * @param source source
         * @param annotationType annotationType
         * @param consumer 在收集完所有的注解属性之后, 需要执行的操作的Consumer
         * @return AnnotationVisitor
         */
        @Nullable
        @JvmStatic
        fun <A : Annotation> get(
            classLoader: ClassLoader,
            @Nullable source: Any?,
            annotationType: Class<A>,
            consumer: Consumer<MergedAnnotation<A>>
        ): AnnotationVisitor? {
            return MergedAnnotationReadingVisitor(classLoader, source, annotationType, consumer)
        }
    }

}