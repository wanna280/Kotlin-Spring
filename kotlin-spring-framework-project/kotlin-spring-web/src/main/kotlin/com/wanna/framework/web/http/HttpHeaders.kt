package com.wanna.framework.web.http

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.LinkedMultiValueMap
import com.wanna.framework.util.MultiValueMap
import com.wanna.framework.util.StringUtils
import com.wanna.framework.web.bind.annotation.RequestMethod

/**
 * HttpHeaders
 */
open class HttpHeaders : MultiValueMap<String, String> {
    companion object {
        /**
         * 客户端想要接收什么媒体类型的响应? 比如"application/json"、"text/html"
         */
        const val ACCEPT = "Accept"

        /**
         * Http1.1当中Connection默认为"keep-alive"(长连接), 告诉对方在发送完成之后不用关闭TCP连接(设置为"false"时关闭长连接)
         * 但是由于WebServer和浏览器的众多的历史原因, 这个字段一直被保留, 也会被浏览器/WebServer所进行发送(比如Tomcat也会发送这个字段)
         */
        const val CONNECTION = "Connection"

        /**
         * 请求的响应类型, 比如"text/html","application/json"
         */
        const val CONTENT_TYPE = "Content-Type"

        /**
         * 报文的长度
         */
        const val CONTENT_LENGTH = "Content-Length"

        const val HOST = "Host"

        const val USER_AGENT = "UserAgent"

        const val UPGRADE = "Upgrade"

        const val VARY = "Vary"

        /**
         * Cookie字段
         */
        const val COOKIE = "Cookie"

        /**
         * Cookie2字段
         */
        const val COOKIE2 = "Cookie2"

        /**
         * Set-Cookie字段
         */
        const val SET_COOKIE = "Set-Cookie"

        /**
         * Set-Cookie2字段
         */
        const val SET_COOKIE2 = "Set-Cookie2"

        /**
         * KeepAlive字段
         */
        const val KEEP_ALIVE = "Keep-Alive"

        /**
         * Http请求源, 在CORS请求的, "PreFlight"(预检)请求当中需要携带请求的"Origin"
         */
        const val ORIGIN = "Origin"

        /**
         * 服务器端的CORS的访问控制允许的源(只有给定的这些源地址, 才能去对资源去进行访问, 用于去完成跨域的配置)
         */
        const val ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin"

        const val ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials"

        /**
         * 服务器端的CORS的访问控制允许的请求方式, 在匹配完客户端的请求方式之后, 会将其值写出给Response
         */
        const val ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods"

        /**
         * 服务器端的CORS的访问控制允许的Headers, 在匹配完客户端的请求方式之后, 会将其值写出给Response
         */
        const val ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers"

        /**
         * CORS的访问控制的最大存活时间(需要由服务器端去告诉浏览器, 多长时间内不用再次发送"PreFlight"预检请求)
         */
        const val ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age"

        /**
         * CORS的访问控制当中要暴露的Headers, 在CORS请求当中, 会将该Header写入到Response当中
         */
        const val ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers"

        /**
         * CORS的访问控制客户端请求时, "PreFlight"(请求)请求携带的Headers列表
         */
        const val ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers"

        /**
         * CORS的访问控制的客户端的请求方式(对于"PreFlight"请求当中, 请求方式为"OPTIONS",
         * 就需要在"Access-Control-Request-Method"这个请求头当中携带真实的CORS请求的真实请求方式)
         */
        const val ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method"

        /**
         * addHeader, "Transfer-Encoding=chucked", 标识将数据去进行分块传输
         * 在非长连接下, 每次传输报文数据时, 都需要进行TCP的三次握手和四次挥手, 而四次挥手可以去标识着该报文已经结束, 因此性能低下.
         * 正常情况下, 如果是长连接的话, 应该要告诉浏览器当前HTTP文档的结束位置在哪, 一般情况下需要添加"Content-Length=xxx"这个Header
         * 但是这样意味着, 服务器要对数据去进行长度的统计, 再去进行发送, 从而耗费了不必要的性能, 因此才有了分块("chuck")传输
         * 分块传输的目的, 就是告诉浏览器, 当前的文档从哪里结束?
         * Note: 如果"Content-Length"太短, 会导致文档被提前截断; "Content-Length"太长, 会导致浏览器的pending(因为接收不到来自服务器的数据, 所以一直等待)
         *
         * 下面是一个Demo的Http报文的格式：
         *
         * ```
         * socket.write('HTTP/1.1 200 OK\r\n');   // request line
         * socket.write('Transfer-Encoding: chunked\r\n');
         * socket.write('\r\n');
         * socket.write('b\r\n');   // 告诉浏览器, 当前的chuck长度为11(0xb)
         * socket.write('01234567890\r\n');
         * socket.write('5\r\n');  // 告诉浏览器, 当前的chuck长度为5
         * socket.write('12345\r\n');
         * socket.write('0\r\n');  // 最后一个chuck, 长度为0, 告诉浏览器到这里文档结束
         * socket.write('\r\n');
         * ```
         */
        const val TRANSFER_ENCODING = "Transfer-Encoding"
    }

    private val httpHeaders = LinkedMultiValueMap<String, String>()

    @Nullable
    override fun getFirst(key: String) = httpHeaders.getFirst(key)

    override fun add(key: String, value: String) = httpHeaders.add(key, value)

    override fun addAll(key: String, values: List<String>) = httpHeaders.addAll(key, values)

    override fun set(key: String, value: String) = httpHeaders.set(key, value)

    override fun toSingleValueMap() = httpHeaders.toSingleValueMap()

    override val size: Int
        get() = httpHeaders.size

    override fun containsKey(key: String) = httpHeaders.containsKey(key)

    override fun containsValue(value: MutableList<String>) = httpHeaders.containsValue(value)

    @Nullable
    override fun get(key: String) = httpHeaders[key]

    override fun isEmpty() = httpHeaders.isEmpty()

    override val entries: MutableSet<MutableMap.MutableEntry<String, MutableList<String>>>
        get() = httpHeaders.entries
    override val keys: MutableSet<String>
        get() = httpHeaders.keys
    override val values: MutableCollection<MutableList<String>>
        get() = httpHeaders.values

    override fun clear() = httpHeaders.clear()

    @Nullable
    override fun put(key: String, value: MutableList<String>): MutableList<String>? = httpHeaders.put(key, value)

    override fun putAll(from: Map<out String, MutableList<String>>) = httpHeaders.putAll(from)

    @Nullable
    override fun remove(key: String): MutableList<String>? = httpHeaders.remove(key)

    /**
     * 获取客户端的CORS的访问控制的请求头
     *
     * @return 请求的Header当中"Access-Control-Request-Headers"字段的值
     */
    open fun getAccessControlRequestHeaders(): List<String> {
        val value = getFirst(ACCESS_CONTROL_REQUEST_HEADERS) ?: return emptyList()
        return StringUtils.commaDelimitedListToStringArray(value, ",").toList()
    }

    /**
     * 设置访问控制的请求头
     *
     * @param headers 访问控制的请求头列表当中"Access-Control-Request-Headers"字段的值
     */
    open fun setAccessControlRequestHeaders(headers: List<String>) {
        set(ACCESS_CONTROL_REQUEST_HEADERS, headers.toMutableList())
    }

    /**
     * 获取浏览器的申请去进行访问控制的请求方式
     *
     * @return 如果header当中不存在"Access-Control-Request-Method", 那么return null; 如果存在, 那么return 该字段的值
     */
    @Nullable
    open fun getAccessControlRequestMethod(): RequestMethod? {
        val name = getFirst(ACCESS_CONTROL_REQUEST_METHOD) ?: return null
        return RequestMethod.forName(name)
    }

    /**
     * 设置访问控制的请求方式
     *
     * @param method 想要使用的访问控制的请求方式
     */
    open fun setAccessControlRequestMethod(method: RequestMethod) {
        set(ACCESS_CONTROL_REQUEST_METHOD, method.name)
    }

    /**
     * set allowCredentials
     *
     * @param allowCredentials
     */
    open fun setAccessControlAllowCredentials(allowCredentials: Boolean) {
        this.set(ACCESS_CONTROL_ALLOW_CREDENTIALS, allowCredentials.toString())
    }

    /**
     * get allowCredentials
     *
     * @return allowCredentials default to false
     */
    open fun getAccessControlAllowCredentials(): Boolean =
        getFirst(ACCESS_CONTROL_ALLOW_CREDENTIALS)?.toBoolean() ?: false

    /**
     * 设置访问控制要去进行暴露的Headers
     *
     * @param exposeHeaders 要去进行暴露的headers
     */
    open fun setAccessControlExposeHeaders(exposeHeaders: List<String>) {
        set(ACCESS_CONTROL_EXPOSE_HEADERS, exposeHeaders.toMutableList())
    }

    /**
     * 获取访问控制要去暴露的Headers列表
     *
     * @return 访问控制要去暴露的Headers
     */
    open fun getAccessControlExposeHeaders(): List<String> {
        val value = getFirst(ACCESS_CONTROL_EXPOSE_HEADERS) ?: return emptyList()
        return StringUtils.commaDelimitedListToStringArray(value).toList()
    }

    /**
     * 设置访问控制允许的Headers
     *
     * @param allowHeaders 允许的headers列表
     */
    open fun setAccessControlAllowHeaders(allowHeaders: List<String>) {
        set(ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders.toMutableList())
    }

    /**
     * 获取访问控制允许的Headers
     *
     * @return 允许的Headers列表
     */
    open fun getAccessControlAllowHeaders(): List<String> {
        val value = getFirst(ACCESS_CONTROL_ALLOW_HEADERS) ?: return emptyList()
        return StringUtils.commaDelimitedListToStringArray(value).toList()
    }


    /**
     * 访问控制允许的请求的请求方法("Access-Control-Allow-Methods")
     *
     * @param allowMethods 允许的请求方法
     */
    open fun setAccessControlAllowMethods(allowMethods: List<RequestMethod>) {
        set(ACCESS_CONTROL_ALLOW_METHODS, allowMethods.map { it.name }.toMutableList())
    }

    /**
     * 获取访问控制允许的请求方法("Access-Control-Allow-Methods")
     *
     * @return 访问控制允许的请求方式列表
     */
    open fun getAccessControlAllowMethods(): List<RequestMethod> {
        val value = getFirst(ACCESS_CONTROL_ALLOW_METHODS) ?: return emptyList()
        return StringUtils.commaDelimitedListToStringArray(value).map { RequestMethod.forName(it) }.toList()
    }

    /**
     * 设置访问控制允许的最大的存活时间
     *
     * @param maxAge 需要设置的"Access-Control-Max-Age"的值
     */
    open fun setAccessControlMaxAge(maxAge: Long) = this.set(ACCESS_CONTROL_MAX_AGE, maxAge.toString())

    /**
     * 获取访问控制允许的最大时间
     *
     * @return "Access-Control-Max-Age"的值, 如果没有, 那么return -1
     */
    open fun getAccessControlMaxAge(): Long {
        return getFirst(ACCESS_CONTROL_MAX_AGE)?.toLong() ?: -1
    }

    /**
     * 设置HttpHeader当中的访问控制允许的源(Origin)
     *
     * @param allowedOrigin 允许的Origin(为null代表删除)
     */
    open fun setAccessControlAllowOrigin(@Nullable allowedOrigin: String?) {
        if (allowedOrigin == null) {
            this.remove(ACCESS_CONTROL_ALLOW_ORIGIN)
        } else {
            this.set(ACCESS_CONTROL_ALLOW_ORIGIN, allowedOrigin)
        }
    }

    /**
     * HttpHeaders当中是否有包含"Access-Control-Allow-Origin"呢?
     *
     * @return 如果有包含""Access-Control-Allow-Origin"", return其值; 没有则return null
     */
    @Nullable
    open fun getAccessControllerAllowOrigin(): String? = getFieldValues(ACCESS_CONTROL_ALLOW_ORIGIN)

    /**
     * 获取给定的字段的值
     *
     * @param name name
     * @return 如果不存在的话, return null; 如果存在多个, 使用","去进行拼接
     */
    @Nullable
    protected open fun getFieldValues(name: String): String? {
        val origin = this[name] ?: return null
        return StringUtils.collectionToCommaDelimitedString(origin)
    }

    @Nullable
    open fun getOrigin(): String? = getFirst(ORIGIN)

    @Nullable
    open fun getHost(): String? = getFirst(HOST)

    /**
     * 获取ContentType
     *
     * @return ContentType for MediaType; 如果header当中没有的话, return null
     */
    @Nullable
    open fun getContentType(): MediaType? {
        return MediaType.parseMediaType(getFirst(CONTENT_TYPE) ?: return null)
    }

    /**
     * 设置ContentType
     *
     * @param contentType 想要使用的ContentType
     */
    open fun setContentType(contentType: MediaType) {
        this.set(CONTENT_TYPE, contentType.toString())
    }

    /**
     * 设置HTTP报文内容的长度
     *
     * @param contentLength 内容长度
     */
    open fun setContentLength(contentLength: Long) {
        this.set(CONTENT_LENGTH, contentLength.toString())
    }

    /**
     * 获取HTTP报文内容的长度
     *
     * @return contentLength
     */
    open fun getContentLength(): Long = getFirst(CONTENT_LENGTH)?.toLong() ?: -1

    /**
     * toString
     */
    override fun toString(): String = this.httpHeaders.toString()
}