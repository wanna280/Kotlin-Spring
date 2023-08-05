package com.wanna.nacos.config.server.model

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/14
 */
class CacheItem(val groupKey: String) {
    @Volatile
    var md5 = ""

    @Volatile
    var lastModifiedTs = 0L

    /**
     * 文件的类型(TEXT/PROPERTIES/JSON等类型)
     */
    var type = ""
}