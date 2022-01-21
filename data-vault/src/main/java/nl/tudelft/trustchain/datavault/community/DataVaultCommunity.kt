package nl.tudelft.trustchain.datavault.community

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import mu.KotlinLogging
import nl.tudelft.ipv8.Community
import nl.tudelft.ipv8.Overlay
import nl.tudelft.ipv8.Peer
import nl.tudelft.ipv8.android.IPv8Android
import nl.tudelft.ipv8.attestation.wallet.AttestationBlob
import nl.tudelft.ipv8.attestation.wallet.AttestationCommunity
import nl.tudelft.ipv8.keyvault.PrivateKey
import nl.tudelft.ipv8.messaging.Packet
import nl.tudelft.ipv8.util.toHex
import nl.tudelft.trustchain.datavault.DataVaultMainActivity
import nl.tudelft.trustchain.datavault.accesscontrol.AccessPolicy
import nl.tudelft.trustchain.datavault.ui.VaultBrowserFragment
import nl.tudelft.trustchain.peerchat.community.AttachmentPayload
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset

private val logger = KotlinLogging.logger {}

class DataVaultCommunity(private val context: Context) : Community() {
    override val serviceId = "3f0dd0ab2c774be3bd1b7bf2d8dddc2938dd18fa"

    private val logTag = "DataVaultCommunity"

    private lateinit var dataVaultMainActivity: DataVaultMainActivity
    private lateinit var vaultBrowserFragment: VaultBrowserFragment
    val attestationCommunity: AttestationCommunity by lazy {
        IPv8Android.getInstance().getOverlay<AttestationCommunity>()!!
    }

    val VAULT by lazy { File(context.filesDir, VaultBrowserFragment.VAULT_DIR) }

    init {
        messageHandlers[MessageId.FILE] = ::onFilePacket
        messageHandlers[MessageId.FILE_REQUEST] = ::onFileRequestPacket
        messageHandlers[MessageId.FILE_REQUEST_FAILED] = ::onFileRequestFailedPacket
        messageHandlers[MessageId.ACCESSIBLE_FILES_REQUEST] = ::onAccessibleFilesRequestPacket
        messageHandlers[MessageId.ACCESSIBLE_FILES] = ::onAccessibleFilesPacket
    }

    private fun onFilePacket(packet: Packet) {
        val (_, payload) = packet.getDecryptedAuthPayload(
            AttachmentPayload.Deserializer, myPeer.key as PrivateKey
        )
        logger.debug { "<- $payload" }
        onFile(payload)
    }

    private fun onAccessibleFilesPacket(packet: Packet) {
        val (peer, payload) = packet.getAuthPayload(AccessibleFilesPayload.Deserializer)
        logger.debug { "<- $payload" }
        onAccessibleFiles(peer, payload)
    }

    private fun onFile(payload: AttachmentPayload) {
        val message = payload.data.toString(Charset.defaultCharset())
        notify(payload.id, message)

        // Store file locally
        /*val (file, _) = vaultFile(payload.id)
        Log.e(logTag, "Received file: ${file.absolutePath}")
        // Save attachment
        val fos = FileOutputStream(file)
        fos.write(payload.data)
        fos.close()*/
    }

    private fun onAccessibleFiles(peer: Peer, payload: AccessibleFilesPayload) {
        Log.e("ACCESSIBLE FILES", "Token: ${payload.accessToken}, files: ${payload.files}")

        if (!::vaultBrowserFragment.isInitialized) {
            return
        }
        vaultBrowserFragment.selectRequestableFile(peer, payload.accessToken, payload.files)
    }

    private fun onFileRequestPacket(packet: Packet) {
        val (peer, payload) = packet.getAuthPayload(VaultFileRequestPayload.Deserializer)
        logger.debug { "<- $payload" }
        onFileRequest(peer, payload)
    }

    private fun onAccessibleFilesRequestPacket(packet: Packet) {
        val (peer, payload) = packet.getAuthPayload(VaultFileRequestPayload.Deserializer)
        logger.debug { "<- $payload" }
        onAccessibleFilesRequest(peer, payload)
    }

    private fun onFileRequestFailedPacket(packet: Packet) {
        val (_, payload) = packet.getAuthPayload(VaultFileRequestFailedPayload.Deserializer)
        logger.debug { "<- $payload" }
        onFileRequestFailed(payload)
    }

    private fun onFileRequestFailed(payload: VaultFileRequestFailedPayload) {
        Log.e(logTag, payload.message)
        //Toast.makeText(context, payload.message, Toast.LENGTH_LONG).show()
        notify("Failed", payload.message)
    }

    private fun vaultFile(filename: String): Pair<File, AccessPolicy> {
        val VAULT = File(context.filesDir, VaultBrowserFragment.VAULT_DIR)
        val file = File(VAULT, filename)
        return Pair(file, AccessPolicy(file, attestationCommunity))
    }

    private fun onFileRequest(peer: Peer, payload: VaultFileRequestPayload) {
        Log.e(logTag, "Received file request. Access token: ${payload.accessToken}")
        try {
            val (file, accessPolicy) = vaultFile(payload.id)
            if (!file.exists()) {
                Log.e(logTag, "The requested file does not exist")
                sendFileRequestFailed(peer, payload.id, "The requested file does not exist")
            } else if (!accessPolicy.verifyAccess(peer, payload.accessToken, payload.attestations)) {
                Log.e(logTag, "Access Policy not met")
                sendFileRequestFailed(peer, payload.id, "Access Policy not met")
                //sendFile(peer, payload.id, file)
            } else {
                sendFile(peer, payload.id, file)
                logger.debug { "$peer.mid" }
            }
        } catch (e: SQLiteConstraintException) {
            e.printStackTrace()
        }
    }

    private fun onAccessibleFilesRequest(peer: Peer, payload: VaultFileRequestPayload) {
        var allFiles = VAULT.list()?.asList()
        if (payload.accessToken != null) {
            Log.e(logTag, "Access token for accessible files (${payload.accessToken})")
            // Currently accessibleFilesRequest don't contain an access token, only attestations
            sendAccessibleFiles(peer, payload.accessToken, filterFiles(peer, payload.accessToken, null, allFiles))
        } else if (payload.attestations != null && !payload.attestations.isEmpty()) {
            Log.e(logTag, "Attestations for accessible files (${payload.attestations.size} att(s))")
            for (attestation in payload.attestations) {
                Log.e(logTag, "attestation: ${attestation.attestationHash.toString(Charset.defaultCharset())} from peer: ${peer.publicKey.keyToBin().toHex()}")
                // VERIFIY ATTESTATIONS & FILTER FILES
            }

            sendAccessibleFiles(peer, "TEMP_TOKEN", filterFiles(peer, null, payload.attestations, allFiles))
        } else {
            Log.e(logTag, "No credentials. Check public files")
            // no credentials
            sendAccessibleFiles(peer, "", filterFiles(peer, null, null, allFiles))
        }
    }

    private fun filterFiles(peer: Peer, accessToken: String?, attestations: List<AttestationBlob>?, files: List<String>?): List<String>{
        if (files != null) {
            return files.filter { fileName -> !fileName.startsWith(".") && !fileName.endsWith(".acl") }.
                filter { fileName ->
                    val (_, accessPolicy) = vaultFile(fileName)
                    accessPolicy.verifyAccess(peer, accessToken, attestations)
                }
        }

        return listOf()
    }

    fun sendFile(peer: Peer, id: String, file: File) {
        Log.e(logTag, "Sending file")
        val payload = AttachmentPayload(id, file.readBytes())
        val packet =
            serializePacket(MessageId.FILE, payload, encrypt = true, recipient = peer)
        logger.debug { "-> $payload" }
        send(peer, packet)
    }

    fun sendAccessibleFiles(peer: Peer, accessToken: String, files: List<String>?) {
        Log.e(logTag, "Sending ${files?.size ?: 0} file(s) to ${peer.publicKey.keyToBin().toHex()}")
        val payload = AccessibleFilesPayload(accessToken, files ?: listOf())
        val packet = serializePacket(MessageId.ACCESSIBLE_FILES, payload)
        logger.debug { "-> $payload" }
        send(peer, packet)
    }

    fun sendAccessibleFilesRequest(peer: Peer, attestations: List<AttestationBlob>) {
        Log.e(logTag, "Sending accessible files request")
        Log.e(logTag, "including ${attestations.size} attestation(s)")
        val payload = VaultFileRequestPayload("ACCFILES", null, attestations = attestations)
        val packet = serializePacket(MessageId.ACCESSIBLE_FILES_REQUEST, payload)
        logger.debug { "-> $payload" }
        send(peer, packet)
    }

    fun sendFileRequest(peer: Peer, id: String, accessToken: String? = null, attestations: List<AttestationBlob>? = null) {
        Log.e(logTag, "Sending file request")
        Log.e(logTag, "accessToken: $accessToken, including ${attestations?.size ?: 0} attestation(s)")

        val payload = VaultFileRequestPayload(id, accessToken, attestations)
        val packet = serializePacket(MessageId.FILE_REQUEST, payload)
        logger.debug { "-> $payload" }
        send(peer, packet)
    }

    private fun sendFileRequestFailed(peer: Peer, id: String, message: String) {
        Log.e(logTag, "Sending file request failed")
        val payload = VaultFileRequestFailedPayload("Vault file request $id: $message")
        val packet = serializePacket(MessageId.FILE_REQUEST_FAILED, payload)
        logger.debug { "-> $payload" }
        send(peer, packet)
    }

    private fun notify(id: String, message: String) {
        if (!::vaultBrowserFragment.isInitialized) {
            return
        }
        vaultBrowserFragment.notify(id, message)
    }

    fun setDataVaultActivity(activity: DataVaultMainActivity) {
        dataVaultMainActivity = activity
    }

    fun setVaultBrowserFragment(fragment: VaultBrowserFragment) {
        vaultBrowserFragment = fragment
    }

    object MessageId {
        const val FILE = 11
        const val FILE_REQUEST = 12
        const val FILE_REQUEST_FAILED = 13
        const val ACCESSIBLE_FILES_REQUEST = 14
        const val ACCESSIBLE_FILES = 15
    }

    class Factory(
        private val context: Context,
    ) : Overlay.Factory<DataVaultCommunity>(DataVaultCommunity::class.java) {
        override fun create(): DataVaultCommunity {
            return DataVaultCommunity(context)
        }
    }
}
