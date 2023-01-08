package com.wanna.boot.origin

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/7
 */
interface Origin {


    companion object {

        @JvmStatic
        fun from(source: Any): Origin? {
            if (source is Origin) {
                return source
            }
            if (source is OriginProvider) {
                return source.getOrigin()
            }
            if (source is Throwable) {
                return from(source)
            }
            return null
        }
    }
}