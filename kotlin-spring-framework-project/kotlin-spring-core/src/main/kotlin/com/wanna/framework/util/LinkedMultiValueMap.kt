package com.wanna.framework.util

/**
 * 基于[LinkedHashMap]去进行实现的多值Map
 *
 * @param K keyType
 * @param V valueType
 */
open class LinkedMultiValueMap<K, V> : MultiValueMapAdapter<K, V>(LinkedHashMap())