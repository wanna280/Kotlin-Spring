package com.wanna.nacos.api.naming.pojo

/**
 * Nacos实例
 */
open class Instance : java.io.Serializable {
    var instanceId: String = ""  // instanceId
    lateinit var ip: String  // ip
    var port: Int = 0  // port
    var enabled: Boolean = true  // 是否能处理请求？
    var ephemeral: Boolean = true // 是否是临时节点？
    var weight: Double = 1.0  // 权值
    lateinit var clusterName: String  // clusterName
    lateinit var serviceName: String  // serviceName
    val metadata = HashMap<String, String>()  // metadata
    var healthy: Boolean = true  // 该实例是否是健康的？

    open fun toIpAddress(): String {
        return "$ip:$port"
    }

    override fun toString(): String {
        return "Instance(ip=$ip, port=$port, ephemeral=$ephemeral, clusterName=$clusterName, serviceName=$serviceName, healthy=$healthy)"
    }
}