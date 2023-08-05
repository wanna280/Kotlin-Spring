package com.wanna.framework.test.context.support

import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.test.context.ActiveProfiles
import com.wanna.framework.test.context.ActiveProfilesResolver
import com.wanna.framework.beans.BeanUtils
import com.wanna.framework.util.StringUtils

/**
 * ActiveProfiles的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
object ActiveProfilesUtils {

    /**
     * 从testClass上去解析[ActiveProfiles]注解, 从而解析到所有的需要使用的profiles
     *
     * @return 解析到的activeProfiles列表
     */
    @JvmStatic
    fun resolveActiveProfiles(testClass: Class<*>): Array<String> {
        val profileArrays = ArrayList<Array<String>>()

        val activeProfiles =
            AnnotatedElementUtils.getMergedAnnotation(testClass, ActiveProfiles::class.java) ?: return emptyArray()
        val profiles = activeProfiles.profiles + activeProfiles.value
        profileArrays.add(profiles)
        val resolver = BeanUtils.instantiateClass(activeProfiles.resolver.java, ActiveProfilesResolver::class.java)
        profileArrays.add(resolver.resolveProfile(testClass))

        val profilesResult = ArrayList<String>()
        profileArrays.forEach { arr ->
            arr.forEach {
                if (StringUtils.hasText(it)) {
                    profilesResult += it
                }
            }
        }
        return profilesResult.toTypedArray()
    }
}