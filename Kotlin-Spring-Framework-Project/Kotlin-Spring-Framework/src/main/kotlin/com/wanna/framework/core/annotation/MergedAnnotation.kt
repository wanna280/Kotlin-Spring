package com.wanna.framework.core.annotation

/**
 * 一个被合成的注解
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/21
 *
 * @param A 注解类型
 */
interface MergedAnnotation<A : Annotation> {
    companion object {
        const val VALUE: String = "value"
    }

    /**
     * 获取真正的注解类型
     *
     * @return 注解类型
     */
    fun getType(): Class<A>
}