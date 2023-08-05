package com.wanna.framework.web.cors

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.StringUtils
import com.wanna.framework.web.bind.annotation.RequestMethod
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * 维护CORS跨域配置的相关信息
 */
open class CorsConfiguration() {
    companion object {
        // 匹配所有请求的的常量
        const val ALL = "*"

        // All List
        val ALL_LIST = listOf(ALL)

        // 默认匹配的全部请求的常量
        val DEFAULT_PERMIT_ALL = listOf(ALL)

        // 默认允许的请求方式
        val DEFAULT_PERMIT_METHODS = listOf(RequestMethod.GET.name, RequestMethod.POST.name, RequestMethod.HEAD.name)

        // 默认情况下的resolvedMethods的值
        val DEFAULT_METHODS = listOf(RequestMethod.GET, RequestMethod.HEAD)

        // 默认的最大存活时间(单位为s, 1800L也就是min之内不必再次发送预检请求)
        const val DEFAULT_MAX_AGE = 1800L

        private val ALL_PATTERN = OriginPattern("*")

        private val ALL_PATTERN_LIST = listOf(ALL_PATTERN)
    }

    /**
     * 拷贝一份CorsConfiguration到一个新创建的CorsConfiguration当中
     *
     * @param other 别的CorsConfiguration
     */
    constructor(other: CorsConfiguration) : this() {
        allowedOrigins = other.allowedOrigins
        allowedOriginPatterns = other.allowedOriginPatterns
        allowedMethods = other.allowedMethods
        resolvedMethods = other.resolvedMethods
        allowedHeaders = other.allowedHeaders
        exposeHeaders = other.exposeHeaders
        allowCredentials = other.allowCredentials
        maxAge = other.maxAge
    }

    // 允许的Origin的正则表达式
    private var allowedOriginPatterns: List<OriginPattern>? = null

    // 允许的origin
    private var allowedOrigins: List<String>? = null

    // 允许的请求方式
    private var allowedMethods: List<String>? = null

    // 完成解析成为枚举值的请求方式(默认为"GET"/"HEAD")
    private var resolvedMethods: List<RequestMethod>? = DEFAULT_METHODS

    // 允许的HttpHeader, 预检"PreFlight"请求当中, 必须携带指定的"Header"才允许该CORS请求
    private var allowedHeaders: List<String>? = null

    // 对于CORS请求, 需要去暴露给客户端的Header列表
    private var exposeHeaders: List<String>? = null

    // Cors的最大存活时间, 超过这个时间, 那么必须重新去进行预检请求
    private var maxAge: Long? = null

    private var allowCredentials: Boolean? = null

    open fun setAllowedOrigins(allowedOrigins: List<String>) {
        this.allowedOrigins = allowedOrigins
    }

    open fun addAllowedOrigin(allowedOrigin: String) {
        val allowedOrigins = if (this.allowedOrigins == null) ArrayList() else ArrayList(this.allowedOrigins!!)
        allowedOrigins += allowedOrigin
        this.allowedOrigins = allowedOrigins
    }

    open fun setAllowedHeaders(allowedHeaders: List<String>) {
        this.allowedHeaders = allowedHeaders
    }

    open fun addAllowedHeader(allowHeader: String) {
        val allowedHeaders = if (this.allowedHeaders == null) ArrayList() else ArrayList(this.allowedHeaders!!)
        allowedHeaders += allowHeader
        this.allowedHeaders = allowedHeaders
    }

    /**
     * 设置CORS允许跨域的请求方式
     *
     * @param allowedMethods 允许去进行跨域的方式, 如果为null, 则设置为默认值
     */
    open fun setAllowedMethods(@Nullable allowedMethods: List<String>?) {
        this.allowedMethods = if (allowedMethods == null) null else ArrayList(allowedMethods)
        if (allowedMethods == null || allowedMethods.isEmpty()) {
            this.resolvedMethods = DEFAULT_METHODS
        } else {
            val resolvedMethods = ArrayList<RequestMethod>()
            allowedMethods.forEach {
                // 如果其中包含了一个ALL("*"), 那么直接将resolvedMethods设为null, 标识通配
                if (it == ALL) {
                    this.resolvedMethods = null
                    return
                } else {
                    resolvedMethods.add(RequestMethod.forName(it))
                }
            }
            this.resolvedMethods = resolvedMethods
        }
    }

    open fun addAllowedMethod(allowMethod: String) {
        val allowedMethods = if (this.allowedMethods == null) ArrayList() else ArrayList(this.allowedMethods!!)
        allowedMethods += allowMethod
        this.allowedMethods = allowedMethods
        if (allowMethod == ALL) {
            this.resolvedMethods = null
            return
        }
        // add ResolvedMethod
        val resolvedMethods = if (this.resolvedMethods == null) ArrayList() else ArrayList(this.resolvedMethods!!)
        resolvedMethods += RequestMethod.forName(allowMethod)
        this.resolvedMethods = resolvedMethods
    }

    open fun setAllowedOriginPatterns(allowedOriginPatterns: List<String>) {
        this.allowedOriginPatterns = allowedOriginPatterns.map { OriginPattern(it) }
    }

    open fun addAllowedOriginPattern(allowedOriginPattern: String) {
        val allowedOriginPatterns =
            if (this.allowedOriginPatterns == null) ArrayList() else ArrayList(this.allowedOriginPatterns!!)
        allowedOriginPatterns += OriginPattern(allowedOriginPattern)
        this.allowedOriginPatterns = allowedOriginPatterns
    }

    open fun setMaxAge(maxAge: Long) {
        this.maxAge = maxAge
    }

    @Nullable
    open fun getExposeHeaders(): List<String>? = this.exposeHeaders

    open fun setExposeHeaders(exposeHeaders: List<String>) {
        this.exposeHeaders = exposeHeaders
    }

    open fun addExposeHeader(exposeHeader: String) {
        val exposeHeaders = if (this.exposeHeaders == null) ArrayList() else ArrayList(this.exposeHeaders!!)
        exposeHeaders += exposeHeader
        this.exposeHeaders = exposeHeaders
    }

    @Nullable
    open fun getAllowCredentials(): Boolean? = this.allowCredentials

    open fun setAllowCredentials(allowCredentials: Boolean) {
        this.allowCredentials = allowCredentials
    }

    /**
     * 获取Cors的存活时间
     *
     * @return 如果没有配置, 那么return null; 如果配置了, 返回指定的Cors的存活时间
     */
    open fun getMaxAge(): Long? = maxAge

    /**
     * 将所有你没有去进行配置过的CORS的配置信息, 全部配置为默认值
     *
     * @return this
     */
    open fun applyPermitDefaultValues(): CorsConfiguration {
        // 1.如果没有指定allowOrigin和allowOriginPattern, 那么设置为permitAll("*")
        if (this.allowedOrigins == null && this.allowedOriginPatterns == null) {
            this.allowedOrigins = DEFAULT_PERMIT_ALL
        }

        // 2.如果没有设置允许的请求方式, 那么设置为默认的允许请求方式("GET", "POST", "HEAD")
        if (this.allowedMethods == null) {
            this.allowedMethods = DEFAULT_PERMIT_METHODS
            this.resolvedMethods = DEFAULT_PERMIT_METHODS.map(RequestMethod.Companion::forName).toList()
        }

        // 3.如果没有配置允许的请求头的话, 那么设置为permitAll("*")
        if (this.allowedHeaders == null) {
            this.allowedMethods = DEFAULT_PERMIT_ALL
        }

        // 4.如果没有设置Cors的默认存活时间, 那么设置为30m(1800s)
        if (this.maxAge == null) {
            this.maxAge = DEFAULT_MAX_AGE
        }
        return this
    }

    /**
     * 检查Origin情况
     *
     * @param requestOrigin RequestOrigin
     * @return 如果给定的origin合法, 返回匹配的Origin情况; 如果不合法, return null
     */
    @Nullable
    open fun checkOrigin(@Nullable requestOrigin: String?): String? {
        // 如果都没有requestOrigin的话, return null
        if (!StringUtils.hasText(requestOrigin)) {
            return null
        }

        // 遍历所有的允许的originPattern, 挨个去进行匹配
        val origins = this.allowedOrigins
        if (origins != null && origins.isNotEmpty()) {
            // 如果存在有ALL("*"), 那么通配所有的Origin, 直接return ALL
            if (origins.contains(ALL)) {
                return ALL
            }
            // 如果没有ALL, 那么挨个尝试去进行匹配
            origins.forEach {
                if (it.equals(requestOrigin, true)) {
                    return requestOrigin
                }
            }
        }
        // 检查allowedOriginPatterns...挨个去进行匹配, 只要匹配了, 那么return origin
        this.allowedOriginPatterns?.forEach {
            if (it.declaredPattern == ALL || it.pattern.asPredicate().test(requestOrigin)) {
                return requestOrigin
            }
        }
        return null
    }

    /**
     * 检查HttpMethod是否合法：
     * * 1.给定的methods==null, return null, 说明该CORS请求不合法
     * * 2.如果resolvedMethods==null(如果不去进行过滤请求方式, 匹配所有的请求方式), return listOf method
     * * 3.如果resolvedMethods!=null, 那么就得匹配请求方式是否合法
     *
     * @return 匹配的requestMethods列表
     */
    @Nullable
    open fun checkHttpMethods(@Nullable method: RequestMethod?): List<RequestMethod>? {
        method ?: return null

        // 如果匹配所有的请求方式, 那么你来什么, 我给你什么
        if (this.resolvedMethods == null) {
            return listOf(method)
        }

        // 如果不是匹配所有的请求方式(指定了应该拥有的请求方式), 那么需要去判断当前的RequestMethod是否允许CROS
        return if (this.resolvedMethods!!.contains(method)) this.resolvedMethods else null
    }

    /**
     * 检查你给出的所有的HttpHeaders的列表, 判断你给出的所有的HttpHeader, 在CROS请求当中, 我作为服务端是否允许?
     * 并返回浏览器, 我服务端允许了你给出的哪些RequestHeader? 如果一个都不允许(return null), 说明本次CORS请求不合法,
     * 因为你连我要求的一个Header都没给出, 你必须给出其中一个我允许的Header, 才能通过本次请求
     *
     * @param requestHeaders requestHeaders(PreFlight请求的"Access-Control-Request-Headers", 或者是Http请求当中的Headers)
     * @return 如果你给的RequestHeader, 我一个都不允许, 那么return null; 只要允许了其中一些, 那么return 允许的Header列表
     */
    @Nullable
    open fun checkHttpHeaders(@Nullable requestHeaders: List<String>?): List<String>? {
        requestHeaders ?: return null
        if (requestHeaders.isEmpty()) {
            return emptyList()
        }
        if (this.allowedHeaders == null || this.allowedHeaders!!.isEmpty()) {
            return null
        }

        // 先统计出判断是否是允许所有的Header?
        val allowAnyHeader = this.allowedHeaders!!.contains(ALL)

        val result = ArrayList<String>(requestHeaders.size)

        // 匹配客户端给的所有请求, 看看服务器端允不允许Header?
        requestHeaders.forEach {
            val requestHeader = it.trim()
            if (allowAnyHeader) {
                result += requestHeader
            } else {
                this.allowedHeaders!!.forEach { allowHeader ->
                    if (requestHeader.equals(allowHeader, true)) {
                        result += requestHeader
                    }
                }
            }
        }

        // 如果一个都没匹配, return null, 如果匹配了其中一个, 那么return 匹配的列表
        return if (result.isEmpty()) null else result
    }


    /**
     * 联合别的CorsConfiguration到一个新的CorsConfiguration当中
     *
     * @param other 别的CorsConfiguration
     * @return 将两个CorsConfiguration联合完成的新的CorsConfiguration
     */
    open fun combine(@Nullable other: CorsConfiguration?): CorsConfiguration {
        other ?: return this
        val config = CorsConfiguration(this)
        val origins = combine(allowedOrigins, allowedOrigins)
        val patterns = combinePatterns(allowedOriginPatterns, other.allowedOriginPatterns)
        config.allowedOrigins =
            if (origins === DEFAULT_PERMIT_ALL && patterns.isNotEmpty()) null else origins
        config.allowedOriginPatterns = patterns
        config.setAllowedMethods(combine(allowedMethods, other.allowedMethods))
        config.setAllowedHeaders(combine(allowedHeaders, other.allowedHeaders))
        config.setExposeHeaders(combine(exposeHeaders, other.exposeHeaders))
        val allowCredentials = other.getAllowCredentials()
        if (allowCredentials != null) {
            config.setAllowCredentials(allowCredentials)
        }
        val maxAge = other.getMaxAge()
        if (maxAge != null) {
            config.setMaxAge(maxAge)
        }
        return config
    }


    /**
     * combine
     */
    private fun combine(@Nullable source: List<String>?, @Nullable other: List<String>?): List<String> {
        if (other == null) {
            return source ?: emptyList()
        }
        if (source == null) {
            return other
        }
        if (source === DEFAULT_PERMIT_ALL || source === DEFAULT_PERMIT_METHODS) {
            return other
        }
        if (other === DEFAULT_PERMIT_ALL || other === DEFAULT_PERMIT_METHODS) {
            return source
        }
        if (source.contains(ALL) || other.contains(ALL)) {
            return ALL_LIST
        }
        val combined: MutableSet<String> = LinkedHashSet(source.size + other.size)
        combined.addAll(source)
        combined.addAll(other)
        return combined.toList()
    }

    private fun combinePatterns(
        source: List<OriginPattern>?,
        other: List<OriginPattern>?
    ): List<OriginPattern> {
        if (other == null) {
            return source ?: emptyList()
        }
        if (source == null) {
            return other
        }
        if (source.contains(CorsConfiguration.ALL_PATTERN) || other.contains(ALL_PATTERN)) {
            return CorsConfiguration.ALL_PATTERN_LIST
        }
        val combined: MutableSet<OriginPattern> = LinkedHashSet(source.size + other.size)
        combined.addAll(source)
        combined.addAll(other)
        return combined.toList()
    }

    /**
     * 匹配Origin的Pattern
     *
     * @param declaredPattern 原始的pattern
     */
    private class OriginPattern(val declaredPattern: String) {
        companion object {
            private val PATTERN = Pattern.compile("(.*):\\[(\\*|\\d+(,\\d+)*)]")
        }

        // 正则表达式的Pattern
        val pattern: Pattern = initPattern(declaredPattern)

        /**
         * 初始化Pattern
         *
         * @param patternVal pattern
         */
        private fun initPattern(patternVal: String): Pattern {
            var patternValue = patternVal
            var portList: String? = null
            val matcher: Matcher = PATTERN.matcher(patternValue)
            if (matcher.matches()) {
                patternValue = matcher.group(1)
                portList = matcher.group(2)
            }

            patternValue = "\\Q$patternValue\\E"
            patternValue = patternValue.replace("*", "\\E.*\\Q")

            if (portList != null) {
                patternValue += if (portList == ALL) "(:\\d+)?" else ":(" + portList.replace(',', '|') + ")"
            }

            return Pattern.compile(patternValue)
        }
    }
}