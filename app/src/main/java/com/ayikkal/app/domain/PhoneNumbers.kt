package com.ayikkal.app.domain

object PhoneNumbers {
    /**
     * Karşılaştırma anahtarı: yalnızca rakamlar, son 10 hane.
     * "+90 532 111 22 33", "0532 111 2233" ve "5321112233" aynı kişiye denk gelir.
     */
    fun normalize(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return if (digits.length > 10) digits.takeLast(10) else digits
    }

    fun matches(a: String, b: String): Boolean {
        val na = normalize(a)
        val nb = normalize(b)
        return na.length >= 6 && na == nb
    }

    /** "0532 *** ** 12" biçiminde maskeleme. */
    fun mask(raw: String): String {
        val n = normalize(raw)
        if (n.length < 10) return raw
        return "0${n.substring(0, 3)} *** ** ${n.takeLast(2)}"
    }
}
