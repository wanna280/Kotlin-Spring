package com.wanna.framework.web.server.netty.server.support

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.core.util.StringUtils
import com.wanna.framework.web.DispatcherHandler
import com.wanna.framework.web.bind.RequestMethod
import com.wanna.framework.web.http.HttpHeaders
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerRequestImpl
import com.wanna.framework.web.server.HttpServerResponse
import com.wanna.framework.web.server.HttpServerResponseImpl
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
open class NettyServerHandler(applicationContext: ApplicationContext) : ChannelInboundHandlerAdapter() {
    private val dispatcherHandler = applicationContext.getBean(DispatcherHandler::class.java)

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is FullHttpRequest) {
            // 解析请求，将netty的FullHttpRequest转为HttpServerRequest
            val request = parseRequest(msg)
            val response = HttpServerResponseImpl()

            // 处理本次HTTP请求
            dispatcherHandler.doDispatch(request, response)

            // 给客户端去返回响应数据
            sendResponse(response, ctx)
        }
    }

    /**
     * 将响应的HttpServerResponse构建成为Netty的Response并去进行写出给客户端
     *
     * @param response HttpServerResponse
     * @param ctx ChannelHandlerContext
     */
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

        // setContentType, default for "application/json"
        httpResponse.headers()[HttpHeaders.CONTENT_TYPE] = response.getContentType()

        // Http1.1当中Connection默认为"keep-alive"(长连接)，告诉对方在发送完成之后不用关闭TCP连接(设置为"false"时关闭长连接)
        // 但是由于WebServer和浏览器的众多的历史原因，这个字段一直被保留，也会被浏览器/WebServer所进行发送(比如Tomcat也会发送这个字段)
        httpResponse.headers()[HttpHeaders.CONNECTION] = "keep-alive"

        // addHeader，"Transfer-Encoding=chucked"，标识将数据去进行分块传输
        httpResponse.headers()[HttpHeaders.TRANSFER_ENCODING] = "chunked"

        // write And Flush，将要Http响应报文数据写出给客户端...
        ctx.writeAndFlush(httpResponse)
    }

    /**
     * 从Netty的request当中去解析成为HttpServerRequest
     *
     * @param msg Netty的FullHttpRequest
     * @return 解析完成的HttpServerResponse
     */
    private fun parseRequest(msg: FullHttpRequest): HttpServerRequest {
        val request = HttpServerRequestImpl()
        // 初始化request的相关信息
        request.init {
            // 解析uri和url
            parseUriUrlAndParams(msg.uri())

            // 设置RequestMethod
            setMethod(RequestMethod.forName(msg.method().name()))

            // 解析HttpHeader
            val iterator = msg.headers().iteratorAsString()
            while (iterator.hasNext()) {
                val (n, v) = iterator.next()
                addHeader(n, v)
            }

            // 将RequestBody当中的内容包装成为InputStream
            val content = msg.content()
            val byteArray = ByteArray(content.readableBytes())
            content.readBytes(byteArray)
            setInputStream(ByteArrayInputStream(byteArray))
        }
        return request
    }
}