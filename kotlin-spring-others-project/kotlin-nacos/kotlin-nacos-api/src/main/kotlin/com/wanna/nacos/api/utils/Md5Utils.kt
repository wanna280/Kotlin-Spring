package com.wanna.nacos.api.utils

import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.jvm.Throws

/**
 * MD5工具类, 提供将字符串去编码成为十六进制的MD5的方式
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/15
 */
object Md5Utils {

    /**
     * MD5常量
     */
    private const val MD5 = "MD5"

    /**
     * 方便根据index去获取到对应的16进制字符串的数组, 例如index=11得到"b", index=12得到"c"
     */
    @JvmStatic
    private val DIGITS_LOWER = charArrayOf(
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f'
    )

    /**
     * MessageDigest, 提供MD5的编码
     */
    private val MESSAGE_DIGEST_LOCAL = object : ThreadLocal<MessageDigest?>() {
        override fun initialValue(): MessageDigest? {
            return try {
                MessageDigest.getInstance(MD5)
            } catch (ex: NoSuchAlgorithmException) {
                null
            }
        }
    }

    /**
     * 根据指定的编码方式, 去将给定的[content]使用[encode]编码方式去转换成为ByteArray, 再把ByteArray去转换成为MD5的16进制字符串
     *
     * @param content 待进行MD5编码的文本content
     * @param encode 编码方式(例如"UTF-8")
     * @return 将content去转换成为MD5编码之后的结果字符串
     */
    @JvmStatic
    fun md5Hex(content: String, encode: String): String {
        return md5Hex(content.toByteArray(Charset.forName(encode)))
    }

    /**
     * 根据给定的[content]的ByteArray去编码得到16进制的字符串
     *
     * @param content 待进行MD5编码文本的ByteArray
     * @return 经过MD5编码之后的结果字符串
     * @throws NoSuchAlgorithmException 如果无法通过SPI获取到MD5的MessageDigest
     */
    @Throws(NoSuchAlgorithmException::class)
    @JvmStatic
    fun md5Hex(content: ByteArray): String {
        try {
            val messageDigest = MESSAGE_DIGEST_LOCAL.get()
            if (messageDigest != null) {
                return encodeHexString(messageDigest.digest(content))
            }
            throw NoSuchAlgorithmException("无法使用SPI获取到MD5的MessageDigest")
        } finally {
            MESSAGE_DIGEST_LOCAL.remove()  // remove
        }
    }

    /**
     * 将给定的ByteArray去转换成为16进制字符串
     * (一个byte转为高4Bit和低4Bit的方式, 就可以转换为0-f的是十六进制数, 因此一个byte就可以得到两个十六进制数)
     *
     * @param bytes 待转为16进制字符串的ByteArray内容
     * @return 转为16进制字符串之后的字符串
     */
    @JvmStatic
    fun encodeHexString(bytes: ByteArray): String {
        val l: Int = bytes.size
        var i = 0
        var j = 0
        // 申请一个长度为2倍的ByteArray
        val out = CharArray(l shl 1)
        while (i < l) {
            // 提取高4Bit
            out[j++] = DIGITS_LOWER[(0xF0 and bytes[i].toInt()) ushr 4]

            // 提取低4Bit
            out[j++] = DIGITS_LOWER[0x0F and bytes[i].toInt()]
            i++
        }
        // 将ByteArray去转为字符串
        return String(out)
    }
}