package com.wanna.framework.core.type.classreading

import com.wanna.framework.asm.AnnotationVisitor
import com.wanna.framework.asm.MethodVisitor
import com.wanna.framework.asm.SpringAsmInfo
import com.wanna.framework.core.annotation.MergedAnnotation
import com.wanna.framework.core.annotation.MergedAnnotations
import com.wanna.framework.core.type.MethodMetadata
import com.wanna.framework.lang.Nullable

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
     * 一个方法上的MergedAnnotation注解列表
     */
    private var annotations = LinkedHashSet<MergedAnnotation<Annotation>>()

    /**
     * 当访问一个方法上标注的一个注解时, 自动回调这个方法作为Callback
     *
     * @param descriptor 注解的描述信息(例如"Ljava.lang.String;"这样的格式)
     * @return 提供对于注解当中的属性读取的AnnotationVisitor
     */
    @Nullable
    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
        return MergedAnnotationReadingVisitor.get(classLoader, null, descriptor, this.annotations::add)
    }

    /**
     * 当访问结束时, 需要将所有的注解信息去Merge成为MergedAnnotations, 并收集到MethodMetadata当中
     *
     * @see MergedAnnotations
     */
    override fun visitEnd() {
        // MergedAnnotations
        val mergedAnnotations = MergedAnnotations.of(this.annotations.toTypedArray())
        this.methodMetadataSet += SimpleMethodMetadata(
            this.methodName, this.declaringClassName, this.returnTypeName, access, mergedAnnotations
        )
    }
}