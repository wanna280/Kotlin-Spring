package com.wanna.nacos.naming.server.healthcheck

/**
 * 维护一个客户端心跳信息，对于客户端发送过来的心跳包，将会被解析成为一个ClientBeatInfo
 */
data class ClientBeatInfo(val clusterName: String, val ip: String, val port: Int)