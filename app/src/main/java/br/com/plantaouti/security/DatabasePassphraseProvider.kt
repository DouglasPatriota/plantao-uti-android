package br.com.plantaouti.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gera e armazena a chave criptográfica de 256 bits do SQLCipher usando
 * Android Keystore (AES256-GCM) via EncryptedSharedPreferences.
 * Os dados clínicos permanecem exclusivamente no dispositivo (LGPD).
 */
@Singleton
class DatabasePassphraseProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getOrCreatePassphrase(): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val existing = prefs.getString(KEY_DB_PASSPHRASE, null)
        if (!existing.isNullOrBlank()) {
            return Base64.decode(existing, Base64.NO_WRAP)
        }

        val generated = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val encoded = Base64.encodeToString(generated, Base64.NO_WRAP)
        prefs.edit().putString(KEY_DB_PASSPHRASE, encoded).apply()
        return generated
    }

    companion object {
        private const val PREFS_NAME = "plantao_uti_secure_keys"
        private const val KEY_DB_PASSPHRASE = "sqlcipher_passphrase_v1"
    }
}
