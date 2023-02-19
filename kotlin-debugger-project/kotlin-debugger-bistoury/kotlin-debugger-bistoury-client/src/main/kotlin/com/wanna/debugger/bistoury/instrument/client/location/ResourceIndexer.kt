package com.wanna.debugger.bistoury.instrument.client.location

import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.jar.JarFile

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/4
 */
class ResourceIndexer(paths: Iterable<String>) {

    val fileSystemResourcesSources: List<FileSystemResourcesSource>

    val sources: List<ResourcesSource>

    init {
        val fileSystemResourcesSources = ArrayList<FileSystemResourcesSource>()
        val sources = ArrayList<ResourcesSource>()

        // 将给定的这些路径, 去分别创建FileSystemResourcesSource
        for (path in paths) {
            if (path.isBlank() || path == "/" || path.contains("//")) {
                continue
            }
            fileSystemResourcesSources += FileSystemResourcesSource(File(path))
        }
        sources += fileSystemResourcesSources

        for (fileSystemResourcesSource in fileSystemResourcesSources) {

        }

        // 可能是没有扩展名的Jar包(例如 java -jar /tmp/mycode)
        for (path in paths) {
            val file = File(path)
            val hasExtension = path.lastIndexOf('.') > path.lastIndexOf('/')

            // check isFile & no Extension
            if (!hasExtension && file.isFile) {
                try {
                    sources += JarResourcesSource(JarFile(file))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

        this.fileSystemResourcesSources = Collections.unmodifiableList(fileSystemResourcesSources)
        this.sources = Collections.unmodifiableList(sources)
    }


    /**
     * 代表了一系列的应用程序资源的集合的Source, 例如一个目录/一个Jar包
     */
    interface ResourcesSource {

        fun getResourcesDatabase(): ResourcesDatabase

        fun getResource(resourcePath: String): InputStream
    }

    /**
     * 代表了一系列应用程序资源的目录(例如被解压的Jar包)或者是单个.class文件
     */
    class FileSystemResourcesSource(file: File) : ResourcesSource {

        private val path: Path = file.toPath()

        override fun getResourcesDatabase(): ResourcesDatabase {
            TODO("Not yet implemented")
        }

        override fun getResource(resourcePath: String): InputStream {
            return Files.newInputStream(path.resolve(resourcePath))
        }
    }

    /**
     * 代表了一个Jar包的应用程序资源
     */
    class JarResourcesSource(private val jarFile: JarFile) : ResourcesSource {
        override fun getResourcesDatabase(): ResourcesDatabase {
            TODO("Not yet implemented")
        }

        override fun getResource(resourcePath: String): InputStream {
            return jarFile.getInputStream(jarFile.getJarEntry(resourcePath))
        }
    }

}