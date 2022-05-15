package com.wanna.framework.core.type

/**
 * 这是维护一个类的元信息
 */
interface ClassMetadata {
    fun getClassName() : String

    fun getPackageName() : String

    fun isInterface() : Boolean
}