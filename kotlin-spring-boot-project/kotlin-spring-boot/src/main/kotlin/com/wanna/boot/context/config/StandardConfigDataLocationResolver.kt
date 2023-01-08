package com.wanna.boot.context.config

import com.wanna.boot.context.properties.bind.Bindable
import com.wanna.boot.context.properties.bind.Binder
import com.wanna.boot.env.PropertiesPropertySourceLoader
import com.wanna.boot.env.PropertySourceLoader
import com.wanna.boot.env.YamlPropertySourceLoader
import com.wanna.framework.constants.STRING_ARRAY_TYPE
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.StandardEnvironment
import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.support.PathMatchingResourcePatternResolver
import com.wanna.framework.core.io.support.SpringFactoriesLoader
import com.wanna.framework.lang.Nullable
import java.io.File
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

/**
 * 标准的ConfigDataLocation的Resolver实现
 */
open class StandardConfigDataLocationResolver() : Ordered,
    ConfigDataLocationResolver<StandardConfigDataResource> {
    companion object {

        private const val PREFIX = "resource:"

        /**
         * SpringBoot的配置文件名对应的属性
         */
        const val CONFIG_NAME_PROPERTY = "spring.config.name"

        /**
         * 默认的SpringBoot的配置文件名, 默认为application
         */
        @JvmField
        val DEFAULT_CONFIG_NAMES = arrayOf("application")

        /**
         * No Profile的常量标识
         */
        @JvmStatic
        private val NO_PROFILE: String? = null

        /**
         * 匹配URL前缀的Pattern正则表达式
         */
        @JvmStatic
        private val URL_PREFIX = Pattern.compile("^([a-zA-Z][a-zA-Z0-9*]*?:)(.*$)")

        /**
         * 匹配文件扩展名的Pattern
         */
        @JvmStatic
        private val EXTENSION_HINT_PATTERN = Pattern.compile("^(.*)\\[(\\.\\w+)\\](?!\\[)$")
    }

    /**
     * Order
     */
    private var order: Int = Ordered.ORDER_LOWEST

    /**
     * PropertySourceLocators
     *
     * @see PropertiesPropertySourceLoader
     * @see YamlPropertySourceLoader
     */
    private val propertySourceLoaders = SpringFactoriesLoader.loadFactories(PropertySourceLoader::class.java)

    /**
     * ResourceLoader
     */
    private val resourceLoader = LocationResourceLoader(PathMatchingResourcePatternResolver())

    /**
     * 配置文件name列表(默认为application)
     */
    private val configNames: Array<String> = this.getConfigNames(Binder.get(StandardEnvironment()))

    /**
     * 获取SprigBoot配置文件名, 尝试从"spring.config.name"当中去进行获取, 如果获取不到, 那么使用"application"作为默认的配置文件名
     *
     * @param binder Binder
     * @return ConfigNames
     */
    open fun getConfigNames(binder: Binder): Array<String> {
        return binder.bind(CONFIG_NAME_PROPERTY, Bindable.of(STRING_ARRAY_TYPE)).orElse(DEFAULT_CONFIG_NAMES)
    }

    override fun getOrder(): Int = this.order

    /**
     * 是否支持去解析? 我们一律支持去进行解析
     */
    override fun isResolvable(
        @Nullable context: ConfigDataLocationResolverContext?,
        location: ConfigDataLocation
    ): Boolean = true

    /**
     * 执行真正的解析, 将给定的ConfigDataLocation去解析成为ConfigDataResource
     *
     * @param context context
     * @param location location
     */
    override fun resolve(
        @Nullable context: ConfigDataLocationResolverContext?,
        location: ConfigDataLocation
    ): List<StandardConfigDataResource> {
        return resolve(getReferences(context, location.split()))
    }

    /**
     * 获取给定的locations去解析到的ConfigDataReference
     *
     * @param context context
     * @param locations 待解析引用的Locations
     * @return 根据locations解析得到的ConfigDataReference
     */
    private fun getReferences(
        context: ConfigDataLocationResolverContext?,
        locations: Array<ConfigDataLocation>
    ): Set<StandardConfigDataReference> {
        val references = LinkedHashSet<StandardConfigDataReference>()
        for (location in locations) {
            references += getReferences(context, location)
        }
        return references
    }

    /**
     * 为单个ConfigDataLocation去解析到对应的ConfigDataReference
     *
     * @param context context
     * @param location location
     */
    private fun getReferences(
        @Nullable context: ConfigDataLocationResolverContext?,
        location: ConfigDataLocation
    ): Set<StandardConfigDataReference> {
        val resourceLocation = getResourceLocation(context, location)
        if (isDirectory(resourceLocation)) {
            return getReferencesForDirectory(location, resourceLocation, NO_PROFILE)
        }
        return getReferencesForFile(location, resourceLocation, NO_PROFILE)
    }

    private fun getResourceLocation(
        @Nullable context: ConfigDataLocationResolverContext?,
        location: ConfigDataLocation
    ): String {
        val resourceLocation = location.getNonPrefixedValue(PREFIX)
        val isAbsolute = resourceLocation.startsWith("/") || URL_PREFIX.matcher(resourceLocation).matches()
        if (isAbsolute) {
            return resourceLocation
        }
        val parent = context?.getParent()
        if (parent is StandardConfigDataResource) {
            val parentResourceLocation = parent.reference.resourceLocation

            // 切取parentResource的目录
            val parentDirectory = parentResourceLocation.substring(0, parentResourceLocation.lastIndexOf('/') + 1)
            return parentDirectory + resourceLocation
        }
        return resourceLocation
    }

    /**
     * 从给定的目录下, 去解析到配置文件的Reference
     *
     * @param location location
     * @param directory directory
     * @param profile profile
     * @return 从该目录下, 解析到的配置文件的Reference
     */
    private fun getReferencesForDirectory(
        location: ConfigDataLocation,
        directory: String,
        profile: String?
    ): Set<StandardConfigDataReference> {
        val references = LinkedHashSet<StandardConfigDataReference>()
        for (configName in configNames) {
            val deque = getReferencesForConfigName(configName, location, directory, profile)
            references += deque
        }
        return references
    }

    /**
     * 为某个特定的Location、特定的Profile、特定的配置文件名, 去获取到配置文件名的Reference列表
     *
     * @param name 配置文件名, 默认为application
     * @param directory 目录
     * @param profile profile(null代表没有profile)
     * @param location location
     */
    private fun getReferencesForConfigName(
        name: String,
        location: ConfigDataLocation,
        directory: String,
        @Nullable profile: String?
    ): Deque<StandardConfigDataReference> {
        val references = ArrayDeque<StandardConfigDataReference>()
        for (propertySourceLoader in this.propertySourceLoaders) {
            for (extension in propertySourceLoader.getFileExtensions()) {
                val reference = StandardConfigDataReference(
                    location, directory, directory + name, profile, extension, propertySourceLoader
                )
                if (!references.contains(reference)) {
                    references.addFirst(reference)
                }
            }
        }
        return references
    }

    /**
     * 为给定的文件名, 去加载到该文件对应的Reference
     *
     * @param location location
     * @param file fileName
     * @param profile profile
     * @return 解析到的Reference
     */
    private fun getReferencesForFile(
        location: ConfigDataLocation,
        file: String,
        @Nullable profile: String?
    ): Set<StandardConfigDataReference> {

        var fileName: String = file
        val extensionHintMatcher = EXTENSION_HINT_PATTERN.matcher(file)
        val extensionHintLocation = extensionHintMatcher.matches()
        if (extensionHintLocation) {
            fileName = extensionHintMatcher.group(0) + extensionHintMatcher.group(1)
        }
        for (propertySourceLoader in propertySourceLoaders) {
            val extension = getLoadableFileExtension(propertySourceLoader, fileName)
            if (extension != null) {
                val root = fileName.substring(0, fileName.length - extension.length - 1)
                val reference = StandardConfigDataReference(
                    location, null, root,
                    profile, if (!extensionHintLocation) extension else null, propertySourceLoader
                )
                return setOf(reference)
            }
        }
        throw IllegalStateException(
            "File extension is not known to any PropertySourceLoader. If the location is meant to reference a directory, it must end in '/' or File.separator"
        )
    }

    @Nullable
    private fun getLoadableFileExtension(loader: PropertySourceLoader, file: String): String? {
        for (fileExtension in loader.getFileExtensions()) {
            if (file.endsWith(fileExtension, false)) {
                return fileExtension
            }
        }
        return null
    }

    /**
     * 检查给定的这个资源路径是否是一个目录
     *
     * @param resourceLocation 待检查的资源路径
     * @return 如果资源路径是以"/"作为结尾, 那么说明是个目录, return true; 否则return false
     */
    private fun isDirectory(resourceLocation: String): Boolean =
        resourceLocation.endsWith("/") || resourceLocation.endsWith(File.separator)

    /**
     * 根据解析到的Reference, 去解析到对应的Resource
     *
     * @param references 待解析的Reference列表
     * @return 解析到的Resource列表
     */
    private fun resolve(references: Set<StandardConfigDataReference>): List<StandardConfigDataResource> {
        val resolved = ArrayList<StandardConfigDataResource>()
        for (reference in references) {
            resolved += resolve(reference)
        }
        if (resolved.isEmpty()) {
            resolved += resolveEmptyDirectories(references)
        }
        return resolved
    }

    /**
     * 解析给定的Reference成为Resource
     */
    private fun resolve(reference: StandardConfigDataReference): List<StandardConfigDataResource> {
        if (!resourceLoader.isPattern(reference.resourceLocation)) {
            return resolveNonPattern(reference)
        }
        return resolvePattern(reference)
    }

    /**
     * 在没有表达式的情况下, 我们直接根据资源路径去获取到对应的资源文件
     *
     * @param reference reference
     * @return 解析得到的Resource
     */
    private fun resolveNonPattern(reference: StandardConfigDataReference): List<StandardConfigDataResource> {
        val resource = resourceLoader.getResource(reference.resourceLocation)
        if (!resource.exists() && reference.skippable) {
            return emptyList()
        }
        return listOf(createConfigResourceLocation(reference, resource))
    }

    private fun resolvePattern(reference: StandardConfigDataReference): List<StandardConfigDataResource> {
        return emptyList()
    }

    private fun createConfigResourceLocation(
        reference: StandardConfigDataReference,
        resource: Resource
    ): StandardConfigDataResource {
        return StandardConfigDataResource(reference, resource)
    }

    private fun resolveEmptyDirectories(references: Set<StandardConfigDataReference>): Collection<StandardConfigDataResource> {
        return emptyList()
    }

    /**
     * 执行给定的Profiles的解析
     */
    override fun resolveProfileSpecific(
        @Nullable context: ConfigDataLocationResolverContext?,
        location: ConfigDataLocation,
        profiles: Profiles
    ): List<StandardConfigDataResource> {
        return super.resolveProfileSpecific(context, location, profiles)
    }
}