package com.wanna.nacos.api.config.listener

import com.wanna.nacos.api.config.ConfigChangeItem

/**
 * 配置文件发生变更的解析器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
interface ConfigChangeParser {

    /**
     * 能否支持去处理这样的配置文件?
     *
     * @param type type
     * @return 如果支持处理的话, return true; 否则return false
     */
    fun isResponsibleFor(type: String): Boolean

    /**
     * 真正地去执行解析配置文件
     *
     * @param oldContent oldContent
     * @param newContent newContent
     * @param type fileType
     * @return 解析出来的配置文件发生变更的情况
     */
    fun doParse(oldContent: String?, newContent: String?, type: String): Map<String, ConfigChangeItem>
}