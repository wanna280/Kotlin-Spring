package com.wanna.framework.core.type.classreading

import com.wanna.framework.core.asm.SpringAsmInfo
import com.wanna.framework.core.type.ClassMetadata
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import org.objectweb.asm.*

/**
 * 基于ASM的方式去提供ClassMetadata的读取的Visitor
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/18
 *
 * @see ClassVisitor
 * @see ClassMetadata
 */
open class ClassMetadataReadingVisitor : ClassVisitor(SpringAsmInfo.ASM_VERSION) {

    /**
     * className
     */
    protected var className: String? = null

    /**
     * 获取当前类的父类的name
     */
    protected var superClassName: String? = null

    /**
     * 外部类的类名
     */
    protected var enclosingClassName: String? = null

    /**
     * 是否是独立的内部类? 其实就是是否是"静态内部类", 对于静态内部类是独立的内部类
     */
    protected var independentInnerClass = false

    /**
     * 当前类的所有接口
     */
    protected var interfaces: Array<String>? = null

    /**
     * 成员类的名字
     */
    protected var memberClassNames = LinkedHashSet<String>()

    /**
     * 类的访问标识符
     */
    protected var access: Int = 0

    /**
     * 访问一个类的签名的相关信息, 可以获取到它的访问标识符、父类、接口信息
     *
     * @param access 类的访问标识符, 可以通过它去判断是否是接口/是否抽象/是否注解等
     * @param name className
     * @param signature 类签名
     * @param superName superClassName
     * @param interfaces interface Names
     */
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<String>
    ) {
        this.access = access
        this.className = getClassName(name)
        val isInterface = (access and Opcodes.ACC_INTERFACE) != 0

        // 对于接口没有superClassName
        if (!isInterface && superName != null) {
            this.superClassName = getClassName(superName)
        }
        // interfaces
        this.interfaces = interfaces.map(this::getClassName).toTypedArray()
    }

    /**
     * 访问外部类的相关信息, 我们可以去初始化enclosingClassName
     *
     * @param owner owner
     * @param name name
     * @param descriptor descriptor
     */
    override fun visitOuterClass(owner: String, name: String, descriptor: String?) {
        this.enclosingClassName = getClassName(owner)
    }

    /**
     * 访问内部类的相关信息
     *
     * @param name name
     */
    override fun visitInnerClass(name: String, outerName: String?, innerName: String?, access: Int) {
        outerName ?: return
        val fqName = getClassName(name)
        val fqOuterName = getClassName(outerName)
        if (this.className == fqName) {
            this.enclosingClassName = fqOuterName
            this.independentInnerClass = (access and Opcodes.ACC_STATIC) != 0
        } else if (this.className == fqOuterName) {
            this.memberClassNames += fqName
        }
    }

    /**
     * 根据给定的资源路径, 去获取到对应的类名
     *
     * @param name resourcePath
     * @return className
     */
    protected open fun getClassName(name: String): String {
        return ClassUtils.convertResourcePathToClassName(name)
    }

    override fun visitEnd() {
        // no-op
    }

    override fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?
    ): FieldVisitor {
        // no-op
        return EmptyFieldVisitor()
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        @Nullable exceptions: Array<out String>?
    ): MethodVisitor {
        // no-op
        return EmptyMethodVisitor()
    }

    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        // no-op
        return EmptyAnnotationVisitor()
    }


    override fun visitSource(source: String?, debug: String?) {
        // no-op
    }

    override fun visitAttribute(attribute: Attribute?) {
        // no-op
    }

    /**
     * 空操作的AnnotationVisitor
     */
    private class EmptyAnnotationVisitor : AnnotationVisitor(SpringAsmInfo.ASM_VERSION) {
        override fun visitAnnotation(name: String?, descriptor: String?): AnnotationVisitor = this
        override fun visitArray(name: String?): AnnotationVisitor = this
    }

    /**
     * 空的MethodVisitor
     */
    private class EmptyMethodVisitor : MethodVisitor(SpringAsmInfo.ASM_VERSION)

    /**
     * 空的FieldVisitor
     */
    private class EmptyFieldVisitor : FieldVisitor(SpringAsmInfo.ASM_VERSION)
}