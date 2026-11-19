package br.com.plantaouti.security

import java.security.MessageDigest

object PinHasher {
    private const val SALT = "plantao_uti_local_salt_v1"

    fun hash(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$SALT:$pin".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verify(pin: String, storedHash: String): Boolean {
        if (storedHash.isBlank()) return false
        return hash(pin) == storedHash
    }
}
