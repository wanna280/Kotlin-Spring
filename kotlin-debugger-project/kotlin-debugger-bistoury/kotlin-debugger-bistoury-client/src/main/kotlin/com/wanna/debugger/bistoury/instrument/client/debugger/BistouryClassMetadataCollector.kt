package com.wanna.debugger.bistoury.instrument.client.debugger

import com.wanna.debugger.bistoury.instrument.client.common.AsmVersions
import com.wanna.debugger.bistoury.instrument.client.debugger.bean.ClassField
import com.wanna.debugger.bistoury.instrument.client.debugger.bean.ClassMetadata
import com.wanna.debugger.bistoury.instrument.client.debugger.bean.LocalVariable
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import javax.annotation.Nullable

/**
 * 使用ASM的方式, 去收集出来一个类当中的字段/局部变量的元信息, 并保存到[ClassMetadata]当中
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 *
 * @param classMetadata 用于收集类的元信息的[ClassMetadata], 收集的结果将会保存到这个对象当中
 */
open class BistouryClassMetadataCollector(private val classMetadata: ClassMetadata) : ClassVisitor(AsmVersions.ASM_VERSION) {

    /**
     * 访问字段时, 把字段信息记录下来, 并收集到[ClassMetadata]当中去
     */
    @Nullable
    override fun visitField(
        access: Int,
        name: String,
        descriptor: String,
        @Nullable signature: String?,
        @Nullable value: Any?
    ): FieldVisitor? {
        classMetadata.addField(ClassField(access, name, descriptor))
        return super.visitField(access, name, descriptor, signature, value)
    }

    /**
     * 访问方法时, 把方法的局部变量表的信息记录下来, 并收集到[ClassMetadata]当中去
     */
    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        @Nullable signature: String?,
        @Nullable exceptions: Array<String>?
    ): MethodVisitor {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)

        val methodId = ClassMetadata.createMethodId(name, descriptor)

        // 创建一个新的MethodVisitor, 用于去进行访问局部变量表
        return object : MethodVisitor(AsmVersions.ASM_VERSION, methodVisitor) {

            /**
             * 记录Label和行号之间的映射关系, 因为对于局部变量, 我们需要记录start和end位置的行号信息
             */
            private val labelToLineNumberMapping = LinkedHashMap<String, Int>()

            /**
             * 当访问行号时, 将行号和Label之间的映射关系存起来
             */
            override fun visitLineNumber(line: Int, start: Label) {
                labelToLineNumberMapping[start.toString()] = line
            }

            /**
             * 访问局部变量表当中的变量时, 把局部变量信息去保存到[ClassMetadata]当中
             */
            override fun visitLocalVariable(
                name: String,
                descriptor: String,
                @Nullable signature: String?,
                start: Label,
                end: Label,
                index: Int
            ) {
                super.visitLocalVariable(name, descriptor, signature, start, end, index)
                // 收集起来所有的局部变量表信息当中的变量信息
                classMetadata.addLocalVariable(
                    methodId,
                    LocalVariable(name, descriptor, labelLine(start), labelLine(end), index)
                )
            }

            /**
             * 根据level, 去获取到对应的行号
             */
            private fun labelLine(label: Label): Int {
                val labelStr = label.toString()
                return labelToLineNumberMapping[labelStr] ?: Int.MAX_VALUE
            }
        }
    }
}