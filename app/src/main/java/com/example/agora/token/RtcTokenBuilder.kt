package com.example.agora.token

import android.util.Base64
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.TreeMap
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class RtcTokenBuilder {

    enum class Role(val value: Int) {
        Role_Publisher(1),
        Role_Subscriber(2),
        Role_Admin(101)
    }

    fun buildTokenWithUid(
        appId: String,
        appCertificate: String,
        channelName: String,
        uid: Int,
        role: Role,
        privilegeTs: Int
    ): String {
        return buildTokenWithUserAccount(
            appId = appId,
            appCertificate = appCertificate,
            channelName = channelName,
            account = if (uid == 0) "" else uid.toString(),
            role = role,
            privilegeTs = privilegeTs
        )
    }

    fun buildTokenWithUserAccount(
        appId: String,
        appCertificate: String,
        channelName: String,
        account: String,
        role: Role,
        privilegeTs: Int
    ): String {
        val token = AccessToken(appId, appCertificate, channelName, account)
        token.addPrivilege(AccessToken.Privileges.kJoinChannel, privilegeTs)
        if (role == Role.Role_Publisher || role == Role.Role_Admin) {
            token.addPrivilege(AccessToken.Privileges.kPublishAudioStream, privilegeTs)
            token.addPrivilege(AccessToken.Privileges.kPublishVideoStream, privilegeTs)
            token.addPrivilege(AccessToken.Privileges.kPublishDataStream, privilegeTs)
        }
        return token.build()
    }
}

class AccessToken(
    val appId: String,
    val appCertificate: String,
    val channelName: String,
    val uid: String
) {
    enum class Privileges(val value: Short) {
        kJoinChannel(1),
        kPublishAudioStream(2),
        kPublishVideoStream(3),
        kPublishDataStream(4)
    }

    private val messages = TreeMap<Short, Int>()
    private val salt = (Math.random() * 0xFFFFFFFFL).toLong().toInt()
    private val ts = (System.currentTimeMillis() / 1000 + 24 * 3600).toInt()

    fun addPrivilege(privilege: Privileges, expireTimestamp: Int) {
        messages[privilege.value] = expireTimestamp
    }

    fun build(): String {
        if (appId.isEmpty() || appCertificate.isEmpty()) {
            return ""
        }

        val msgBytes = packMessages()
        val signature = generateSignature(appCertificate, appId, channelName, uid, msgBytes)

        val content = ByteArrayOutputStream()
        content.write(signature)
        content.write(packCrc(channelName))
        content.write(packCrc(uid))
        content.write(msgBytes)

        val encoded = Base64.encodeToString(content.toByteArray(), Base64.NO_WRAP)
        return "006$appId$encoded"
    }

    private fun packMessages(): ByteArray {
        val baos = ByteArrayOutputStream()
        baos.write(packInt(salt))
        baos.write(packInt(ts))
        baos.write(packShort(messages.size.toShort()))
        for ((key, value) in messages) {
            baos.write(packShort(key))
            baos.write(packInt(value))
        }
        return baos.toByteArray()
    }

    private fun generateSignature(
        appCertificate: String,
        appId: String,
        channelName: String,
        uid: String,
        msgBytes: ByteArray
    ): ByteArray {
        val baos = ByteArrayOutputStream()
        baos.write(appId.toByteArray(Charsets.UTF_8))
        baos.write(channelName.toByteArray(Charsets.UTF_8))
        baos.write(uid.toByteArray(Charsets.UTF_8))
        baos.write(msgBytes)

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(appCertificate.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return mac.doFinal(baos.toByteArray())
    }

    private fun packCrc(str: String): ByteArray {
        val crc = crc32(str.toByteArray(Charsets.UTF_8))
        return packInt(crc)
    }

    private fun crc32(bytes: ByteArray): Int {
        val checksum = java.util.zip.CRC32()
        checksum.update(bytes)
        return checksum.value.toInt()
    }

    private fun packInt(v: Int): ByteArray {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(v).array()
    }

    private fun packShort(v: Short): ByteArray {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(v).array()
    }
}
