package com.wanna.framework.web.http

interface HttpMessage {
    fun getHeaders(): Map<String, String>
}