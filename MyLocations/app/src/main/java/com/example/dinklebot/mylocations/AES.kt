package com.example.dinklebot.mylocations

/* ********************************* */
/* Program: My Locations */
/* Description: Class AES encrypts and decrypts strings using specified cipher*/
/* Author: Robert Leslie */
/* Notes: */
/* Reference: Jianna Zhang Advanced Encryption Standard*/
/* Last Modified: 6/8/2018 */
/* ********************************** */

import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class AES @Throws(GeneralSecurityException::class)

internal constructor() {

    //global args
    private var cipher: Cipher? = null
    private var rand: SecureRandom? = null
    private lateinit var secretKey : SecretKey

    //initiate cipher and secretkey for encryption
    init {

        cipher = Cipher.getInstance(ALGORITHM)
        rand = SecureRandom()
        /*val keyGen = KeyGenerator.getInstance(ALGORITHM)
        keyGen.init(256)*/
        val intary = arrayOf(-97, 14, -3, 6, 71, 64, 112, -74, -109, 48, 16, 127, -107, 86, -3, 120, -107, -31, 80, 126, 53, 88, -95, -40, -34, 75, 102, 100, -4, -124, 106, -44)
        val byteary = intary.foldIndexed(ByteArray(intary.size)) { i, a, v -> a.apply { set(i, v.toByte()) } }
        secretKey = SecretKeySpec(byteary, 0, byteary.size, "AES")
    }

    //method to encrypt or decrypt given string message based on given int opMode ENCRYPT_MODE or DECRYPT_MODE
    fun crypt(opMode: Int, message: String, key: SecretKey): String? {
        return try {
            cipher?.init(opMode, key, rand)
            val messageBytes = message.toByteArray(charset("ISO-8859-1"))
            val encodedBytes = cipher?.doFinal(messageBytes)

            String(encodedBytes!!, charset("ISO-8859-1"))
        } catch (e: Exception) {
            null
        }
    }

    //getter methods for getting the secretkey as either SecretKey or byteArray

    fun getKey() : SecretKey {
        return secretKey
    }

    fun getKeyBytes(): ByteArray {
        return secretKey.encoded
    }

    //store const value algorithm type
    companion object {
        private var ALGORITHM = "AES"
    }
}