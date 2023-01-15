package com.wanna.framework.core

import com.wanna.framework.core.asm.SpringAsmInfo
import com.wanna.framework.util.ClassUtils
import org.objectweb.asm.*
import java.io.IOException
import java.lang.reflect.Constructor
import java.lang.reflect.Executable
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * 基于局部变量表的参数名发现器, 基于ASM技术去访问一个类的各个方法当中的各个局部变量表的方式去进行实现;
 * 前提必须保证代码当中是有DEBUG信息的, 如果代码编译时没有产生有DEBUG INFO;
 * 那么也是无法直接使用访问局部变量表的方式去获取到方法的参数名的, 因为参数名信息都完全丢失了, 没有利用的价值.
 * <note>它并不能支持去访问接口当中的方法, 只能访问类当中的方法/构造器方法</note>
 *
 * @see ClassReader
 * @see MethodVisitor
 * @see ClassVisitor
 */
open class LocalVariableTableParameterNameDiscoverer : ParameterNameDiscoverer {
    companion object {
        /**
         * 标识没有任何的debug info的Flag, 代码当中没有debug info, 自然也就无法从局部变量表当中去进行参数的获取
         */
        private val NO_DEBUG_INFO_MAP = emptyMap<Executable, Array<String>>()
    }

    /**
     * 参数名列表的缓存, K-Class, HK-方法/构造器, KV-方法/构造器的参数名列表, 必须保证线程安全, 因此采用ConcurrentHashMap
     */
    private val parameterNamesCache = ConcurrentHashMap<Class<*>, Map<Executable, Array<String>>>()

    override fun getParameterNames(constructor: Constructor<*>) = doGetParameter(constructor)
    override fun getParameterNames(method: Method) = doGetParameter(method)

    /**
     * 基于ASM的方式去获取方法/构造器的参数名列表
     *
     * @param executable 要去寻找的方法/构造器
     * @return 如果没有DEBUG INFO; 那么基本是就没有办法获取到参数信息了; 如果有DEBUG INFO的话那么则可以获取到
     */
    private fun doGetParameter(executable: Executable): Array<String>? {
        val declaringClass = executable.declaringClass
        val executableMap = this.parameterNamesCache.computeIfAbsent(declaringClass, ::inspectClass)
        return if (executableMap != NO_DEBUG_INFO_MAP) executableMap[executable] else null
    }

    /**
     * 检查目标类, 使用ASM技术去对DEBUG INFO当中的局部变量表进行检测, 如果探测不到, 那么return null
     *
     * note:inspect(检查)
     */
    private fun inspectClass(clazz: Class<*>): Map<Executable, Array<String>> {
        // 使用clazz作为相对类路径, 去进行寻找到该clazz的".class"文件, 找不到直接return NO_DEBUG_INFO
        val inputStream = clazz.getResourceAsStream(ClassUtils.getClassFileName(clazz)) ?: return NO_DEBUG_INFO_MAP
        try {
            // 使用ClassReader去进行读取该类的".class"文件的相关信息, 使用访问者的方式去进行访问当中的各个方法
            val classReader = ClassReader(inputStream)
            val executableMap = ConcurrentHashMap<Executable, Array<String>>()
            classReader.accept(ParameterNameDiscoveringVisitor(clazz, executableMap), 0)
            return executableMap
        } catch (ignored: IOException) {

        } finally {
            inputStream.close()
        }
        return NO_DEBUG_INFO_MAP
    }

    /**
     * 这是基于ASM的参数名发现器的Visitor, 主要是提供一个类的访问
     */
    class ParameterNameDiscoveringVisitor(
        private val clazz: Class<*>,
        private val executableMap: MutableMap<Executable, Array<String>>
    ) : ClassVisitor(SpringAsmInfo.ASM_VERSION) {

        companion object {

            /**
             * 类的初始化方法为<clinit>
             */
            private const val STATIC_CLASS_INIT = "<clinit>"

            /**
             * 判断一个方法是否是桥接方法？
             *
             * @param access 访问标识符
             */
            private fun isSyntheticOrBridged(access: Int): Boolean =
                ((access and Opcodes.ACC_SYNTHETIC) or (access and Opcodes.ACC_BRIDGE)) > 0

            /**
             * 方法是否是Static的？
             *
             * @param access 访问标识符
             */
            private fun isStatic(access: Int): Boolean = (access and Opcodes.ACC_STATIC) > 0
        }

        /**
         * @param access 访问修饰符, 通过比特位去进行判断
         * @param name 方法名
         * @param descriptor 方法的描述符
         * @param signature 方法签名
         * @param exceptions 异常表
         */
        override fun visitMethod(
            access: Int,
            name: String,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor? {
            // 排除桥接方法和了类的static初始化方法, 其他的方法才需要去进行visit
            if (!isSyntheticOrBridged(access) && name != STATIC_CLASS_INIT) {
                return LocalVariableTableVisitor(clazz, executableMap, name, descriptor, isStatic(access))
            }
            return null
        }
    }

    /**
     * 这是用于访问一个方法的局部变量表的Visitor
     */
    class LocalVariableTableVisitor(
        private val clazz: Class<*>,
        private val executableMap: MutableMap<Executable, Array<String>>,
        private val name: String,
        private val descriptor: String?,
        private val isStatic: Boolean
    ) : MethodVisitor(SpringAsmInfo.ASM_VERSION) {

        companion object {
            private const val CONSTRUCTOR_NAME = "<init>"
        }

        // 根据方法的描述符, 去解析出来参数的类型
        private val args: Array<Type> = Type.getArgumentTypes(descriptor)

        // 该方法的参数名列表
        private val parameterNames = arrayOfNulls<String>(args.size)

        // 局部变量表当中的每个参数所在的索引？
        private val lvtSlotIndex = computeLvtSlotIndex(isStatic, args)

        // 是否已经有局部变量表的相关信息了？
        private var hasLvtInfo = false

        /**
         * 访问局部变量表的方法
         *
         * @param index 局部变量表的槽位
         */
        override fun visitLocalVariable(
            name: String,
            descriptor: String,
            signature: String?,
            start: Label?,
            end: Label?,
            index: Int
        ) {
            this.hasLvtInfo = true  // flag to true
            // 记录下来index对应的槽位的参数列表
            for (i in this.lvtSlotIndex.indices) {
                if (lvtSlotIndex[i] == index) {
                    parameterNames[i] = name
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun visitEnd() {
            if (this.hasLvtInfo || (this.isStatic && this.args.isEmpty())) {
                executableMap[resolveExecutable()] = parameterNames as Array<String>  // cast no not null array
            }
        }

        /**
         * 解析当前访问的方法/构造器
         *
         * @return 如果当前是构造器, 那么return构造器; 如果当前是方法, 那么return 方法
         */
        private fun resolveExecutable(): Executable {
            val argTypes = Array<Class<*>>(this.args.size) {
                ClassUtils.forName<Any>(this.args[it].className, this.clazz.classLoader)
            }
            try {
                // 如果方法名是"<init>"的话, 说明它是构造器
                return if (this.name == CONSTRUCTOR_NAME) {
                    this.clazz.getDeclaredConstructor(*argTypes)
                } else {
                    this.clazz.getDeclaredMethod(this.name, *argTypes)
                }
            } catch (ex: NoSuchMethodError) {
                throw IllegalArgumentException("方法[name=${this.name}]在.class文件当中被找到了, 但是类当中无法找到这个方法！")
            }
        }

        /**
         * 计算局部变量表当中的各个参数所在的索引, 索引和参数的数组index并不相同;
         * (1)对于Long/Double, 占用8个字节, 需要占用两个局部变量表的槽位;
         * (2)对于非静态方法, 局部变量表的槽位0放的是this, 局部变量从1开始计算;
         * 对于静态方法则不需要this, 因此局部变量从0开始计算
         */
        @Suppress("UNCHECKED_CAST")
        private fun computeLvtSlotIndex(isStatic: Boolean, parameterTypes: Array<Type>): Array<Int> {
            val lvtSlotIndex = arrayOfNulls<Int>(parameterTypes.size)
            // 初始化索引, 如果是static方法, 那么为0; 不是static方法为1(this占用了第一个槽位)
            var currentIndex = if (isStatic) 0 else 1
            for (index in parameterTypes.indices) {
                lvtSlotIndex[index] = currentIndex
                currentIndex += if (isWideType(parameterTypes[index])) 2 else 1
            }

            return lvtSlotIndex as Array<Int> // cast to not null array
        }

        /**
         * 判断它是否是一个宽的类型？Long/Double属于宽类型
         *
         * @return 如果是宽类型, return true; 不然return false
         */
        private fun isWideType(type: Type): Boolean = type == Type.LONG_TYPE || type == Type.DOUBLE_TYPE
    }
}