package com.wanna.framework.core.type

/**
 * 这是维护一个类的元信息
 */
interface ClassMetadata {

    /**
     * 获取类名
     *
     * @return className
     */
    fun getClassName() : String

    /**
     * 获取该类的包名
     *
     * @return packageName
     */
    fun getPackageName() : String

    /**
     * 判断该类是否是一个接口
     *
     * @return 如果是一个接口，return true；否则return false
     */
    fun isInterface() : Boolean
}