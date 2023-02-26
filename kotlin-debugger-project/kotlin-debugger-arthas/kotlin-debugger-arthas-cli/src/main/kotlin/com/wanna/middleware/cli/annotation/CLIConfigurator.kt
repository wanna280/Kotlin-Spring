package com.wanna.middleware.cli.annotation

import com.wanna.middleware.cli.CLI
import com.wanna.middleware.cli.CommandLine
import com.wanna.middleware.cli.TypedOption
import com.wanna.middleware.cli.impl.ArgumentComparator
import com.wanna.middleware.cli.impl.DefaultCLI
import com.wanna.middleware.cli.impl.ReflectionUtils
import java.lang.reflect.Method


/**
 * CLI的配置器的工具类, 负责提供工具方法, 去将一个beanClass当中的注解信息, 去解析成为CLI
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/26
 */
object CLIConfigurator {

    @JvmStatic
    fun define(clazz: Class<*>): CLI {
        return define(clazz, false)
    }

    @JvmStatic
    fun define(clazz: Class<*>, caseSensitive: Boolean): CLI {
        val cli = DefaultCLI()
        cli.setCaseSensitive(caseSensitive)
        val summary = clazz.getAnnotation(Summary::class.java)
        val description = clazz.getAnnotation(Description::class.java)
        val hidden = clazz.getAnnotation(Hidden::class.java)

        // 必须要有@Name注解, 才能被定义成为一个CLI命令
        val name = clazz.getAnnotation(Name::class.java)
            ?: throw IllegalStateException("The command cannot be defined, the @Name annotation is missing.")

        if (name.value.isNotBlank()) {
            cli.setName(name.value)
        } else {
            throw IllegalStateException("The command cannot be defined, the @Name value is empty or blank")
        }
        if (summary != null) {
            cli.setSummary(summary.value)
        }
        if (description != null) {
            cli.setDescription(description.value)
        }
        if (hidden != null) {
            cli.setHidden(true)
        }

        // 解析所有Setter上的@Option注解和@Argument注解, 保存到CLI当中
        val setterMethods = ReflectionUtils.getSetterMethods(clazz)
        for (setterMethod in setterMethods) {
            val option = setterMethod.getAnnotation(Option::class.java)
            if (option != null) {
                cli.addOption(createOption(setterMethod))
            }
            val argument = setterMethod.getAnnotation(Argument::class.java)
            if (argument != null) {
                cli.addArgument(createArgument(setterMethod))
            }
        }

        // 对Arguments去进行排序
        cli.setArguments(cli.getArguments().sortedWith(ArgumentComparator))
        return cli
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    private fun createOption(method: Method): com.wanna.middleware.cli.Option {
        val opt = TypedOption<Any>()
        val option = method.getAnnotation(Option::class.java)
            ?: throw IllegalStateException("Cannot find @Option annotation on method")

        opt.setArgName(option.argName)
            .setLongName(option.longName)
            .setShortName(option.shortName)

        val description = method.getAnnotation(Description::class.java)
        if (description != null) {
            opt.setDescription(description.value)
        }
        val hidden = method.getAnnotation(Hidden::class.java)
        if (hidden != null) {
            opt.setHidden(true)
        }

        if (ReflectionUtils.isMultiple(method)) {
            opt.setType(ReflectionUtils.getComponentType(method, 0) as Class<Any>).setMultipleValued(true)
        } else {
            val type = method.parameterTypes[0]
            opt.setType(type as Class<Any>)
            if (type != Boolean::class.java && type != Boolean::class.javaObjectType) {
                opt.setSingleValued(true)
            }
        }
        return opt
    }

    @JvmStatic
    private fun createArgument(method: Method): com.wanna.middleware.cli.Argument {
        return com.wanna.middleware.cli.Argument()
    }

    /**
     * 执行CLI的注入
     *
     * @param commandLine commandLine
     * @param obj 要去进行注入的实例对象
     */
    @JvmStatic
    fun inject(commandLine: CommandLine, obj: Any) {

    }
}