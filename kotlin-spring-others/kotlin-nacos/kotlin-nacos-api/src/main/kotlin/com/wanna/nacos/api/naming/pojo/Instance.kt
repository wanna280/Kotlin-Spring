package com.wanna.nacos.api.naming.pojo

import com.wanna.framework.util.StringUtils
import com.wanna.nacos.api.common.Constants
import com.wanna.nacos.api.naming.ValidateBase

/**
 * Nacos实例
 */
open class Instance : java.io.Serializable, ValidateBase {
    var instanceId: String = ""  // instanceId
    var ip: String = ""  // ip
    var port: Int = 0  // port
    var enabled: Boolean = true  // 是否能处理请求？
    var ephemeral: Boolean = true // 是否是临时节点？
    var weight: Double = 1.0  // 权值
    var clusterName: String = Constants.DEFAULT_CLUSTER_NAME  // clusterName
    var serviceName: String = ""  // serviceName
    val metadata = HashMap<String, String>()  // metadata
    var healthy: Boolean = true  // 该实例是否是健康的？

    /**
     * 检测Instance当中的属性是否合法
     */
    override fun validate() {
        if (!StringUtils.hasText(ip)) {
            throw IllegalStateException("ip必须被初始化")
        }
        if (port == 0) {
            throw IllegalStateException("port必须被初始化")
        }
        if (!StringUtils.hasText(serviceName)) {
            throw IllegalStateException("serviceName必须被初始化")
        }
        if (!StringUtils.hasText(clusterName)) {
            throw IllegalStateException("clusterName必须被初始化")
        }
    }

    open fun toIpAddress(): String {
        return "$ip:$port"
    }

    override fun toString(): String {
        return "Instance(ip=$ip, port=$port, ephemeral=$ephemeral, clusterName=$clusterName, serviceName=$serviceName, healthy=$healthy)"
    }
}