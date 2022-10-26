package com.wanna.boot.autoconfigure.condition

import com.wanna.framework.util.ClassUtils
import java.io.Console
import java.lang.invoke.MethodHandles
import java.time.Duration
import java.util.*
import java.util.stream.Stream

/**
 * Java版本的枚举值，每个版本都通过一个方法去进行探测得到
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/27
 *
 * @param versionName 当前版本的名称(例如1.8/9/10)
 * @param clazz 鉴别当前Java版本所使用到的类
 * @param methodName 鉴别当前Java版本所使用的方法名？
 * @param available 该Java版本是否是available的？
 */
enum class JavaVersion(
    val versionName: String,
    val clazz: Class<*>,
    val methodName: String,
    private val available: Boolean = ClassUtils.hasMethod(clazz, methodName)
) {

    /**
     * Java8，新增了Optional类
     */
    EIGHT(
        "1.8", Optional::
        class.java, "empty"
    ),

    /**
     * Java9，Optional新增了stream方法
     */
    NIGHT(
        "9", Optional::
        class.java, "stream"
    ),

    /**
     * java10，Optional新增了orElseThrow方法
     */
    TEN(
        "10", Optional::
        class.java, "orElseThrow"
    ),

    /**
     * Java11，String新增了strip方法的实现
     */
    ELEVEN(
        "11", java.lang.String::
        class.java, "strip"
    ),

    /**
     * Java12，String新增了describeConstable方法的实现
     */
    TWELVE(
        "12", java.lang.String::
        class.java, "describeConstable"
    ),

    /**
     * Java13，String新增了stripIndent方法的实现
     */
    THIRTEEN(
        "13", java.lang.String::
        class.java, "stripIndent"
    ),

    /**
     * Java14，MethodHandles.Lookup新增了hasFullPrivilegeAccess的实现
     */
    FOURTEEN(
        "14", MethodHandles.Lookup::
        class.java, "hasFullPrivilegeAccess"
    ),

    /**
     * Java15，CharSequence类新增了方法的实现
     */
    FIFTEEN(
        "15", java.lang.CharSequence::
        class.java, "isEmpty"
    ),

    /**
     * Java16，Stream类新增了toList方法的实现
     */
    SIXTEEN(
        "16", Stream::
        class.java, "toList"
    ),

    /**
     * Java17，Console类新增了charset方法的实现
     */
    SEVENTEEN(
        "17", Console::
        class.java, "charset"
    ),

    /**
     * Java18，Duration类新增了isPositive方法的实现
     */
    EIGHTEEN(
        "18", Duration::
        class.java, "isPositive"
    );


    /**
     * 判断当前(this)是否比给定的版本高(">=")？
     *
     * @param version 需要去进行检查的版本version
     * @return 如果当前比version高(">=")，那么return true；否则return false
     */
    open fun isEqualOrNewerThan(version: JavaVersion): Boolean = compareTo(version) >= 0

    /**
     * 判断当前(this)是否比给定的版本低("<")?
     *
     * @param version 需要去进行检查的版本version
     * @return 如果比version低("<")，那么return true；否则return false
     */
    open fun isOlderThan(version: JavaVersion): Boolean = compareTo(version) <= 0


    companion object {

        /**
         * 获取当前的Java版本，从高版本往低版本方向去进行检查；
         * 直到找到一个available的，那么说明它就是当前Java版本；
         *
         * @return Current JVM JavaVersion
         */
        @JvmStatic
        fun getJavaVersion(): JavaVersion {
            val javaVersions = ArrayList(listOf(*values()))
            javaVersions.reverse()
            javaVersions.forEach {
                if (it.available) {
                    return it
                }
            }
            return EIGHT
        }
    }
}