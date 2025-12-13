package com.example.screenshotocr.util

import java.io.File
import java.security.MessageDigest

object HashUtil {
    fun sha256(path: String): String {
        val bytes = File(path).readBytes()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}