package com.wanna.boot.autoconfigure.web.mvc

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.core.util.StringUtils
import com.wanna.framework.web.DispatcherHandler
import com.wanna.framework.web.bind.RequestMethod
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@Sharable
class NettyServerHandler(private val applicationContext: ApplicationContext) : ChannelInboundHandlerAdapter() {
    private val dispatcherHandler = applicationContext.getBean(DispatcherHandler::class.java)

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (dispatcherHandler != null && msg is FullHttpRequest) {
            // 解析请求，将netty的FullHttpRequest转为HttpServerRequest
            val request = parseRequest(msg)
            val response = HttpServerResponse()

            // 处理本次HTTP请求
            dispatcherHandler.doDispatch(request, response)

            // 给客户端去返回响应数据
            sendResponse(response, ctx)
        }
    }

    private fun sendResponse(response: HttpServerResponse, ctx: ChannelHandlerContext) {
        val outputStream = response.getOutputStream()
        val responseByteBuf = Unpooled.copiedBuffer((outputStream as ByteArrayOutputStream).toByteArray())

        // 构建响应状态码
        val responseStatus = if (StringUtils.hasText(response.getMessage())) HttpResponseStatus.valueOf(
            response.getStatusCode(), response.getMessage()
        ) else HttpResponseStatus.valueOf(response.getStatusCode())

        val httpResponse = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus, responseByteBuf)

        // 添加header
        response.getHeaders().forEach(httpResponse.headers()::add)

        httpResponse.headers()["Content-Type"] = response.getContentType()

        // write And Flush
        ctx.writeAndFlush(httpResponse)
    }

    private fun parseRequest(msg: FullHttpRequest): HttpServerRequest {
        val request = HttpServerRequest()

        // 设置uri和method
        request.setUri(msg.uri())
        request.setMethod(RequestMethod.forName(msg.method().name()))

        // 解析header
        val iterator = msg.headers().iteratorAsString()
        while (iterator.hasNext()) {
            val (n, v) = iterator.next()
            request.addHeader(n, v)
        }

        val content = msg.content()
        val size = content.readableBytes()
        val byteArray = ByteArray(size)
        content.readBytes(byteArray)

        // 将request当中的内容包装成为InputStream
        request.setInputStream(ByteArrayInputStream(byteArray))
        return request
    }
}