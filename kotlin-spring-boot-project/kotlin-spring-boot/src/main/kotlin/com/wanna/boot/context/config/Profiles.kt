package com.wanna.boot.context.config

import com.wanna.boot.context.properties.bind.Bindable
import com.wanna.boot.context.properties.bind.Binder
import com.wanna.boot.context.properties.source.ConfigurationPropertyName
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.core.environment.AbstractEnvironment
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.LinkedMultiValueMap
import com.wanna.framework.util.MultiValueMap
import com.wanna.framework.util.StringUtils
import java.util.*
import java.util.function.Function
import kotlin.collections.LinkedHashSet

/**
 * 提供对于SpringBoot应用当中的Profiles的计算
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 *
 * @param environment Environment
 * @param binder Binder
 * @param additionalProfiles 额外要去进行添加的activeProfiles
 */
open class Profiles(
    environment: Environment,
    binder: Binder,
    additionalProfiles: Collection<String>
) : Iterable<String> {
    companion object {
        /**
         * 需要额外去进行导入的Profiles的属性名, 通过这个属性值可以去是实现额外导入Profiles的功能
         */
        const val INCLUDE_PROFILES_PROPERTY_NAME = "spring.profiles.include"

        /**
         * Profiles分组的属性名
         */
        private const val PROFILES_GROUP_PROPERTY_NAME = "spring.profiles.group"

        /**
         * "spring.profiles.include"构建的ConfigurationPropertyName
         */
        @JvmStatic
        private val INCLUDE_PROFILES = ConfigurationPropertyName.of(INCLUDE_PROFILES_PROPERTY_NAME)

        /**
         * MultiValueMap<String, String>的Bindable
         */
        @JvmStatic
        private val STRING_STRINGS_MAP: Bindable<MultiValueMap<String, String>> =
            Bindable.of(
                ResolvableType.forClassWithGenerics(
                    MultiValueMap::class.java,
                    String::class.java,
                    String::class.java
                )
            )

        /**
         * Set<String>的Bindable
         */
        @JvmStatic
        private val STRING_SET: Bindable<Set<String>> = Bindable.setOf(String::class.java)
    }

    /**
     * 获取到"spring.profiles.group"当中配置的group信息;
     * 如果配置了这个属性的话, 那么profiles的配置("spring.profiles.active"/"spring.profiles.default")将会支持以分组的方式去进行导入;
     * 例如"spring.profiles.group.prod=prod1,prod2,prod3", "spring.profiles.active=prod"将会最终应用"prod,prod1,prod2,prod3"这些profiles
     *
     * @see AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME
     * @see AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME
     */
    private val groups = binder.bind(PROFILES_GROUP_PROPERTY_NAME, STRING_STRINGS_MAP)
        .orElseGet { LinkedMultiValueMap() }

    /**
     * 激活的Profiles
     */
    private val activeProfiles: List<String> =
        expandProfiles(getActivatedProfiles(environment, binder, additionalProfiles))

    /**
     * 默认的Profiles
     */
    private val defaultProfiles: List<String> = expandProfiles(getDefaultProfiles(environment, binder))

    /**
     * 提供profiles的迭代的迭代器
     *
     * @return profiles迭代的迭代器
     */
    override fun iterator(): Iterator<String> = getAccepted().iterator()

    /**
     * 获取默认的Profiles
     *
     * @param environment Environment
     * @param binder Binder
     * @return default Profiles
     */
    private fun getDefaultProfiles(environment: Environment, binder: Binder): List<String> {
        return asUniqueItemList(getProfiles(environment, binder, Type.DEFAULT).toList())
    }

    /**
     * 获取激活的Profiles
     *
     * @param environment Environment
     * @param binder Binder
     * @param additionalProfiles 额外的需要使用的Profiles
     * @return 激活的Profiles列表
     */
    private fun getActivatedProfiles(
        environment: Environment,
        binder: Binder,
        additionalProfiles: Collection<String>
    ): List<String> {
        return asUniqueItemList(getProfiles(environment, binder, Type.ACTIVE), additionalProfiles)
    }

    private fun getProfiles(environment: Environment, binder: Binder, type: Type): Collection<String> {
        // 从Environment的Property当中, 根据prefix(spring.profiles.active/spring.profiles.default)去获取对应的配置信息
        val environmentPropertyValue = environment.getProperty(type.prefix)

        // 把Environment的Property当中配置的profiles进行拆分成为Set<String>
        val environmentPropertyProfiles =
            if (!StringUtils.hasText(environmentPropertyValue)) Collections.emptySet()
            else StringUtils.commaDelimitedListToStringArray(environmentPropertyValue).toSet()

        // 获取Environment当中对应的profiles...
        val environmentProfiles = LinkedHashSet(type.get(environment).toSet())

        // 将prefix对应的配置信息去完成绑定...
        val boundProfiles = binder.bind(type.prefix, STRING_SET)

        // 如果用户有使用编程式的方式去设置过Profiles, 那么需要去进行merge配置文件当中的profiles和Environment对象当中的profiles
        if (hasProgrammaticallySetProfiles(
                type, environmentPropertyValue, environmentPropertyProfiles, environmentProfiles
            )
        ) {
            // 如果无需去进行merge, 那么直接return Environment当中的profiles
            if (!type.mergeWithEnvironmentProfiles || !boundProfiles.isBound()) {
                return environmentProfiles
            }

            // 如果需要去进行merge的话, 那么需要将配置文件当中的和Environment当中的profiles去进行merge
            return boundProfiles.map { merge(environmentProfiles, it) }.get()
        }

        // 如果用户没有配置过Environment的话, 那么直接返回通过配置文件当中指定的...
        return boundProfiles.orElseGet { type.defaultValue }
    }

    /**
     * 检查用户是否有使用编程式的方式去设置过Profiles?
     *
     * @param type profileType(DEFAULT/ACTIVE)
     * @param environmentPropertyValue 在Environment的PropertySource当中配置的对应的配置信息
     * @param environmentPropertyProfiles 在Environment的PropertySource当中配置的对应的配置信息按照","去进行分割的结果
     * @param environmentProfiles Environment的字段(activeProfiles/defaultProfiles)当中配置的Profiles
     * @return 如果有编程式的设置过Profiles, return true; 否则return false
     */
    private fun hasProgrammaticallySetProfiles(
        type: Type,
        @Nullable environmentPropertyValue: String?,
        environmentPropertyProfiles: Set<String>,
        environmentProfiles: Set<String>
    ): Boolean {
        // 如果用户没有通过配置文件去进行配置的话, 那么检查Environment对象当中的配置是否和默认的配置一样?
        // 如果Environment的activeProfiles/defaultProfiles被动过了, 那么return true; 用户有进行过自定义
        if (!StringUtils.hasText(environmentPropertyValue)) {
            return type.defaultValue != environmentProfiles
        }
        // 如果Environment当中的Profiles和默认值一样, 那么就说明没有进行过用户自定义
        if (type.defaultValue == environmentProfiles) {
            return false
        }

        // 如果配置文件当中指定的和Environment当中的不一样...那么说明用户有进行过自定义, 如果一样则说明没有自定义过
        return environmentPropertyProfiles != environmentProfiles
    }

    private fun merge(environmentProfiles: Set<String>, bound: Set<String>): Set<String> {
        val result = LinkedHashSet(environmentProfiles)
        result.addAll(bound)
        return result
    }

    /**
     * 将给定的Profiles去转换成为唯一的元素的List
     *
     * @param profiles profiles
     * @return 去重之后的Profiles
     */
    private fun asUniqueItemList(profiles: Collection<String>): List<String> = asUniqueItemList(profiles, null)

    /**
     * 将给定的Profiles&additionalProfiles去转换成为唯一的元素的List
     *
     * @param profiles profiles
     * @param additionalProfiles 额外需要使用的Profiles
     * @return 两者merge之后去重得到的Profiles
     */
    private fun asUniqueItemList(
        profiles: Collection<String>,
        @Nullable additionalProfiles: Collection<String>?
    ): List<String> {
        val result = LinkedHashSet<String>()
        if (additionalProfiles != null) {
            result.addAll(additionalProfiles)
        }
        result.addAll(profiles)
        return result.toList()
    }

    /**
     * 检查group的方式的配置, 如果有配置group的话, 那么使用group去扩展给定的profiles
     *
     * @param profiles 原始的Profiles
     * @return 扩展之后的profiles
     */
    private fun expandProfiles(profiles: List<String>): List<String> {
        val stack = ArrayDeque<String>()
        profiles.reversed().forEach(stack::push)
        val expandedProfiles = LinkedHashSet<String>()
        while (stack.isNotEmpty()) {
            val current = stack.pop()
            if (expandedProfiles.add(current)) {
                // 把当前profile作为group, 去merge该group下的全部的profiles
                this.groups[current]?.reversed()?.forEach(stack::push)
            }
        }
        return asUniqueItemList(expandedProfiles)
    }


    /**
     * 获取所有接受的Profile列表
     *
     * @return accepted profiles
     */
    open fun getAccepted(): List<String> = activeProfiles.ifEmpty { defaultProfiles }

    /**
     * 检查给定的profile是否是一个可以被accept的profile
     *
     * @param profile profile
     * @return 如果该profile是active的, return true; 否则return false
     */
    open fun isAccepted(profile: String): Boolean = getAccepted().contains(profile)

    /**
     * 获取全部激活的Profiles
     *
     * @return active profiles
     */
    open fun getActive(): List<String> = this.activeProfiles

    /**
     * 获取全部默认的Profiles
     *
     * @return default profiles
     */
    open fun getDefault(): List<String> = this.defaultProfiles

    /**
     * profile的类型枚举值
     *
     * @param prefix 该类型的Profile要去绑定的属性前缀
     * @param getter 获取Environment当中配置的ActiveProfiles/DefaultProfiles的getter
     * @param mergeWithEnvironmentProfiles 是否需要使用Environment当中的Profiles去进行Merge
     * @param defaultValue 该类型的Profile的默认值
     */
    private enum class Type(
        val prefix: String,
        val getter: Function<Environment, Array<String>>,
        val mergeWithEnvironmentProfiles: Boolean,
        val defaultValue: Set<String>
    ) {
        ACTIVE(
            AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME,
            Environment::getActiveProfiles,
            true,
            Collections.emptySet()
        ),
        DEFAULT(
            AbstractEnvironment.DEFAULT_PROFILES_PROPERTY_NAME,
            Environment::getDefaultProfiles,
            false,
            Collections.singleton("default")
        );

        /**
         * 根据Environment去获取到对应的Profiles列表
         *
         * @param environment Environment
         * @return profiles
         */
        fun get(environment: Environment): Array<String> {
            return getter.apply(environment)
        }
    }
}