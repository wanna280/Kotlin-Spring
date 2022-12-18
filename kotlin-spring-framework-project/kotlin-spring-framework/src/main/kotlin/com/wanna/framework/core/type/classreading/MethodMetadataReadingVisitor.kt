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
 */
open class MethodMetadataReadingVisitor(
    private val methodName: String,
    private val access: Int,
    private val declaringClassName: String,
    private val returnTypeName: String,
    private val classLoader: ClassLoader,
    private val methodMetadataSet: MutableSet<MethodMetadata>
) : MethodVisitor(SpringAsmInfo.ASM_VERSION), MethodMetadata {

    /**
     * MetaAnnotationMap
     */
    protected val metaAnnotationMap = LinkedHashMap<String, Set<String>>()

    /**
     * 维护一个方法上的注解的属性信息(Key是AnnotationName, Value-该注解对应的属性信息)
     */
    protected val attributesMap = LinkedMultiValueMap<String, AnnotationAttributes>()

    override fun getAnnotations(): Array<Annotation> {
        TODO("Not yet implemented")
    }

    override fun getAnnotationAttributes(annotationName: String): Map<String, Any> {
        TODO("Not yet implemented")
    }

    override fun getAnnotationAttributes(annotationClass: Class<out Annotation>): Map<String, Any> =
        getAnnotationAttributes(annotationClass.name)

    /**
     * 当访问一个方法上的一个注解时, 自动回调这个方法作为Callback
     *
     * @param descriptor 注解的描述信息
     * @return 提供对于注解当中的属性读取的AnnotationVisitor
     */
    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        val className = Type.getType(descriptor).className
        this.methodMetadataSet += this
        return AnnotationAttributesReadingVisitor(className, attributesMap, metaAnnotationMap, classLoader)
    }

    override fun getMethodName(): String = this.methodName

    override fun getDeclaringClassName(): String = this.declaringClassName

    override fun getReturnTypeName(): String = this.returnTypeName

    override fun isAbstract(): Boolean = (access and Opcodes.ACC_ABSTRACT) != 0

    override fun isStatic(): Boolean = (access and Opcodes.ACC_STATIC) != 0

    override fun isOverridable(): Boolean = !isStatic() && !isFinal() && !isPrivate()

    override fun isPrivate(): Boolean = (access and Opcodes.ACC_PRIVATE) != 0

    override fun isFinal(): Boolean = (access and Opcodes.ACC_FINAL) != 0
}