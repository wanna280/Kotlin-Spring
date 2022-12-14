package com.wanna.boot.web.embedded.netty

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.util.StringUtils
import com.wanna.framework.web.DispatcherHandler
import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.http.DefaultCookieCodec
import com.wanna.framework.web.http.HttpHeaders
import com.wanna.framework.web.server.ActionCode
import com.wanna.framework.web.server.ActionHook
import com.wanna.framework.web.server.HttpServerRequestImpl
import com.wanna.framework.web.server.HttpServerResponseImpl
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.InetSocketAddress

/**
 * NettyServerHandler, 将Netty的Message去转换成为[HttpServerRequestImpl]和[HttpServerResponseImpl]对象,
 * 交给[DispatcherHandler]去进行请求的处理
 *
 * @param applicationContext ApplicationContext
 */
@Sharable
open class NettyServerHandler(applicationContext: ApplicationContext) : ChannelInboundHandlerAdapter() {
    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(NettyServerHandler::class.java)

        /**
         * path当中的多个参数的分隔符
         */
        const val PARAM_SEPARATOR = "&"

        /**
         * path当中的参数的K-V分隔符
         */
        const val EQUAL = "="
    }

    /**
     * DispatcherHandler
     */
    private val dispatcherHandler = applicationContext.getBean(DispatcherHandler::class.java)

    /**
     * Cookie的编解码器
     */
    private val cookieCodec = DefaultCookieCodec()

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is FullHttpRequest) {
            // 1.创建request和response
            val request = HttpServerRequestImpl()
            val response = HttpServerResponseImpl()

            // 2.初始化request和response
            initRequest(request, response, msg, ctx)
            initResponse(request, response, ctx)

            // 3.交给DispatcherHandler去处理本次HTTP请求
            dispatcherHandler.doDispatch(request, response)
            // fixed: not to flush，对于所有的flush操作，全部交给使用方去进行flush
            // response.flush()
        }
    }

    /**
     * 当处理请求的过程当中, 发生了异常的情况
     *
     * @param ctx ChannelHandlerContext
     * @param cause 发生的异常
     */
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if (cause is IOException) {
            // ignore IOException
            logger.error("Netty WebServer handle Request Error", cause)
        } else {
            ctx.fireExceptionCaught(cause)
        }
    }

    /**
     * 初始化Response，设置FlushCallback，在response调用flush时，就可以将数据写入给客户端了
     *
     * @param request request
     * @param response response
     * @param ctx ChannelHandlerContext
     */
    private fun initResponse(
        request: HttpServerRequestImpl,
        response: HttpServerResponseImpl,
        ctx: ChannelHandlerContext
    ) {
        response.initFlushCallback {
            val responseByteBuf = Unpooled.copiedBuffer(getOutputStream().toByteArray())

            // 构建响应状态码
            val responseStatus = if (StringUtils.hasText(getMessage())) HttpResponseStatus.valueOf(
                getStatusCode(), getMessage()
            ) else HttpResponseStatus.valueOf(getStatusCode())

            val httpResponse = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus, responseByteBuf)

            // 添加header
            getHeaders().forEach(httpResponse.headers()::add)

            // setContentType, default for "application/json"
            httpResponse.headers()[HttpHeaders.CONTENT_TYPE] = getContentType()

            // Http1.1当中Connection默认为"keep-alive"(长连接)，告诉对方在发送完成之后不用关闭TCP连接(设置为"false"时关闭长连接)
            // 但是由于WebServer和浏览器的众多的历史原因，这个字段一直被保留，也会被浏览器/WebServer所进行发送(比如Tomcat也会发送这个字段)
            httpResponse.headers()[HttpHeaders.CONNECTION] = "keep-alive"

            // addHeader，"Transfer-Encoding=chucked"，标识将数据去进行分块传输
            httpResponse.headers()[HttpHeaders.TRANSFER_ENCODING] = "chunked"

            httpResponse.headers()[HttpHeaders.KEEP_ALIVE] = "timeout=60"

            // 添加Cookie
            val cookieHeader = cookieCodec.encodeAsHeader(response.getCookies())
            // 如果存在有Cookie的话，那么我们需要设置Header
            if (cookieHeader != null) {
                httpResponse.headers()[HttpHeaders.SET_COOKIE] = cookieHeader
            }


            // write And Flush，将要Http响应报文数据写出给客户端...
            ctx.writeAndFlush(httpResponse)
        }
    }

    /**
     * 从Netty的request当中去解析成为HttpServerRequest
     *
     * @param request request
     * @param response response
     * @param msg Netty的FullHttpRequest
     * @param context ChannelContext
     * @return 解析完成的HttpServerResponse
     */
    private fun initRequest(
        request: HttpServerRequestImpl,
        response: HttpServerResponseImpl,
        msg: FullHttpRequest,
        context: ChannelHandlerContext
    ) {
        // 初始化request的相关信息
        request.init {

            // 设置RequestMethod
            setMethod(RequestMethod.forName(msg.method().name()))

            // 解析HttpHeader
            val iterator = msg.headers().iteratorAsString()
            while (iterator.hasNext()) {
                val (name, value) = iterator.next()
                addHeader(name, value)
                if (name.equals(HttpHeaders.COOKIE, false)) {
                    setCookies(*cookieCodec.decodeAsCookie(value))
                }
            }
            val remoteAddress = context.channel().remoteAddress() as InetSocketAddress
            val localAddress = context.channel().localAddress() as InetSocketAddress

            // remote host and remote port
            setRemoteIp(remoteAddress.hostName)
            setRemotePort(remoteAddress.port)
            setRemoteHost(remoteAddress.hostName + ":" + remoteAddress.port)

            if (getHeaders().getHost() == null) {
                getHeaders().add(HttpHeaders.HOST, localAddress.hostName + ":" + localAddress.port)
            }

            // 解析uri和url
            parseUriUrlAndParams(this, msg.uri())

            // 将RequestBody当中的内容，包装成为InputStream设置到request当中
            val content = msg.content()
            val byteArray = ByteArray(content.readableBytes())
            content.readBytes(byteArray)
            setInputStream(ByteArrayInputStream(byteArray))

            // 设置ActionHook为重新使用DispatcherHandler去进行doDispatch
            setActionHook(object : ActionHook {
                override fun action(code: ActionCode, param: Any?) {
                    when (code) {

                        // 异步派发
                        ActionCode.ASYNC_DISPATCH -> context.channel().eventLoop().execute {
                            dispatcherHandler.doDispatch(request, response)
                        }

                        // 异步完成
                        ActionCode.ASYNC_COMPLETE -> response.flush()
                    }
                }
            })
        }
    }

    /**
     * 解析uri/url和params并存入到request当中去
     *
     * @param request request
     * @param originPath 原始的路径
     */
    private fun parseUriUrlAndParams(request: HttpServerRequestImpl, originPath: String) {
        val host = request.getHeaders().getHost() ?: ""
        // path格式参考"localhost:8080/servlet?id=1"
        val path = host + originPath

        val indexOf = path.indexOf("?")

        // 如果没有"?"的话, 那么没有query, 只有url这部分组成
        if (indexOf == -1) {
            // uri格式参考"/servlet"
            request.setUri(originPath)

            // url格式参考"localhost:8080/servlet"
            request.setUrl(path)
            return
        }
        // 如果有"?"的话, 那么我们就需要去进行解析了...

        // 把path当中的query部分去掉, 得到了url
        request.setUrl(path.substring(0, indexOf))
        // 把url当中的host去掉, 得到了uri
        request.setUri(path.substring(host.length, indexOf))

        // 把"?"之后的元素, 使用"&"去进行拆分成为一个个的params
        val params = path.substring(indexOf + 1).split(PARAM_SEPARATOR)

        // 对每个param, 按照"="去进行拆分
        params.forEach {
            val eqIndex = it.indexOf(EQUAL)
            if (eqIndex == -1) {
                throw IllegalStateException("无法从给定的param当中找到K-V的分隔符'=', param=$it")
            }
            // 拼接param
            request.addParam(it.substring(0, eqIndex), it.substring(eqIndex + 1))
        }
    }
}