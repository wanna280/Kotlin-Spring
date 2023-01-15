package com.wanna.cloud.nacos.ribbon

import com.alibaba.nacos.api.naming.pojo.Instance
import com.netflix.loadbalancer.Server

/**
 * 这是一个Nacos的Server, 用来实现Ribbon的Server
 */
open class NacosServer(private val instance: Instance) : Server(instance.ip, instance.port) {

}