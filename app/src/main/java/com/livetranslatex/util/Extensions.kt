package com.livetranslatex.util

import java.security.MessageDigest

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return md.digest(toByteArray())
        .joinToString("") { "%02x".format(it) }
}
