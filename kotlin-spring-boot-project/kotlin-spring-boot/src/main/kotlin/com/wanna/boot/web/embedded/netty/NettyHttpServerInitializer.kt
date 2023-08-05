package com.wanna.boot.web.embedded.netty

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.stream.ChunkedWriteHandler

/**
 * 这是NettyHttpSever的初始化器, 负责给容器当中去注册Http报文的编解码器以及处理本次请求的Handler
 *
 * @see ChannelInitializer
 * @see HttpServerCodec
 * @see HttpObjectAggregator
 * @see ChunkedWriteHandler
 */
open class NettyHttpServerInitializer(private val handler: ChannelHandler) : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
        val pipeline = ch.pipeline()
        pipeline.addLast(HttpServerCodec())  // 添加HttpServer的编解码器
        pipeline.addLast(HttpObjectAggregator(128 * 1024))
        pipeline.addLast(ChunkedWriteHandler())
        pipeline.addLast(handler)
    }
}