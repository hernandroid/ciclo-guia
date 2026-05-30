package com.cicloguia.app.feature.map.data

import java.security.MessageDigest

object Sha256Checksum {

    fun calculate(content: String): String {
        val digest = MessageDigest
            .getInstance(ALGORITHM)
            .digest(content.toByteArray(Charsets.UTF_8))

        return digest.joinToString(separator = "") { byte ->
            "%02x".format(byte.toInt() and 0xff)
        }
    }

    private const val ALGORITHM = "SHA-256"
}
