package com.wanna.framework.web.method

import com.wanna.framework.web.bind.RequestMethod

/**
 * 它封装了@RequestMapping注解当中的各个属性的信息
 */
class RequestMappingInfo {

    // @RequestMapping当中配置的支持的路径
    var paths: MutableList<String> = ArrayList()

    // @RequestMapping当中配置的参数
    var params: MutableList<String> = ArrayList()

    // 支持的方法列表，默认为全部
    var methods: MutableList<RequestMethod> = mutableListOf(
        RequestMethod.GET,
        RequestMethod.POST,
        RequestMethod.PUT,
        RequestMethod.DELETE,
        RequestMethod.PATCH,
        RequestMethod.OPTIONS,
        RequestMethod.TRACE
    )

    /**
     * Builder，方便更方便地去构建RequestMappingInfo
     */
    class Builder {
        private val requestMappingInfo = RequestMappingInfo()

        fun paths(vararg paths: String): Builder {
            requestMappingInfo.paths = arrayListOf(*paths)
            return this
        }

        fun params(vararg params: String): Builder {
            requestMappingInfo.params = arrayListOf(*params)
            return this
        }

        fun methods(vararg methods: RequestMethod): Builder {
            requestMappingInfo.methods = arrayListOf(*methods)
            return this
        }

        fun build(): RequestMappingInfo {
            return this.requestMappingInfo
        }
    }
}