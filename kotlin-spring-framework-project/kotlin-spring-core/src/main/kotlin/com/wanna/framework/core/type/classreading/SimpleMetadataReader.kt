package com.wanna.framework.core.type.classreading

import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.core.type.ClassMetadata
import com.wanna.framework.asm.ClassReader
import java.io.IOException

/**
 * [MetadataReader]的简单实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/18
 *
 * @param resource 要去读取的Class文件的Resource
 * @param classLoader ClassLoader
 *
 * @see AnnotationMetadataReadingVisitor
 * @see SimpleAnnotationMetadata
 */
open class SimpleMetadataReader(final override val resource: Resource, val classLoader: ClassLoader) : MetadataReader {

    /**
     * 给定的Resource对应的Class文件的AnnotationMetadata, 维护了一个类上的各个注解的相关信息
     */
    private var metadata: SimpleAnnotationMetadata

    override val classMetadata: ClassMetadata
        get() = metadata

    override val annotationMetadata: AnnotationMetadata
        get() = metadata

    init {
        // 获取到提供类的访问的Visitor
        val visitor = AnnotationMetadataReadingVisitor(classLoader)

        // ClassReader.accept, 让Visitor去访问ClassReader内部的资源, 交给ClassReader去callback给定的Visitor
        getClassReader(resource).accept(visitor, PARSING_OPTIONS)

        // 获取到访问完成的Metadata信息
        this.metadata = visitor.getMetadata()
    }

    companion object {

        /**
         * ClassVisitor访问Class文件时需要使用到的参数
         */
        const val PARSING_OPTIONS = ClassReader.SKIP_DEBUG or ClassReader.SKIP_CODE or ClassReader.SKIP_FRAMES


        /**
         * 根据给定的Resource, 去构建出来ClassReader
         *
         * @param resource Class文件的Resource
         * @return 读取Class文件的ClassReader
         */
        @Throws(IOException::class)
        @JvmStatic
        private fun getClassReader(resource: Resource): ClassReader {
            // 使用Resource去获取到它的InputStream, 并构建出来ClassReader, 去提供类的访问
            val inputStream = resource.getInputStream()
            try {
                return ClassReader(inputStream)
            } catch (ex: Exception) {
                throw IllegalStateException(
                    "ASM ClassReader failed to parse class file - probably due to a new Java class file version that isn't supported yet:$resource",
                    ex
                )
            }
        }
    }
}