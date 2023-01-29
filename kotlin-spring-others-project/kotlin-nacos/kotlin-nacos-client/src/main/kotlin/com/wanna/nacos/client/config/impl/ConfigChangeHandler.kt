package com.wanna.nacos.client.config.impl

import com.wanna.nacos.api.config.ConfigChangeItem
import com.wanna.nacos.api.config.listener.ConfigChangeParser
import java.util.*
import kotlin.collections.ArrayList

/**
 * 配置文件发生变更的处理器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
object ConfigChangeHandler {

    /**
     * 配置文件发生变更的解析器列表
     */
    @JvmStatic
    private val configChangeParserList = ArrayList<ConfigChangeParser>()

    init {
        // 先使用SPI机制去加载所有的解析器...
        val loader = ServiceLoader.load(ConfigChangeParser::class.java)
        loader.iterator().forEach(configChangeParserList::add)

        // 添加一个处理Properties配置文件的变更的解析器...
        configChangeParserList += PropertiesChangeParser()
    }

    /**
     * 解析配置文件的变更情况
     *
     * @param oldContent oldContent
     * @param newContent newContent
     * @param type type
     */
    @JvmStatic
    fun parseChangeData(oldContent: String?, newContent: String?, type: String): Map<String, ConfigChangeItem> {
        configChangeParserList.forEach {
            if (it.isResponsibleFor(type)) {
                return it.doParse(oldContent, newContent, type)
            }
        }
        return emptyMap()
    }
}