package com.wanna.cloud.openfeign

/**
 * FeignUtils
 */
object FeignUtils {

    /**
     * 添加模板参数, 给定一个原始(original)的Collection<String>, 将paramName作为模板参数加入到Collection<String>当中去进行返回值
     *
     * Note: 模板参数-Feign当中的模板参数就是"{paramName}"的形式
     *
     * @param params 原始的模板参数列表(可以为null)
     * @param paramName 想要新添加的模板参数名
     * @return 重新构建好的模板参数列表
     */
    @JvmStatic
    fun addTemplateParameter(params: Collection<String>?, paramName: String): Collection<String> {
        val result = ArrayList<String>()
        params?.let {
            result.addAll(it)
        }
        result.add("{$paramName}")
        return result
    }
}