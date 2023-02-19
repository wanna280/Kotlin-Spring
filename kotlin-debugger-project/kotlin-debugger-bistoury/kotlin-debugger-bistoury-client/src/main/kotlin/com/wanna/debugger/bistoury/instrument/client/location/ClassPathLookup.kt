package com.wanna.debugger.bistoury.instrument.client.location

import java.io.File
import javax.annotation.Nullable

/**
 * 提供ClassPath下相关的资源的解析和寻找
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/2
 *
 * @param useDefaultClassPath 是否需要使用默认的ClassPath?
 * @param extraClassPath 额外要去进行使用的ClassPath
 */
open class ClassPathLookup(private val useDefaultClassPath: Boolean, @Nullable extraClassPath: Array<String>?) {

    /**
     * 需要去进行额外使用的ClassPath路径
     */
    private val extraClassPath: Array<String>

    private val classResourcesIndexers = ArrayList<ClassResourcesIndexer>()

    init {
        var extraClassPathToUse = extraClassPath

        // 如果需要使用默认的ClassPath, 并且没有指定额外的ClassPath的话, 那么尝试探测Tomcat/Jetty的默认类路径
        if (useDefaultClassPath && extraClassPath.isNullOrEmpty()) {
            extraClassPathToUse = findExtraClassPath()
        }
        this.extraClassPath = extraClassPathToUse ?: emptyArray()

        // 根据ClassPath, 去初始化应用程序当中的资源信息的索引
        indexApplicationResources()
    }

    companion object {

        /**
         * 获取JVM的默认的ClassPath的系统属性Key
         */
        private const val JAVA_CLASS_PATH_KEY = "java.class.path"

        /**
         * Java文件的扩展名
         */
        private const val JAVA_FILE_EXTENSION = ".java"

        /**
         * 尝试去找出一些额外的应用程序的类的所在的位置.
         *
         * 通常情况下, 应用程序的类所在的位置是在一些文件夹里, 或者是在Jar包里, 并且通常是以ClassPath方式去进行指定的.
         * 一个应用程序也可以通过自定义ClassLoader的方式去实现从任何地方去进行类的加载.通常情况下, 推断应用程序所有的潜在的
         * 类所在的位置是基本上不可能的.
         *
         * 对于Web服务器(例如Jetty/Tomcat)就是一个很特殊的例子, Web服务器就经常从不在ClassPath下的地方去进行
         * 类加载. 但是对于想要去进行WebServer下的Debug又是非常重要的, 另外一种方式是让用户去进行手动指定位置, 但是这会让
         * Debugger的部署变得很复杂.
         *
         * 一般情况下, 想要去决定一个Web应用的所在位置是非常难的, 我们需要去读取Web服务器的配置文件, 并且每种类型的Web服务器,
         * 还会存在有不同的配置文件, 因此我们只能去做一些简单的支持, 但是最常见的情况下, Web服务器都会有一个默认的ROOT路径, 因此
         * 在这个方法当中, 针对于Tomcat和Jetty的ROOT路径去对应用程序所在的类去进行推测.
         *
         * @return Tomcat/Jetty应用的应用程序类可能存放的位置
         */
        @JvmStatic
        fun findExtraClassPath(): Array<String> {
            val paths = LinkedHashSet<String>()

            // Tomcat
            addSystemPropertyRelative(paths, "catalina.base", "webapps/ROOT/WEB-INF/lib")
            addSystemPropertyRelative(paths, "catalina.base", "webapps/ROOT/WEB-INF/classes")

            // Jetty
            addSystemPropertyRelative(paths, "jetty.base", "webapps/ROOT/WEB-INF/lib")
            addSystemPropertyRelative(paths, "jetty.base", "webapps/ROOT/WEB-INF/classes")

            // Jetty比较新的版本, 将ROOT目录改名成了root目录
            addSystemPropertyRelative(paths, "jetty.base", "webapps/root/WEB-INF/lib")
            addSystemPropertyRelative(paths, "jetty.base", "webapps/root/WEB-INF/classes")

            return paths.toTypedArray()
        }

        /**
         * 基于给定的name作为系统属性Key, 获取到系统属性值作为basePath, 并拼接上给定的相对路径去计算得到最终的路径, 并完成收集.
         *
         * Note: 如果属性名name对应的系统属性不存在, 或者是计算得到的最终路径不存在的话, 那么都不会被收集
         *
         * @param paths 用于收集最终的path的列表
         * @param name 用于获取basePath的系统属性值
         * @param suffix 相对basePath的相对路径
         */
        @JvmStatic
        private fun addSystemPropertyRelative(paths: MutableSet<String>, name: String, suffix: String) {
            val property = System.getProperty(name)
            if (property.isNullOrBlank()) {
                return
            }
            val path = File(property, suffix)
            if (!path.exists()) {
                return
            }
            paths += path.toString()
        }
    }


    /**
     * 解析给定位置的代码行所在的方法的相关位置信息
     *
     * @param sourceJavaFile Java源代码文件路径(例如"com/wanna/Test.java")
     * @param lineNumber 源代码所处的行号
     * @return 解析到的源代码的位置的详细信息
     */
    open fun resolveSourceLocation(sourceJavaFile: String, lineNumber: Int): ResolvedSourceLocation {
        val index = sourceJavaFile.indexOf(JAVA_FILE_EXTENSION)
        if (index == -1) {
            throw IllegalStateException("Only files with .java extension are supported")
        }
        val inputStream =
            ClassLoader.getSystemClassLoader().getResourceAsStream(sourceJavaFile.substring(0, index) + ".class")
                ?: throw IllegalStateException("Cannot get resource")

        val sourceFileMapper = SourceFileMapper(listOf(inputStream))

        return sourceFileMapper.map(lineNumber)
    }

    /**
     * 通过JVM的ClassPath和额外的ClassPath路径, 去列举出来所有的.class和.jar文件的路径
     */
    private fun indexApplicationResources() {
        val effectiveClassPath = LinkedHashSet<String>()

        // 如果需要使用原始的JVM的ClassPath的话, 那么收集起来
        if (useDefaultClassPath) {
            val jvmClassPath = System.getProperty(JAVA_CLASS_PATH_KEY, "")
            effectiveClassPath += jvmClassPath.split(File.separatorChar)
        }

        // 添加额外的ClassPath
        if (extraClassPath.isNotEmpty()) {
            effectiveClassPath += extraClassPath
        }

        // 根据给定的有效ClassPath, 去进行索引的构建...
        val resourceIndexer = ResourceIndexer(effectiveClassPath)
        for (source in resourceIndexer.sources) {
            classResourcesIndexers += ClassResourcesIndexer(source)
        }
    }
}