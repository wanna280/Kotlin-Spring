package com.wanna.boot.context.properties.source

/**
 * 不合法的[ConfigurationPropertyName]异常;
 * 对于一个属性名, 只允许有小写字母/数字/'-'的存在, 别的字符都是不合法的
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 *
 * @param message error message
 * @param name 属性名
 * @param invalidCharacters 包含的不合法的字符
 */
class InvalidConfigurationPropertyNameException private constructor(
    message: String,
    val name: String,
    val invalidCharacters: List<Char>
) : RuntimeException(message) {

    constructor(name: String, invalidCharacters: List<Char>) : this(
        "ConfigurationPropertyName of [$name]当中可能存在有${invalidCharacters}这些字符不合法", name, invalidCharacters
    )

    companion object {

        /**
         * 如果存在有不合法的字符的话, 那么就抛异常
         *
         * @param name name属性名
         * @param invalidCharacters 不合法的字符列表
         * @throws InvalidConfigurationPropertyNameException 如果invalidCharacters列表不为空
         */
        @JvmStatic
        fun throwIfHasInvalidCharacters(name: String, invalidCharacters: List<Char>) {
            if (invalidCharacters.isNotEmpty()) {
                throw InvalidConfigurationPropertyNameException(name, invalidCharacters)
            }
        }
    }

}