package com.wanna.boot.devtools.autoconfigure

import com.wanna.boot.context.properties.ConfigurationProperties
import com.wanna.boot.context.properties.NestedConfigurationProperty

/**
 * 维护"SpringBoot-DevTools"的配置信息
 */
@ConfigurationProperties("spring.devtools")
open class DevToolsProperties {

    // 本地的关于Restart的配置信息
    @NestedConfigurationProperty
    var restart = Restart()

    // 远程的Restart的相关配置信息
    @NestedConfigurationProperty
    var remote = RemoteDevToolsProperties()

    /**
     * 关于DevTools的重启相关的配置信息
     */
    class Restart {
        companion object {
            // 默认要去进行排除的路径
            const val DEFAULT_EXCLUDE =
                "resource/**,META-INF/maven/**,META-INF/resources/**,public/**,static/**,template/**,META-INF/docs/**"
        }

        // 是否要开启Restart? 
        var enable: Boolean = true

        // 触发Restart的文件(默认为所有的文件都当做触发Restart的文件, 如果需要, 需要自行配置)
        // 只需要配置fileName即可, 无需添加路径, 支持去匹配所有的垃圾下的该fileName的文件
        var triggerFile: String? = null

        // 要去进行排除的路径(配置了这些路径之后, 这些文件的更改将会不被重启)
        var exclude: String = DEFAULT_EXCLUDE

        // poll的轮询时间
        var pollInterval = 1000L

        // quiet的间隔时间
        var quietPeriod = 400L

        // 额外要监控的路径
        var additionalPaths: List<String> = ArrayList()
    }
}