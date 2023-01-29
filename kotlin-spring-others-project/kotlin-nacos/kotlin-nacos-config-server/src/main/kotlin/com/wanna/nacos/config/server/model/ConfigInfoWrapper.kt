package com.wanna.nacos.config.server.model

/**
 * ConfigInfoWrapper
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
class ConfigInfoWrapper : ConfigInfo() {
    var lastModified: Long = 0L
}