package com.wanna.boot.context.config

import com.wanna.framework.core.io.FileUrlResource
import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.util.StringUtils
import java.io.File
import java.util.*

/**
 * 提供从一个给定的Location的方式去进行资源的加载的策略接口, 支持单个资源路径, 也支持去使用简单的通配符的表达式("*");
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 *
 * @param resourceLoader 提供资源的加载的ResourceLoader
 */
class LocationResourceLoader(private val resourceLoader: ResourceLoader) {

    companion object {

        /**
         * 空的Resource数组常量
         */
        @JvmStatic
        private val EMPTY_RESOURCES = emptyArray<Resource>()

        /**
         * 基于文件路径的排序的File比较器
         */
        @JvmStatic
        private val FILE_PATH_COMPARATOR: Comparator<File> = Comparator.comparing(File::getAbsolutePath)

        /**
         * 基于文件名的File比较器
         */
        private val FILE_NAME_COMPARATOR: Comparator<File> = Comparator.comparing(File::getName)
    }

    /**
     * 检查给定的Location是否含有"*"这种简单的通配表达式
     *
     * @param location location
     * @return 如果含有"*", return true; 否则return false
     */
    fun isPattern(location: String): Boolean = location.contains("*")

    /**
     * 根据给定的Location去进行Resource的加载
     *
     * @param location location
     * @return 根据location加载得到的Resource
     */
    fun getResource(location: String): Resource {
        val cleanPath = StringUtils.cleanPath(location)
        return this.resourceLoader.getResource(cleanPath)
    }

    /**
     * 根据给定的pattern去进行资源的解析
     *
     * @param location pattern
     * @param resourceType 要去获取的资源类型
     * @return 根据pattern去解析得到的资源列表
     */
    fun getResources(location: String, resourceType: ResourceType): Array<Resource> {
        // 切取"*/"之前的内容作为文件夹
        val directoryPath = location.substring(0, location.indexOf("*/"))

        // 切取最后一个"/"之后的内容作为文件名
        val fileName = location.substring(location.lastIndexOf('/') + 1)

        // 根据文件夹路径去获取到Resource
        val resource = getResource(directoryPath)

        // 如果文件夹不存在, 直接return empty
        if (!resource.exists()) {
            return EMPTY_RESOURCES
        }

        // 获取到Resource对应的File
        val file = getFile(resource, location)

        // 如果file不是文件夹, return empty
        if (!file.isDirectory) {
            return EMPTY_RESOURCES
        }

        // 列举出来该文件夹下面的所有的子文件夹
        val subDirectories = file.listFiles(this::isVisibleDirectory)
        if (subDirectories == null || subDirectories.isEmpty()) {
            return EMPTY_RESOURCES
        }
        // 基于路径的方式对文件夹去进行排序
        Arrays.sort(subDirectories, FILE_PATH_COMPARATOR)

        // 如果是文件夹的话, 那么直接将文件夹(File)转换为Resource去返回即可
        if (resourceType == ResourceType.DIRECTORY) {
            return subDirectories.map { FileUrlResource(it.path) }.toTypedArray()
        }

        // 如果不是文件夹的话, 那么需要列出所有的子文件夹下面的所有文件, 去进行挨个检查
        val resources = ArrayList<Resource>()
        for (directory in subDirectories) {
            val files = directory.listFiles { _, name -> name == fileName }
            if (files != null) {
                Arrays.sort(files, FILE_NAME_COMPARATOR)
                resources += files.map { FileUrlResource(it.path) }
            }
        }
        return resources.toTypedArray()
    }

    private fun getFile(resource: Resource, patternLocation: String): File {
        try {
            return resource.getFile()
        } catch (ex: Exception) {
            throw IllegalStateException("Unable to load config data resource from pattern '$patternLocation'", ex)
        }
    }

    /**
     * 检查给定的文件是否是一个可见的文件夹
     *
     * @param file file
     * @return 如果File是文件夹, 并且fileName不是".."的话, return true; 否则return false
     */
    private fun isVisibleDirectory(file: File): Boolean = file.isDirectory && file.name != ".."

    /**
     * 资源类型的枚举值(文件/文件夹)
     */
    enum class ResourceType {
        FILE, DIRECTORY
    }
}