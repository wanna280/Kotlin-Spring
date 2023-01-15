package com.wanna.boot.devtools.settings

import com.wanna.framework.core.io.support.PropertiesLoaderUtils
import java.net.URL
import java.util.*
import java.util.regex.Pattern

/**
 * 负责从本地的配置文件当中去加载"DevTools"的一些配置信息
 *
 * @see com.wanna.boot.devtools.restart.ChangeableUrls
 * @see com.wanna.boot.devtools.restart.DefaultRestartInitializer
 */
class DevToolsSettings {
    // 重启时应该包含进来的正则表达式
    private val restartIncludePatterns = ArrayList<Pattern>()

    // 重启时应该排除的正则表达式
    private val restartExcludePatterns = ArrayList<Pattern>()

    /**
     * 将加载到的Properties当中的关于"restart"的相关的配置信息转换成为Pattern
     *
     * @param properties 配置信息
     */
    fun add(properties: Properties) {
        restartIncludePatterns += getPatterns(properties, "restart.include.").values
        restartExcludePatterns += getPatterns(properties, "restart.exclude.").values
    }

    /**
     * 判断给的URL是否应该被include？
     *
     * @param url url
     * @return 如果该URL匹配其中一个includePattern, return true; 否则return false
     */
    fun isRestartInclude(url: URL): Boolean = isMatch(url.toString(), restartIncludePatterns)

    /**
     * 判断给的URL是否应该被exclude？
     *
     * @param url url
     * @return 如果该URL匹配其中一个excludePattern, return true; 否则return false
     */
    fun isRestartExclude(url: URL): Boolean = isMatch(url.toString(), restartExcludePatterns)

    /**
     * 判断给定的URL是否符合给定的正则表达式？
     *
     * @param url url
     * @param patterns 要去进行匹配的正则表达式
     * @return 如果存在有其中一个正则表达式匹配了url, 那么return true; 否则return false
     */
    private fun isMatch(url: String, patterns: Collection<Pattern>): Boolean {
        patterns.forEach {
            if (it.matcher(url).find()) {
                return true
            }
        }
        return false
    }

    /**
     * 获取到候选的Properties当中, 所有的以prefix作为开头去进行配置配置的正则表达式
     *
     * @param prefix 要去匹配的prefix
     * @param properties 候选的Properties
     * @return Patterns(key-propertyName, value-Pattern)
     */
    private fun getPatterns(properties: Properties, prefix: String): Map<String, Pattern> =
        properties.filter { it.key.toString().startsWith(prefix) }  // 过滤出来所有的以prefix作为开头的配置信息
            .map { it.key.toString() to Pattern.compile(it.value.toString()) }  // 将Value使用Pattern去进行转换
            .toMap()  // 转为Map

    companion object {
        // DevToolsSetting的资源路径
        const val SETTINGS_RESOURCE_LOCATION = "META-INF/spring-devtools.properties"

        // 单例的DevToolsSettings对象
        private var setting: DevToolsSettings? = null

        @JvmStatic
        fun get(): DevToolsSettings {
            if (this.setting == null) {
                this.setting = load()
            }
            return this.setting!!
        }

        @JvmStatic
        private fun load(): DevToolsSettings = load(SETTINGS_RESOURCE_LOCATION)

        @JvmStatic
        private fun load(location: String): DevToolsSettings {
            val devToolsSettings = DevToolsSettings()
            val properties =
                PropertiesLoaderUtils.loadAllProperties(location, Thread.currentThread().contextClassLoader)
            devToolsSettings.add(properties)
            return devToolsSettings
        }
    }
}