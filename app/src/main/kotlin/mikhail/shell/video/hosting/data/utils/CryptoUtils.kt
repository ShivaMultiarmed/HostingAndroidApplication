package mikhail.shell.video.hosting.data.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object CryptoUtils {
    private const val KEY_ALIAS = "trendy_key"
    private const val PROVIDER = "AndroidKeyStore"
    private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
    private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
    private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"

    private val cipher = Cipher.getInstance(TRANSFORMATION)
    private val keystore = KeyStore
        .getInstance(PROVIDER)
        .apply {
            load(null)
        }
    private fun generateKey(): SecretKey = KeyGenerator
        .getInstance(ALGORITHM, PROVIDER)
        .apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setRandomizedEncryptionRequired(true)
                    .setUserAuthenticationRequired(false)
                    .build()
            )
        }
        .generateKey()
    private fun getKey(): SecretKey {
        val existingKeyEntry = keystore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKeyEntry?.secretKey?: generateKey()
    }
    @OptIn(ExperimentalEncodingApi::class)
    fun encrypt(input: String): String {
        val inputBytes = input.encodeToByteArray()
        val encryptedBytes = encrypt(inputBytes)
        return Base64.encode(encryptedBytes)
    }
    fun encrypt(input: ByteArray): ByteArray {
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        val iv = cipher.iv
        return iv + cipher.doFinal(input)
    }
    @OptIn(ExperimentalEncodingApi::class)
    fun decrypt(input: String): String {
        val inputBytes = Base64.decode(input)
        val decryptedBytes = decrypt(inputBytes)
        return decryptedBytes.decodeToString()
    }
    fun decrypt(input: ByteArray): ByteArray {
        val iv = input.copyOfRange(0, cipher.blockSize)
        val encryptedBytes = input.copyOfRange(cipher.blockSize, input.size)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
        return cipher.doFinal(encryptedBytes)
    }
}