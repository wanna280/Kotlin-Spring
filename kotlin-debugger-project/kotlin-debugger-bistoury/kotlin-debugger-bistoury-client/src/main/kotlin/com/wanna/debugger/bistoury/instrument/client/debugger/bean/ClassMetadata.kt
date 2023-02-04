package com.wanna.debugger.bistoury.instrument.client.debugger.bean

import org.objectweb.asm.Opcodes

/**
 * 记录一个类进行ASM的读取时记录下来的元信息, 包括成员变量/类变量/局部变量表等相关信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 *
 * @param className 要去进行描述元信息的类
 */
open class ClassMetadata(private val className: String) {

    /**
     * 类当中的成员变量字段信息
     */
    val fields = ArrayList<ClassField>()

    /**
     * 类当中的static变量字段信息
     */
    val staticFields = ArrayList<ClassField>()

    /**
     * 类当中的各个方法的局部变量信息(Key-methodId, 也就是"methodName+descriptor", Value-该方法当中的LocalVariables局部变量信息).
     *
     * Note: 因为存在有方法重载的情况, 因此methodId必须使用方法名+方法参数去进行生成
     */
    val localVariables = LinkedHashMap<String, MutableList<LocalVariable>>()

    /**
     * 添加一个字段的元信息到当前[ClassMetadata]当中, 可以自动根据是否是static方法去添加到对应的列表当中去
     *
     * @param field 要去进行添加的字段元信息
     */
    open fun addField(field: ClassField) {
        if (isStaticField(field)) {
            staticFields += field
        } else {
            fields += field
        }
    }

    /**
     * 添加一个局部变量的元信息到当前[ClassMetadata]当中
     *
     * @param methodId methodId
     * @param localVariable 该方法当中的局部变量信息
     */
    open fun addLocalVariable(methodId: String, localVariable: LocalVariable) {
        localVariables.computeIfAbsent(methodId) { ArrayList() } += localVariable
    }

    /**
     * 通过访问修饰符去检查给定的字段是否是static字段
     *
     * @param field ClassField
     * @return 如果是static字段, return true; 否则return false
     */
    private fun isStaticField(field: ClassField): Boolean = (field.access and Opcodes.ACC_STATIC) != 0

    companion object {

        /**
         * 根据methodName和descriptor去生成methodId.
         *
         * Note: 因为存在有方法重载的情况, 因此methodId必须使用方法名+方法参数去进行生成
         *
         * @param methodName 方法名methodName
         * @param descriptor 方法的参数/返回值的描述信息
         * @return 生成的methodId
         */
        @JvmStatic
        fun createMethodId(methodName: String, descriptor: String): String {
            return methodName + descriptor
        }
    }

}