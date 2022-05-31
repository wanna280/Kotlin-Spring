package com.wanna.framework.web.http

import com.wanna.framework.util.LinkedMultiValueMap
import com.wanna.framework.util.MultiValueMap

/**
 * HttpHeaders
 */
class HttpHeaders : MultiValueMap<String, String> {
    companion object {
        /**
         * 客户端想要接收什么媒体类型的响应？比如"application/json"、"text/html"
         */
        const val ACCEPT = "Accept"

        /**
         * Http1.1当中Connection默认为"keep-alive"(长连接)，告诉对方在发送完成之后不用关闭TCP连接(设置为"false"时关闭长连接)
         * 但是由于WebServer和浏览器的众多的历史原因，这个字段一直被保留，也会被浏览器/WebServer所进行发送(比如Tomcat也会发送这个字段)
         */
        const val CONNECTION = "Connection"

        const val CONTENT_TYPE = "Content-Type"

        const val HOST = "Host"

        const val USER_AGENT = "UserAgent"

        const val UPGRADE = "Upgrade"

        /**
         * addHeader，"Transfer-Encoding=chucked"，标识将数据去进行分块传输
         * 在非长连接下，每次传输报文数据时，都需要进行TCP的三次握手和四次挥手，而四次挥手可以去标识着该报文已经结束，因此性能低下。
         * 正常情况下，如果是长连接的话，应该要告诉浏览器当前HTTP文档的结束位置在哪，一般情况下需要添加"Content-Length=xxx"这个Header
         * 但是这样意味着，服务器要对数据去进行长度的统计，再去进行发送，从而耗费了不必要的性能，因此才有了分块("chuck")传输
         * 分块传输的目的，就是告诉浏览器，当前的文档从哪里结束？
         * Note: 如果"Content-Length"太短，会导致文档被提前截断；"Content-Length"太长，会导致浏览器的pending(因为接收不到来自服务器的数据，所以一直等待)
         *
         * 下面是一个Demo的Http报文的格式：
         *
         * socket.write('HTTP/1.1 200 OK\r\n');   // request line
         * socket.write('Transfer-Encoding: chunked\r\n');
         * socket.write('\r\n');

         * socket.write('b\r\n');   // 告诉浏览器，当前的chuck长度为11(0xb)
         * socket.write('01234567890\r\n');

         * socket.write('5\r\n');  // 告诉浏览器，当前的chuck长度为5
         * socket.write('12345\r\n');

         * socket.write('0\r\n');  // 最后一个chuck，长度为0，告诉浏览器到这里文档结束
         * socket.write('\r\n');
         */
        const val TRANSFER_ENCODING = "Transfer-Encoding"
    }

    private val httpHeaders = LinkedMultiValueMap<String, String>()

    override fun getFirst(key: String): String? {
        return httpHeaders.getFirst(key)
    }

    override fun add(key: String, value: String) {
        return httpHeaders.add(key, value)
    }

    override fun addAll(key: String, values: List<String>) {
        return httpHeaders.addAll(key, values)
    }

    override fun set(key: String, value: String) {
        return httpHeaders.set(key, value)
    }

    override fun toSingleValueMap(): Map<String, String> {
        return httpHeaders.toSingleValueMap()
    }

    override val size: Int
        get() = httpHeaders.size

    override fun containsKey(key: String): Boolean {
        return httpHeaders.containsKey(key)
    }

    override fun containsValue(value: MutableList<String>): Boolean {
        return httpHeaders.containsValue(value)
    }

    override fun get(key: String) = httpHeaders[key]

    override fun isEmpty() = httpHeaders.isEmpty()

    override val entries: MutableSet<MutableMap.MutableEntry<String, MutableList<String>>>
        get() = httpHeaders.entries
    override val keys: MutableSet<String>
        get() = httpHeaders.keys
    override val values: MutableCollection<MutableList<String>>
        get() = httpHeaders.values

    override fun clear() {
        httpHeaders.clear()
    }

    override fun put(key: String, value: MutableList<String>): MutableList<String>? {
        return httpHeaders.put(key, value)
    }

    override fun putAll(from: Map<out String, MutableList<String>>) {
        httpHeaders.putAll(from)
    }

    override fun remove(key: String): MutableList<String>? {
        return httpHeaders.remove(key)
    }
}