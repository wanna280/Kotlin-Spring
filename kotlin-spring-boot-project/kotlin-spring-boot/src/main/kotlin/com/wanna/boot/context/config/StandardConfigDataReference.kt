package com.wanna.boot.context.config

import com.wanna.boot.env.PropertySourceLoader
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.StringUtils

/**
 * StandardConfigData的Reference
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 *
 * @param configDataLocation ConfigData Location
 * @param directory directory
 * @param root root
 * @param profile profile
 * @param propertySourceLoader 对当前这个位置的配置文件去提供加载的Loader
 */
class StandardConfigDataReference(
    val configDataLocation: ConfigDataLocation,
    @Nullable val directory: String?,
    root: String,
    @Nullable val profile: String?,
    @Nullable extension: String?,
    val propertySourceLoader: PropertySourceLoader
) {
    /**
     * 生成资源路径, 格式是"root-profile.extension", 例如"application-dev.properties", 如果profile为空的话, 那么"-"也将会被自动去掉
     */
    val resourceLocation: String =
        root + if (StringUtils.hasText(profile)) "-$profile" else "" + (if (StringUtils.hasText(extension)) ".$extension" else "")

    val skippable: Boolean
        get() = configDataLocation.optional || this.directory != null || this.profile != null

    /**
     * toString采用resourceLocation去进行生成
     */
    override fun toString(): String = this.resourceLocation


}