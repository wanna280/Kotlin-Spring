package com.wanna.framework.util

/**
 * 基于LinkedHashMap的多值Map
 */
open class LinkedMultiValueMap<K, V> : MultiValueMapAdapter<K, V>(LinkedHashMap())