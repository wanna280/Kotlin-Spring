package com.wanna.boot

import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.io.ResourceLoader
import org.slf4j.Logger
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintStream

/**
 * 这是SpringApplicationBanner的打印器, 负责完成Banner的打印; 支持使用Logger和Console两种方式去进行日志的输出;
 * 采用的默认Banner为SpringBoot的Banner
 *
 * @see SpringBootBanner
 *
 * @param resourceLoader 加载资源的ResourceLoader
 * @param fallbackBanner 当没有根据路径匹配到合适的Banner时, 应该使用的fallbackBanner
 */
open class SpringApplicationBannerPrinter(
    private val resourceLoader: ResourceLoader,
    private val fallbackBanner: Banner?
) {
    companion object {

        /**
         * banner charset property name
         */
        const val BANNER_CHARSET_PROPERTY = "spring.banner.charset"

        /**
         * location of banner
         */
        const val BANNER_LOCATION_PROPERTY = "spring.banner.location"

        /**
         * default banner location
         */
        const val DEFAULT_BANNER_LOCATION = "banner.txt"
    }

    /**
     * 默认的Banner, 打印SpringBoot的Logo, 如果没有配置自定义的话, 将会采用这个Banner
     */
    private val defaultBanner = SpringBootBanner()

    /**
     * 使用日志的方式去输出Banner, 本质上也是调用的输出流的Banner去获取到要输出的字符串, 再交给Slf4j的Logger去进行输出;
     * 只是在中间去进行了一层的转换, 让输出流的Banner也能使用到Logger去完成日志的输出
     *
     * @param environment Environment
     * @param mainClass mainClass
     * @param logger 要使用的输出的Logger
     * @return 将原来的创建好的Banner包装成为PrintedBanner去进行return
     */
    open fun print(environment: ConfigurableEnvironment, mainClass: Class<*>?, logger: Logger): Banner {
        // getBanner
        val banner = getBanner(environment)

        // 获取到Banner需要去进行输出的字符串, 并交给Logger去进行最终的输出
        logger.info(createStringFromBanner(environment, mainClass, banner))

        return PrintedBanner(banner, mainClass)  // wrapBanner
    }

    /**
     * 使用输出流的方式去输出Banner
     *
     * @param environment Environment
     * @param mainClass
     * @param printStream 打印输出流
     * @return 将原来的创建好的Banner包装成为PrintedBanner去进行return
     */
    open fun print(environment: ConfigurableEnvironment, mainClass: Class<*>?, printStream: PrintStream): Banner {

        // getBanner
        val banner = getBanner(environment)

        // printBanner
        banner.printBanner(environment, mainClass, printStream)

        // wrapBanner
        return PrintedBanner(banner, mainClass)
    }

    /**
     * (1)获取Banner, 如果配置了自定义的Banner, 那么使用自定义的Banner;
     * (2)如果没有找到自定义的Banner, 那么先考虑使用fallbackBanner
     * (3)如果fallbackBanner也没有, 那么使用默认的SpringBootBanner即可
     *
     * @param environment Spring应用的Environment信息
     */
    private fun getBanner(environment: Environment): Banner {
        val banners = Banners()

        // 如果必要的话, 添加一个ImageBanner
        banners.addBannerInNecessary(getImageBanner(environment))

        // 如果必要的话, 添加一个TextBanner
        banners.addBannerInNecessary(getTextBanner(environment))

        // 如果之前存在有至少一个Banner的话, 那么直接沿用
        if (banners.hasAtLeastOneBanner()) {
            return banners
        }

        // 如果没有找到合适的Banner, 那么尝试使用fallbackBanner
        // 如果fallbackBanner都不存在的话, 那么直接使用defaultBanner去作为输出的Banner
        return fallbackBanner ?: defaultBanner
    }

    /**
     * 要使用Logger去进行输出, 但是Banner只能接受PrintStream去进行输出,
     * 因此这里创建一个ByteArrayOutputStream去存储PrintStream当中的内容
     * 输出的内容, 接着将ByteArrayOutputStream转为字符串, 交给Logger去进行输出即可(适配器模式? )
     *
     * @param environment Environment
     * @param mainClass mainClass
     * @param banner Banner
     * @return 解析到的真正的要去进行输出的Banner字符串
     */
    private fun createStringFromBanner(environment: Environment, mainClass: Class<*>?, banner: Banner): String {
        val bous = ByteArrayOutputStream()
        banner.printBanner(environment, mainClass, PrintStream(bous))
        val charset = environment.getProperty(BANNER_CHARSET_PROPERTY, "utf-8")
        return bous.toString(charset)
    }


    /**
     * 这是对Banner去进行的聚合, 内部聚合了多个Banner, 支持去添加多个Banner, 去完成打印
     *
     * @see Banner
     */
    class Banners : Banner {
        // Banner列表
        private val bannerList = ArrayList<Banner>()

        /**
         * 如果必要的话, 添加一个Banner(如果Banner为空的话, 那么就不必去进行继续的添加)
         *
         * @param banner 尝试去进行添加的Banner
         */
        fun addBannerInNecessary(banner: Banner?) {
            if (banner != null) {
                this.bannerList += banner
            }
        }

        /**
         * Banner列表当中是否至少含有一个Banner?
         *
         * @return 如果bannerList当中存在有Banner的话, 那么return true; 否则return false
         */
        fun hasAtLeastOneBanner(): Boolean = bannerList.size >= 1

        /**
         * 打印Banner, 采用的打印的方式为：遍历所有的Banner, 挨个去完成Banner的打印
         *
         * @param environment Environment
         * @param sourceClass sourceClass
         * @param printStream 输出流
         */
        override fun printBanner(environment: Environment, sourceClass: Class<*>?, printStream: PrintStream) {
            bannerList.forEach { it.printBanner(environment, sourceClass, printStream) }
        }
    }

    /**
     * 这是已经完成打印的Banner, 它将之前已经创建好的Banner去进行保存,
     * 如果后续当中还需要进行打印的话, 也可以使用这个Banner去完成打印,
     * 它通常会被加入到SpringApplication的beanFactory当中, 因此它
     * 本身也可以使用Autowire去进行自动注入, 从而获取到
     *
     * @param banner 内部包装的Banner, 执行真正的Banner输出工作
     * @param sourceClass sourceClass
     */
    class PrintedBanner(private val banner: Banner, private val sourceClass: Class<*>?) : Banner {
        override fun printBanner(environment: Environment, sourceClass: Class<*>?, printStream: PrintStream) {
            banner.printBanner(environment, sourceClass, printStream)
        }
    }

    /**
     * 获取Image的Banner
     * // TODO
     */
    private fun getImageBanner(environment: Environment): Banner? {
        return null
    }

    /**
     * 获取Text的Banner, 尝试从文件当中去获取合适的Banner文件, 具体的文件路径, 从Environment当中去进行获取
     *
     * @param environment Environment
     * @return 如果解析到了TextBanner的资源文件, 那么return ResourceBanner; 否则return null
     */
    private fun getTextBanner(environment: Environment): Banner? {
        // 获取BannerLocation
        val bannerLocation = environment.getProperty(BANNER_LOCATION_PROPERTY, DEFAULT_BANNER_LOCATION)
        val resource = resourceLoader.getResource(bannerLocation)
        try {
            if (resource.exists()) {
                return ResourceBanner(resource)
            }
        } catch (ex: IOException) {
            // ignore
        }
        return null
    }
}