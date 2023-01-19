package com.wanna.framework.core.type.filter

import com.wanna.framework.core.type.classreading.MetadataReader
import com.wanna.framework.core.type.classreading.MetadataReaderFactory
import com.wanna.framework.lang.Nullable
import com.wanna.common.logging.Logger
import com.wanna.common.logging.LoggerFactory

/**
 * 类型的继承关系的匹配的Filter
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/4
 *
 * @param considerInherited 是否要考虑继承关系?
 * @param considerInterfaces 是否需要考虑接口?
 */
abstract class AbstractTypeHierarchyTraversingFilter(
    private val considerInherited: Boolean = true,
    private val considerInterfaces: Boolean = true
) : TypeFilter {

    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun matches(metadataReader: MetadataReader, metadataReaderFactory: MetadataReaderFactory): Boolean {
        if (matchSelf(metadataReader)) {
            return true
        }
        val metadata = metadataReader.classMetadata
        if (matchClassName(metadata.getClassName())) {
            return true
        }
        if (considerInherited) {
            val superClassName = metadata.getSuperClassName()
            if (superClassName != null) {
                val matchSuperClass = matchSuperClass(superClassName)
                if (matchSuperClass != null) {
                    return matchSuperClass
                }
                try {
                    if (match(superClassName, metadataReaderFactory)) {
                        return true
                    }
                } catch (ex: Exception) {
                    // read superClassE rror
                    if (logger.isDebugEnabled) {
                        logger.debug("Could not read super class [${metadata.getSuperClassName()}] of type-filtered class [${metadata.getClassName()}]")
                    }
                }
            }
        }

        if (considerInterfaces) {
            for (interfaceName in metadata.getInterfaceNames()) {
                val matchInterface = matchInterface(interfaceName)
                if (matchInterface != null) {
                    return matchInterface
                }
                try {
                    if (match(interfaceName, metadataReaderFactory)) {
                        return true
                    }
                } catch (ex: Exception) {
                    // read interfaceName Error
                    if (logger.isDebugEnabled) {
                        logger.debug("Could not read interface [[${interfaceName}] of type-filtered class [${metadata.getClassName()}]")
                    }
                }
            }
        }
        return false
    }

    private fun match(className: String, metadataReaderFactory: MetadataReaderFactory): Boolean {
        return matches(metadataReaderFactory.getMetadataReader(className), metadataReaderFactory)
    }

    protected open fun matchSelf(metadataReader: MetadataReader): Boolean {
        return false
    }

    protected open fun matchClassName(className: String): Boolean {
        return false
    }

    @Nullable
    protected open fun matchSuperClass(superClassName: String): Boolean? {
        return null
    }

    @Nullable
    protected open fun matchInterface(interfaceName: String): Boolean? {
        return null
    }
}