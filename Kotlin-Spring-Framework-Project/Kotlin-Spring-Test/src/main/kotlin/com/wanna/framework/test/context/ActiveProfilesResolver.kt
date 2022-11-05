package com.wanna.framework.test.context

/**
 * ActiveProfiles的解析器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
interface ActiveProfilesResolver {

    /**
     * 根据testClass去进行自定义的profiles的解析
     *
     * @param testClass testClass
     * @return profiles
     */
    fun resolveProfile(testClass: Class<*>): Array<String>
}