package com.wanna.framework.web.http

/**
 * HttpStatus的枚举值
 *
 * @param value HttpStatus状态码的具体的值
 * @param series 该状态码所属的系列
 * @param reasonPhase reasonMessage
 */
enum class HttpStatus(val value: Int, val series: Series, val reasonPhase: String) {
    SUCCESS(200, Series.SUCCESSFUL, "OK"),
    BAD_REQUEST(400, Series.CLIENT_EROR, "Bad Request"),
    FORBIDDEN(403, Series.CLIENT_EROR, "Forbidden"),
    NOT_FOUND(404, Series.CLIENT_EROR, "Not Found"),
    INTERNAL_SERVER_ERROR(500, Series.SERVER_ERROR, "Internal Server Error")
    ;

    /**
     * Http状态码所属的系列的枚举值, 也就是我们常说的"1xx", "2xx", "3xx", "4xx", "5xx"
     *
     * @param value SeriesInt
     */
    enum class Series(val value: Int) {
        INFORMATIONAL(1),
        SUCCESSFUL(2),
        REDIRECTION(3),
        CLIENT_EROR(4),
        SERVER_ERROR(5)
    }
}