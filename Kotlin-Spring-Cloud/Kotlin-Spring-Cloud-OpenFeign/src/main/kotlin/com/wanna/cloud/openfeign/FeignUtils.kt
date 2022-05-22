package com.wanna.cloud.openfeign

/**
 * FeignUtils
 */
object FeignUtils {

    /**
     * 添加模板参数
     */
    @JvmStatic
    fun addTemplateParameter(params: Collection<String>?, paramName: String): Collection<String> {
        val result = ArrayList<String>()
        if (params != null) {
            result.addAll(params)
        }
        result.add("{$paramName}")
        return result
    }
}