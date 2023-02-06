package com.wanna.boot.loader.tools

import com.wanna.framework.asm.SpringAsmInfo
import org.objectweb.asm.*
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.io.InputStream
import javax.annotation.Nullable

/**
 * SpringBoot的主类的寻找器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
object MainClassFinder {

    /**
     * ".class"后缀
     */
    private const val DOT_CLASS = ".class"

    /**
     * main方法的方法名
     */
    private const val MAIN_METHOD_NAME = "main"

    /**
     * main方法的参数&返回值的描述符
     */
    private const val MAIN_METHOD_DESCRIPTOR = "([Ljava/lang/String;)V"

    /**
     * 对于Java包目录去进行过滤的[FileFilter]
     */
    @JvmStatic
    private val PACKAGE_DIRECTORY_FILTER: FileFilter = FileFilter { isPackageDirectory(it) }

    /**
     * 对于Java的字节码文件去进行过滤的的[FileFilter]
     */
    @JvmStatic
    private val CLASS_FILE_FILTER: FileFilter = FileFilter { isClassFile(it) }

    /**
     * 从给定的目录下的所有的".class"文件去进行处理, 从而去找到一个最合适的主启动类
     *
     * @param rootDirectory 根目录
     * @param annotationName 要去进行搜索主启动类的注解名
     * @return 从给定的目录下, 去搜索得到的主启动类(没有搜索得到的话, 那么return null)
     */
    @Nullable
    @JvmStatic
    fun findSingleMainClass(rootDirectory: File, annotationName: String): String? {
        val mainClassCallback = SingleMainClassCallback(annotationName)
        doWithMainClasses(rootDirectory, mainClassCallback)
        return mainClassCallback.getMainClassName()
    }

    /**
     * 搜索得到给定的目录下的全部主类, 并使用[MainClassCallback]去进行处理
     *
     * @param rootDirectory 要去进行搜索主类的文件夹
     * @param mainClassCallback 要对主类去进行处理的回调函数
     * @return 根据给定的回调函数去处理的结果(如果没有找到主类, return null)
     */
    @Nullable
    @JvmStatic
    private fun <T : Any> doWithMainClasses(rootDirectory: File, mainClassCallback: MainClassCallback<T>): T? {
        if (!rootDirectory.exists()) {
            return null
        }
        if (!rootDirectory.isDirectory) {
            throw IllegalStateException("Invalid root directory '$rootDirectory'")
        }
        val prefix = rootDirectory.absolutePath + "/"

        // 使用BFS广度优先遍历的方式, 去遍历给定的根目录下的所有的文件, 去找到合适的主类...
        val stack = ArrayDeque<File>()
        stack += rootDirectory
        while (stack.isNotEmpty()) {
            val file = stack.removeFirst()

            if (file.isFile) {
                // asset file is a ".class" file
                FileInputStream(file).use {

                    // 基于ASM的方式去统计该类上标注的注解信息, 以及该类当中是否存在有main方法?
                    val classDescriptor = createClassDescriptor(it)
                    if (classDescriptor != null && classDescriptor.mainMethodFound) {
                        val className = convertToClassName(file.absolutePath, prefix)

                        // 使用MainClass的callback回调函数去进行处理...
                        val result = mainClassCallback.doWith(MainClass(className, classDescriptor.annotationNames))
                        if (result != null) {
                            return result
                        }
                    }
                }
            }

            // 如果是一个文件夹的话, 将该文件夹下的".class"文件和目录加入到stack当中...等待去进行递归处理
            if (file.isDirectory) {
                pushAllSorted(stack, file.listFiles(PACKAGE_DIRECTORY_FILTER) ?: emptyArray())
                pushAllSorted(stack, file.listFiles(CLASS_FILE_FILTER) ?: emptyArray())
            }
        }
        return null
    }

    /**
     * 将给定的文件路径, 去转换成为className
     *
     * @param name ".class"文件的文件名全路径
     * @param prefix root目录, 需要去进行去掉的前缀
     * @return 计算得到的className
     */
    private fun convertToClassName(name: String, prefix: String): String {
        var nameToUse = name

        // 将文件目录分隔符替换成为"."
        nameToUse = nameToUse.replace("/", ".")
        nameToUse = nameToUse.replace("\\", ".")

        // 去掉文件名末尾的".class"
        nameToUse = nameToUse.substring(0, nameToUse.length - DOT_CLASS.length)

        // 去掉文件名开头的目录
        nameToUse = nameToUse.substring(prefix.length)

        return nameToUse
    }


    /**
     * 根据给定的".class"文件的输入流[InputStream], 基于ASM的方式去计算得到该类的相关元信息
     *
     * @param inputStream ".class"文件的输入流
     * @return 基于ASM的方式去计算得到的类的元信息(计算失败, return null)
     */
    @Nullable
    private fun createClassDescriptor(inputStream: InputStream): ClassDescriptor? {
        try {
            val classReader = ClassReader(inputStream)
            val classDescriptor = ClassDescriptor()
            classReader.accept(classDescriptor, ClassReader.SKIP_CODE)
            return classDescriptor
        } catch (ex: Exception) {
            return null
        }
    }

    /**
     * 将给定的文件列表, 先去按照"name"去进行排序, 并添加到栈当中
     *
     * @param stack 文件栈
     * @param files 要去添加到栈当中的文件列表
     */
    @JvmStatic
    private fun pushAllSorted(stack: ArrayDeque<File>, files: Array<File>) {
        files.sortBy(File::getName)
        files.forEach(stack::addLast)
    }

    /**
     * 检查给定的[File], 是否是一个Java的包目录? (对于"."开头的文件夹, 肯定应该skip掉, 比如类似"."/".."/".git"这样的文件夹)
     *
     * @param file 要去进行检查的文件
     * @return 如果它是一个Java的包目录, 那么return true; 否则return false
     */
    @JvmStatic
    private fun isPackageDirectory(file: File): Boolean {
        return file.isDirectory && !file.startsWith(".")
    }

    /**
     * 检查给定的[File], 是否是一个Java的".class"字节码文件?
     *
     * @param file 要去进行检查的文件
     * @return 如果它是一个Java的字节码文件, 那么return true; 否则return false
     */
    @JvmStatic
    private fun isClassFile(file: File): Boolean {
        return file.isFile && file.name.endsWith(DOT_CLASS)
    }

    /**
     * ClassVisitor, 基于ASM, 去统计一个类的相关信息
     *
     * * 1.统计是否有"public static void main(String[] args)"方法
     * * 2.统计该类上的所有的注解信息
     */
    private class ClassDescriptor : ClassVisitor(SpringAsmInfo.ASM_VERSION) {

        /**
         * 该类当中的收集起来所有的注解列表
         */
        val annotationNames = LinkedHashSet<String>()

        /**
         * 是否在这个类当中找到了main方法?
         */
        var mainMethodFound = false

        /**
         * 访问注解时, 我们收集起来所有的注解类名
         *
         * @param descriptor 注解类型的描述符
         * @param visible 是否可见
         */
        @Nullable
        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
            annotationNames += Type.getType(descriptor).className
            return null
        }

        /**
         * 当访问方法时, 我们需要统计出来该方法是否是main方法
         *
         * @param access 方法访问修饰符
         * @param name 方法名
         * @param descriptor 方法描述符
         * @param signature 方法签名
         * @param exceptions 方法异常表
         */
        @Nullable
        override fun visitMethod(
            access: Int,
            name: String,
            descriptor: String,
            signature: String?,
            exceptions: Array<String>?
        ): MethodVisitor? {
            if (name == MAIN_METHOD_NAME
                && (access and Opcodes.ACC_PUBLIC) != 0 && (access and Opcodes.ACC_STATIC) != 0
                && descriptor == MAIN_METHOD_DESCRIPTOR
            ) {
                mainMethodFound = true
            }
            return null
        }
    }


    /**
     * 根据给定的注解类名, 去获取得到单个[MainClass]最合适的Callback回调函数
     *
     * * 1.如果有标注了该注解的主类, 那么选用它去作为最合适的主类;
     * * 2.如果没有标注该注解的主类, 只要有合适的主类就行;
     * * 3.如果找到了多个主类, 但是无法决策出来最合适的一个的话, 那么丢出[IllegalStateException]异常
     *
     * @param annotationName 要去进行搜索主类的注解
     */
    private class SingleMainClassCallback(private val annotationName: String) : MainClassCallback<Any> {

        /**
         * 收集起来所有的[MainClass]
         */
        private val mainClasses = LinkedHashSet<MainClass>()

        /**
         * 对[MainClass]去进行处理时, 我们去将该[MainClass]去进行收集起来
         *
         * @param mainClass MainClass
         * @return null
         */
        @Nullable
        override fun doWith(mainClass: MainClass): Any? {
            mainClasses += mainClass
            return null
        }

        /**
         * 获取到最合适的一个[MainClass]的类名
         *
         * @return 最合适的主类类名(如果不存在有主类的话, 那么return null)
         *
         * @throws IllegalStateException 如果找到了多个合适的主类, 无法决策出最合适的一个
         */
        @Throws(IllegalStateException::class)
        @Nullable
        fun getMainClassName(): String? {
            val candidates = LinkedHashSet<MainClass>()
            for (mainClass in mainClasses) {
                if (mainClass.annotationNames.contains(annotationName)) {
                    candidates += mainClass
                }
            }
            if (candidates.isEmpty()) {
                candidates += mainClasses
            }

            if (candidates.size > 1) {
                throw IllegalStateException("Unable to find a single main class from the following candidates $mainClasses")
            }
            return if (candidates.isEmpty()) return null else candidates.iterator().next().name
        }
    }

    /**
     * 处理[MainClass]的回调函数
     *
     * @param T 处理MainClass的返回值类型
     */
    fun interface MainClassCallback<T : Any> {

        /**
         * 执行对于[MainClass]的处理
         *
         * @param mainClass 需要去进行处理的MainClass
         * @return 处理MainClass的结果
         */
        @Nullable
        fun doWith(mainClass: MainClass): T?
    }

    /**
     * 封装有main方法的类的相关信息
     *
     * @param name 主类名
     * @param annotationNames 该类上的注解列表
     */
    data class MainClass(val name: String, val annotationNames: Set<String>)
}