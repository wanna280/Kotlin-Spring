package com.wanna.nacos.client.config.impl

import com.wanna.nacos.api.PropertyKeyConst
import com.wanna.nacos.api.utils.IPUtils
import java.io.Closeable
import java.util.*
import kotlin.collections.ArrayList

/**
 * Nacos的ServerList的管理器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/19
 */
class ServerListManager(fixed: List<String>) : Closeable {

    /**
     * contextPath
     */
    var contextPath: String = ""

    /**
     * 根据给定的Properties当中的配置信息去进行初始化
     *
     * @param properties 关于NacosConfigClient的Properties配置信息
     */
    constructor(properties: Properties) : this(properties[PropertyKeyConst.SERVER_ADDR].toString().split(","))

    /**
     * Nacos ConfigServer List
     */
    @Volatile
    private var serverList: List<String> = ArrayList()

    /**
     * current ServerAddr
     */
    var currentServerAddr: String = ""

    init {
        val serverList = ArrayList<String>()
        fixed.forEach {
            val ipAndPort = it.split(IPUtils.IP_PORT_SEPARATOR)

            if (ipAndPort.size == 1) {
                serverList += ipAndPort + IPUtils.IP_PORT_SEPARATOR + "9966"
            } else {
                serverList += ipAndPort[0] + IPUtils.IP_PORT_SEPARATOR + ipAndPort[1]
            }
        }
        this.serverList = serverList
    }

    /**
     * 获取下一个ServerAddr
     *
     * @return nextServerAddr
     */
    fun getNextServerAddr(): String {
        return serverList[0]
    }

    @Synchronized
    fun start() {
        this.currentServerAddr = "http://" + serverList[0]
    }


    override fun close() {

    }
}