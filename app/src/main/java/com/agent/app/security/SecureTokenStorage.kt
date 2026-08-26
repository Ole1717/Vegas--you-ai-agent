package com.agent.app.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureTokenStorage(context: Context) {

    private val preferences = context.getSharedPreferences(
        "vegas_secure_storage",
        Context.MODE_PRIVATE
    )

    private val keyStore = java.security.KeyStore.getInstance(
        ANDROID_KEYSTORE
    ).apply {
        load(null)
    }

    private fun getOrCreateKey(): SecretKey {
        val existingKey = keyStore.getKey(KEY_ALIAS, null)

        if (existingKey is SecretKey) {
            return existingKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or
                KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(
                KeyProperties.ENCRYPTION_PADDING_NONE
            )
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(spec)

        return keyGenerator.generateKey()
    }

    fun saveGitHubToken(token: String) {
        if (token.isBlank()) {
            throw IllegalArgumentException("GitHub token is empty")
        }

        val cipher = Cipher.getInstance(TRANSFORMATION)

        cipher.init(
            Cipher.ENCRYPT_MODE,
            getOrCreateKey()
        )

        val encrypted = cipher.doFinal(
            token.toByteArray(StandardCharsets.UTF_8)
        )

        val iv = cipher.iv

        preferences.edit()
            .putString(
                KEY_ENCRYPTED_TOKEN,
                Base64.encodeToString(
                    encrypted,
                    Base64.NO_WRAP
                )
            )
            .putString(
                KEY_IV,
                Base64.encodeToString(
                    iv,
                    Base64.NO_WRAP
                )
            )
            .apply()
    }

    fun getGitHubToken(): String? {
        val encryptedBase64 = preferences.getString(
            KEY_ENCRYPTED_TOKEN,
            null
        )

        val ivBase64 = preferences.getString(
            KEY_IV,
            null
        )

        if (encryptedBase64.isNullOrBlank() ||
            ivBase64.isNullOrBlank()
        ) {
            return null
        }

        return try {
            val encrypted = Base64.decode(
                encryptedBase64,
                Base64.NO_WRAP
            )

            val iv = Base64.decode(
                ivBase64,
                Base64.NO_WRAP
            )

            val cipher = Cipher.getInstance(TRANSFORMATION)

            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(
                    GCM_TAG_LENGTH,
                    iv
                )
            )

            String(
                cipher.doFinal(encrypted),
                StandardCharsets.UTF_8
            )
        } catch (_: Exception) {
            null
        }
    }

    fun deleteGitHubToken() {
        preferences.edit()
            .remove(KEY_ENCRYPTED_TOKEN)
            .remove(KEY_IV)
            .apply()
    }

    fun hasGitHubToken(): Boolean {
        return getGitHubToken() != null
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"

        private const val KEY_ALIAS =
            "vegas_github_token_key"

        private const val KEY_ENCRYPTED_TOKEN =
            "github_token_encrypted"

        private const val KEY_IV =
            "github_token_iv"

        private const val TRANSFORMATION =
            "AES/GCM/NoPadding"

        private const val GCM_TAG_LENGTH = 128
    }
}
