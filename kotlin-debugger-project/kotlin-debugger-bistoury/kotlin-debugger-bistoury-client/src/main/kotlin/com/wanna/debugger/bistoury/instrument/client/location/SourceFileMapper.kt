package com.wanna.debugger.bistoury.instrument.client.location

import com.wanna.debugger.bistoury.instrument.client.common.AsmVersions
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import java.io.InputStream
import java.util.*
import javax.annotation.Nullable

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/1
 */
class SourceFileMapper(classResources: Iterable<InputStream>) {

    /**
     * 按照行号, 去对源代码当中的Statement去进行排序
     */
    private val statements = TreeMap<Int, ResolvedSourceLocation>()

    /**
     * 已经访问的类的类名列表
     */
    private val classes = LinkedHashSet<String>()

    init {
        for (classResource in classResources) {
            loadClass(classResource)
        }
    }

    fun map(lineNumber: Int): ResolvedSourceLocation {
        return statements[lineNumber] ?: throw IllegalStateException("")
    }


    /**
     * 加载给定的资源, 使用ASM的ClassReader去进行Class文件的资源解析
     *
     * @param classResource 资源输入流
     */
    private fun loadClass(classResource: InputStream) {
        val classReader = ClassReader(classResource)
        classReader.accept(MapperClassVisitor(), ClassReader.SKIP_FRAMES)
    }

    /**
     * 建立类的源代码当中的行和Statement之间的映射关系的ClassVisitor
     */
    private inner class MapperClassVisitor : ClassVisitor(AsmVersions.ASM_VERSION) {

        /**
         * 当前正在访问的类的类名
         */
        private var currentClassName: String? = null

        /**
         * 当访问这个类时, 把当前类的类名去记录下来
         */
        override fun visit(
            version: Int,
            access: Int,
            name: String,
            signature: String?,
            superName: String?,
            interfaces: Array<String>?
        ) {
            this.currentClassName = name
            this@SourceFileMapper.classes += name
            super.visit(version, access, name, signature, superName, interfaces)
        }

        /**
         * 当访问方法当中的行时, 我们需要去进行记录下来该位置的相关信息
         */
        override fun visitMethod(
            access: Int,
            name: String,
            descriptor: String,
            @Nullable signature: String?,
            @Nullable exceptions: Array<String>?
        ): MethodVisitor {
            return object : MethodVisitor(AsmVersions.ASM_VERSION) {
                override fun visitLineNumber(lineNumber: Int, start: Label) {
                    if (statements.containsKey(lineNumber)) {
                        return
                    }
                    val classSignature = "L$currentClassName;"
                    statements[lineNumber] = ResolvedSourceLocation(classSignature, lineNumber, name, descriptor)
                }
            }
        }
    }
}