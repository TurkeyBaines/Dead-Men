package io.dm.net

import net.rsprot.crypto.rsa.RsaKeyPair
import java.io.File
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateCrtKey
import java.util.Properties

/**
 * Loads the server's private RSA key (used to decrypt the client's login block), or
 * generates and persists a fresh 1024-bit key on first run.
 *
 * NOTE: the *public* half (modulus + public exponent) must be embedded into the client
 * you ship. If you generate a new key here you MUST update the client's login key.
 * The legacy stack kept this key inside the Kronos client; see MIGRATION.md "RSA".
 */
object RsaKeys {

    fun loadOrGenerate(file: File): RsaKeyPair {
        if (file.exists()) {
            val props = Properties().apply { file.inputStream().use { load(it) } }
            val modulus = BigInteger(props.getProperty("modulus"))
            val privateExponent = BigInteger(props.getProperty("exponent"))
            return RsaKeyPair(privateExponent, modulus) // RSProt ctor is (exponent, modulus)
        }
        val generator = KeyPairGenerator.getInstance("RSA").apply { initialize(1024) }
        val key = generator.generateKeyPair().private as RSAPrivateCrtKey
        val props = Properties().apply {
            setProperty("modulus", key.modulus.toString())
            setProperty("exponent", key.privateExponent.toString())
            setProperty("publicExponent", key.publicExponent.toString())
        }
        file.parentFile?.mkdirs()
        file.outputStream().use { props.store(it, "Dead-Men RSProt login key. Public exponent belongs in the client.") }
        return RsaKeyPair(key.privateExponent, key.modulus) // RSProt ctor is (exponent, modulus)
    }
}
