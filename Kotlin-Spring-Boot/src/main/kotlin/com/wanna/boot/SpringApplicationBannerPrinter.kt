package com.wanna.boot

import com.wanna.framework.core.environment.ConfigurableEnvironment
import com.wanna.framework.core.environment.Environment
import org.slf4j.Logger
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintStream

/**
 * 这是SpringApplicationBanner的打印器，负责完成Banner的打印；支持使用Logger和Console两种方式去进行日志的输出；
 * 采用的默认Banner为SpringBoot的Banner
 *
 * @see SpringBootBanner
 */
open class SpringApplicationBannerPrinter(private val fallbackBanner: Banner?) {

    companion object {
        const val BANNER_CHARSET_PROPERTY = "spring.banner.charset" // banner charset property name
        const val BANNER_LOCATION_PROPERTY = "spring.banner.location"  // location of banner
        const val DEFAULT_BANNER_LOCATION = "banner.txt"  // default banner location
    }

    // 默认的Banner，打印SpringBoot的Logo，如果没有配置自定义的话，将会采用这个Banner
    private val defaultBanner = SpringBootBanner()

    /**
     * 使用日志的方式去输出Banner，本质上也是调用的输出流的Banner去获取到要输出的字符串，再交给Slf4j的Logger去进行输出；
     * 只是在中间去进行了一层的转换，让输出流的Banner也能使用到Logger去完成日志的输出
     *
     * @param environment Environment
     * @param mainClass mainClass
     * @param logger 要使用的输出的Logger
     * @return 将原来的创建好的Banner包装成为PrintedBanner去进行return
     */
    open fun print(environment: ConfigurableEnvironment, mainClass: Class<*>, logger: Logger): Banner {
        val banner = getBanner(environment)  // getBanner
        logger.info(createStringFromBanner(environment, mainClass, banner))  // Banner获取到输出的字符串，并交给Logger去进行输出
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
    open fun print(environment: ConfigurableEnvironment, mainClass: Class<*>, printStream: PrintStream): Banner {
        val banner = getBanner(environment)  // getBanner
        banner.printBanner(environment, mainClass, printStream)  // printBanner
        return PrintedBanner(banner, mainClass)  // wrapBanner
    }

    /**
     * (1)获取Banner，如果配置了自定义的Banner，那么使用自定义的Banner；
     * (2)如果没有找到自定义的Banner，那么先考虑使用fallbackBanner
     * (3)如果fallbackBanner也没有，那么使用默认的SpringBootBanner即可
     */
    private fun getBanner(environment: Environment): Banner {
        val banners = Banners()
        banners.addBannerInNecessary(getImageBanner(environment))
        banners.addBannerInNecessary(getTextBanner(environment))
        if (banners.hasAtLeastOneBanner()) {
            return banners
        }
        if (fallbackBanner != null) {
            return fallbackBanner
        }
        return defaultBanner
    }

    /**
     * 要使用Logger去进行输出，但是Banner只能接受PrintStream去进行输出，因此这里创建一个ByteArrayOutputStream去存储PrintStream
     * 输出的内容，接着将ByteArrayOutputStream转为字符串，交给Logger去进行输出即可(适配器模式？)
     */
    private fun createStringFromBanner(environment: Environment, mainClass: Class<*>, banner: Banner): String {
        val bous = ByteArrayOutputStream()
        banner.printBanner(environment, mainClass, PrintStream(bous))
        val charset = environment.getProperty(BANNER_CHARSET_PROPERTY, "utf-8")
        return bous.toString(charset)
    }


    /**
     * 这是对Banner去进行的聚合，内部聚合了多个Banner，支持去添加多个Banner，去完成打印
     */
    class Banners : Banner {
        // Banner列表
        private val bannerList = ArrayList<Banner>()

        /**
         * 如果必要的话，添加一个Banner
         */
        fun addBannerInNecessary(banner: Banner?) {
            if (banner != null) {
                this.bannerList += banner
            }
        }

        /**
         * Banner列表当中是否至少含有一个Banner？
         */
        fun hasAtLeastOneBanner(): Boolean = bannerList.size >= 1

        /**
         * 打印Banner，采用的打印的方式为：遍历所有的Banner，挨个去完成Banner的打印
         */
        override fun printBanner(environment: Environment, sourceClass: Class<*>, printStream: PrintStream) {
            bannerList.forEach { it.printBanner(environment, sourceClass, printStream) }
        }
    }

    /**
     * 这是已经已经完成打印的Banner，它将之前已经创建好的Banner去进行保存
     */
    class PrintedBanner(private val banner: Banner, private val sourceClass: Class<*>) : Banner {
        override fun printBanner(environment: Environment, sourceClass: Class<*>, printStream: PrintStream) {
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
     * 获取Text的Banner
     *
     * // TODO
     */
    private fun getTextBanner(environment: Environment): Banner? {
        val bannerLocation = environment.getProperty(BANNER_LOCATION_PROPERTY, DEFAULT_BANNER_LOCATION)
        return null
    }
}