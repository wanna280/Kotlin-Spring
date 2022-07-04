package com.wanna.framework.core.util

import java.io.File
import java.io.IOException
import java.net.JarURLConnection
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.jar.JarEntry

/**
 * 扫描指定包下面的所有的类的工具类
 */
object ClassDiscoveryUtils {
    /**
     * 扫描指定的包，获取指定的包下所有的类的集合
     * @param packages 要扫描的包的列表
     * @param classLoader 要使用的ClassLoader
     * @return 指定的包下递归扫描到的类的集合
     */
    fun scan(vararg packages: String, classLoader: ClassLoader? = null): Set<Class<*>> {
        val classLoaderToUse = classLoader ?: Thread.currentThread().contextClassLoader
        val classes: MutableSet<Class<*>> = HashSet()
        packages.forEach { classes.addAll(getClassesForPackage(it, classLoaderToUse)) }
        return classes
    }

    /**
     * 获取指定的包下的所有的类的集合
     * @param pack 指定的package
     * @param classLoader ClassLoader
     * @param recursive 是否递归扫描
     */
    private fun getClassesForPackage(
        pack: String,
        classLoader: ClassLoader,
        recursive: Boolean = true,
    ): Set<Class<*>> {

        val classes: MutableSet<Class<*>> = LinkedHashSet()  // 存放搜寻到的类的集合
        val packageDirName = pack.replace('.', '/')  // 获取包的名字 并进行替换成为目录的形式(将.替换为/)
        try {
            val dirs = classLoader.getResources(packageDirName)
            while (dirs.hasMoreElements()) {
                val url = dirs.nextElement()  // 获取迭代器的下一个元素
                val protocol = url.protocol  // 得到协议的名称
                // 如果是以文件的形式保存在服务器上
                if ("file" == protocol) {
                    // 获取包的物理路径
                    val filePath = URLDecoder.decode(url.file, StandardCharsets.UTF_8)
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findClassesInPackageByFile(pack, filePath, recursive, classes, classLoader)

                    // 如果要扫描的是一个jar包文件
                } else if ("jar" == protocol) {
                    try {
                        // 获取jar包文件
                        val jar = (url.openConnection() as JarURLConnection).jarFile

                        // 在jar包当中去搜寻类
                        findClassesInPackageByJar(
                            pack,
                            jar.entries(),
                            packageDirName,
                            recursive,
                            classes,
                            classLoader
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return classes
    }

    private fun findClassesInPackageByJar(
        pkgName: String,
        entries: Enumeration<JarEntry>,
        packageDirName: String,
        recursive: Boolean,
        classes: MutableSet<Class<*>>,
        classLoader: ClassLoader
    ) {
        // 同样的进行循环迭代
        var packageName = pkgName
        while (entries.hasMoreElements()) {
            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
            val entry = entries.nextElement()
            var name = entry.name
            // 如果是以/开头的
            if (name[0] == '/') {
                // 获取后面的字符串
                name = name.substring(1)
            }
            // 如果前半部分和定义的包名相同
            if (name.startsWith(packageDirName)) {
                val idx = name.lastIndexOf('/')
                // 如果以"/"结尾 是一个包
                if (idx != -1) {
                    // 获取包名 把"/"替换成"."
                    packageName = name.substring(0, idx).replace('/', '.')
                }
                // 如果可以迭代下去 并且是一个包
                if (idx != -1 || recursive) {
                    // 如果是一个.class文件 而且不是目录
                    if (name.endsWith(".class") && !entry.isDirectory) {
                        // 去掉后面的".class" 获取真正的类名
                        val className = name.substring(packageName.length + 1, name.length - 6)
                        try {
                            // 添加到classes
                            classes.add(Class.forName("$packageName.$className", false, classLoader))
                        } catch (e: ClassNotFoundException) {
                            // .error("添加用户自定义视图类错误 找不到此类的.class文件");
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    private fun findClassesInPackageByFile(
        packageName: String,
        packagePath: String,
        recursive: Boolean,
        classes: MutableSet<Class<*>>,
        classLoader: ClassLoader
    ) {
        // 获取此包的目录 建立一个File
        val dir = File(packagePath)
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory) {
            // log.warn("用户定义包名 " + packageName + " 下没有任何文件");
            return
        }
        // 如果存在 就获取包下的所有文件 包括目录
        val dirfiles = dir.listFiles { file ->
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            recursive && file.isDirectory || file.name.endsWith(".class")
        }!!
        for (file in dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory) {
                findClassesInPackageByFile(
                    packageName + "." + file.name,
                    file.absolutePath,
                    recursive,
                    classes,
                    classLoader
                )
            } else {
                // 去掉后面的.class 只留下类名
                val className = file.name.substring(0, file.name.length - 6)
                try {
                    // 添加到集合中去
                    classes.add(classLoader.loadClass("$packageName.$className"))
                } catch (e: ClassNotFoundException) {
                    // log.error("添加用户自定义视图类错误 找不到此类的.class文件");
                    e.printStackTrace()
                }
            }
        }
    }
}