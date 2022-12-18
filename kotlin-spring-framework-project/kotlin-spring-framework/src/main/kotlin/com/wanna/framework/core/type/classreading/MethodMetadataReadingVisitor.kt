package com.wanna.framework.core.type.classreading

import com.wanna.framework.context.annotation.AnnotationAttributes
import com.wanna.framework.core.asm.SpringAsmInfo
import com.wanna.framework.core.type.MethodMetadata
import com.wanna.framework.util.LinkedMultiValueMap
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/**
 * 提供MethodMetadata的读取的MethodVisitor
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/18
 *
 * @param methodName 要去读取的方法的方法名
 * @param access 该方法的访问修饰符
 * @param declaringClassName 定义该方法的类
 * @param returnTypeName 该方法的返回值类型
 * @param classLoader ClassLoader
 * @param methodMetadataSet MethodMetadataSet
 */
open class MethodMetadataReadingVisitor(
    private val methodName: String,
    private val access: Int,
    private val declaringClassName: String,
    private val returnTypeName: String,
    private val classLoader: ClassLoader,
    private val methodMetadataSet: MutableSet<MethodMetadata>
) : MethodVisitor(SpringAsmInfo.ASM_VERSION) {

    /**
     * MetaAnnotationMap
     */
    protected val metaAnnotationMap = LinkedHashMap<String, Set<String>>()

    /**
     * 维护一个方法上的注解的属性信息(Key是AnnotationName, Value-该注解对应的属性信息)
     */
    protected val attributesMap = LinkedMultiValueMap<String, AnnotationAttributes>()

    /**
     *
     */
    private val annotationSet = LinkedHashSet<String>()

    /**
     * MethodMetadata
     */
    private var methodMetadata: SimpleMethodMetadata? = null

    /**
     * 当访问一个方法上的一个注解时, 自动回调这个方法作为Callback
     *
     * @param descriptor 注解的描述信息(例如"Ljava.lang.String;")
     * @return 提供对于注解当中的属性读取的AnnotationVisitor
     */
    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        val className = Type.getType(descriptor).className
        this.annotationSet += className
        this.methodMetadataSet += buildMetadata()
        return AnnotationAttributesReadingVisitor(className, attributesMap, metaAnnotationMap, classLoader)
    }

    override fun visitEnd() {
        this.methodMetadata = buildMetadata()
    }

    open fun buildMetadata(): SimpleMethodMetadata {
        return SimpleMethodMetadata(
            this.methodName,
            this.declaringClassName,
            this.returnTypeName,
            access,
            emptyArray(),
            this.attributesMap,
            this.annotationSet
        )
    }

    open fun getMetadata(): SimpleMethodMetadata =
        this.methodMetadata ?: throw IllegalStateException("MethodMetadata is null")
}