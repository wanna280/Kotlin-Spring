package com.wanna.framework.web.http.client.support

import com.wanna.framework.core.comparator.AnnotationAwareOrderComparator
import com.wanna.framework.web.http.client.ClientHttpRequestFactory
import com.wanna.framework.web.http.client.ClientHttpRequestInterceptor
import com.wanna.framework.web.http.client.InterceptingClientHttpRequestFactory
import com.wanna.framework.web.client.RestTemplate

/**
 * 带有拦截功能的HttpAccessor, 它内部聚合了拦截器列表, 支持去使用拦截器去拦截目标请求的执行, 不要直接使用, 具体使用见RestTemplate
 *
 * @see RestTemplate
 * @see HttpAccessor
 * @see ClientHttpRequestInterceptor
 */
open class InterceptingHttpAccessor : HttpAccessor() {

    // 客户端拦截器列表
    private val interceptors = ArrayList<ClientHttpRequestInterceptor>()

    // 带有拦截功能的ClientHttpRequestFactory
    private var interceptingRequestFactory: ClientHttpRequestFactory? = null

    /**
     * 设置拦截器列表, 支持自动使用比较器去进行拦截器列表的排序
     *
     * Note: 如果之前已经有拦截器了, 那么将会替换掉之前的; 如果想要去之前的基础上进行扩展, 需要先去进行get, 再去进行set
     *
     * @param interceptors 你想要使用的拦截器列表
     */
    open fun setInterceptors(interceptors: List<ClientHttpRequestInterceptor>) {
        if (this.interceptors != interceptors) {
            this.interceptors.clear()
            this.interceptors += interceptors
            AnnotationAwareOrderComparator.sort(this.interceptors)
        }
    }

    /**
     * 获取拦截器列表
     *
     * @return 拦截器列表
     */
    open fun getInterceptors(): List<ClientHttpRequestInterceptor> {
        return this.interceptors
    }

    /**
     * 在获取RequestFactory时, 如果必要的话, 使用带拦截功能的RequestFactory去进行包装一层
     *
     * @return 如果有拦截器的话, 返回InterceptingClientHttpRequestFactory; 否则, 返回普通的RequestFactory
     */
    override fun getRequestFactory(): ClientHttpRequestFactory {
        if (interceptors.isNotEmpty()) {
            var factory = this.interceptingRequestFactory
            if (factory == null) {
                // 创建一个支持拦截的RequestFactory, 去包装原来的RequestFactory
                factory = InterceptingClientHttpRequestFactory(super.getRequestFactory(), getInterceptors())
                this.interceptingRequestFactory = factory
            }
            return factory
        }
        return super.getRequestFactory()
    }
}