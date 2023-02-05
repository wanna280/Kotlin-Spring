package com.wanna.boot.loader.tools

import java.util.jar.Attributes
import java.util.jar.JarFile
import java.util.jar.Manifest
import javax.annotation.Nullable

/**
 * 打包器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
abstract class Packager {

    companion object {

        private const val MAIN_CLASS_ATTRIBUTE = "Main-Class"

        private const val START_CLASS_ATTRIBUTE = "Start-Class"

        private const val BOOT_VERSION_ATTRIBUTE = "Spring-Boot-Version"

        private const val BOOT_CLASSES_ATTRIBUTE = "Spring-Boot-Classes"

        private const val BOOT_LIB_ATTRIBUTE = "Spring-Boot-Lib"

        private const val BOOT_CLASSPATH_INDEX_ATTRIBUTE = "Spring-Boot-Classpath-Index"

        private const val BOOT_LAYERS_INDEX_ATTRIBUTE = "Spring-Boot-Layers-Index"
    }

    var mainClass: String? = null

    fun write(sourceJar: JarFile, libraries: Libraries, writer: AbstractJarWriter) {
        write(sourceJar, writer)
    }

    private fun write(sourceJar: JarFile, writer: AbstractJarWriter) {
        // 写入Manifest到Jar包当中
        writer.writeManifest(buildManifest(sourceJar))

        // 写入LoaderClasses
        writeLoaderClasses(writer)

    }

    /**
     * 为给定的JarFile, 去构建Manifest
     *
     * @param sourceJar JarFile
     * @return Manifest
     */
    private fun buildManifest(sourceJar: JarFile): Manifest {
        val manifest = createInitialManifest(sourceJar)
        addMainAndStartAttributes(sourceJar, manifest)
        addBootAttributes(manifest)
        return manifest
    }

    /**
     * 添加"Main-Class"和"Start-Class"的属性到Manifest当中
     */
    private fun addMainAndStartAttributes(sourceJar: JarFile, manifest: Manifest) {
        val mainClass = getMainClass(sourceJar, manifest)

    }

    private fun addBootAttributes(manifest: Manifest) {

        addBootAttributesForLayout(manifest.mainAttributes)
    }

    private fun addBootAttributesForLayout(attributes: Attributes) {

    }

    @Nullable
    private fun getMainClass(sourceJar: JarFile, manifest: Manifest): String? {
        if (mainClass != null) {
            return mainClass
        }
        val startClass = manifest.mainAttributes.getValue(START_CLASS_ATTRIBUTE)
        if (startClass != null) {
            return startClass
        }
        // 从给定的Jar当中, 去寻找Main方法...
        return findMainMethodWithTimeoutWarning(sourceJar)
    }

    @Nullable
    private fun findMainMethodWithTimeoutWarning(sourceJar: JarFile): String? {
        return findMainMethod(sourceJar)
    }

    protected open fun findMainMethod(sourceJar: JarFile): String? {
        return null
    }

    private fun writeLoaderClasses(writer: AbstractJarWriter) {
        writer.writeLoaderClasses()
    }

    /**
     * 为给定的JarFile, 去创建一个初始的Manifest
     *
     * @param sourceJar SourceJar
     * @return Manifest
     */
    private fun createInitialManifest(sourceJar: JarFile): Manifest {
        if (sourceJar.manifest != null) {
            return Manifest(sourceJar.manifest)
        }
        val manifest = Manifest()
        manifest.mainAttributes.putValue("Manifest-Version", "1.0")
        return manifest
    }

}