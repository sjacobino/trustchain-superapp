package nl.tudelft.trustchain.datavault.accesscontrol

import android.util.Log
import kotlinx.coroutines.*
import nl.tudelft.ipv8.Peer
import nl.tudelft.ipv8.attestation.wallet.AttestationBlob
import nl.tudelft.ipv8.attestation.wallet.AttestationCommunity
import nl.tudelft.ipv8.util.sha1
import nl.tudelft.ipv8.util.toHex
import nl.tudelft.trustchain.common.ebsi.JWTHelper
import nl.tudelft.trustchain.datavault.community.DataVaultCommunity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class AccessControlFile(
    private val file: File,
    private val dataVaultCommunity: DataVaultCommunity?,
    private val attestationCommunity: AttestationCommunity?
    ) {

    private val logTag = "ACL"
    private var aclCache: JSONObject? = null // Cached acl. Use getACL()
    private val aclFile: File get() {
        return File(file.absolutePath + ".acl")
    }
    val lastModified: String get() {
        return getACL().getString(LAST_CHANGED)
    }

    fun verifyAccess(accessMode: String, accessTokenType: Policy.AccessTokenType, accessTokens: List<Any>) : Boolean {
        if (!isPublic() || dataVaultCommunity?.vaultBrowserFragment?.PERFORMANCE_TEST == true) {
            Log.e(logTag, "Verifying $accessMode access for att $accessTokenType")

            when (accessTokenType) {
                Policy.AccessTokenType.SESSION_TOKEN -> {
                    val sessionToken = accessTokens[0] as String
                    val aft = DirectoryTree.parse(SessionToken.getAFT(sessionToken))
                    return aft.contains(file.path)
                }
                Policy.AccessTokenType.TCID -> {
                    val policies = getPolicies(accessMode)
                    val candidateAttestations = accessTokens.map { at -> at as AttestationBlob }

                    policies.forEach { policy ->
                        if (policy.evaluate(accessMode, candidateAttestations)) {
                            return true
                        }
                    }
                }
                Policy.AccessTokenType.JWT -> {
                    accessTokens.forEach { jwt ->
                        val policies = getPolicies(accessMode)
                        policies.forEach { policy ->
                            if (policy.evaluate(accessMode, jwt as String)) {
                                return true
                            }
                        }
                    }
                }
                Policy.AccessTokenType.JSONLD -> {
                    Log.e(logTag, "Access token type $accessTokenType not implemented")
//                    todo return false or implement
                    return false
                }
            }

            return false
        }

        return true
    }

    private fun getACL(): JSONObject {
        if (aclCache == null) {
            aclCache = if (aclFile.exists()) {
                try {
                    val rd = aclFile.readText()
                    Log.d(logTag, "ACL contents: $rd")
                    JSONObject(rd)
                } catch (e: Exception) {
                    Log.d(logTag, "Corrupt ACL file: ${file.absolutePath}", e)
                    newACL()
                }
            } else {
                Log.d(logTag, "No acl file yet")
                newACL()
            }
        }

        return aclCache!!
    }

    fun getPolicies(): List<Policy> {
        return try{
            val policiesJSON = getACL().getJSONArray(POLICIES).noLastNull()
            val policies = mutableListOf<Policy>()

            for (i in 0 until policiesJSON.length()) {
                policies.add(Policy(policiesJSON.getJSONObject(i), attestationCommunity))
            }

            policies
        } catch (_: Exception) {
            Log.e(logTag, "Corrupt ACL file: ${file.absolutePath}")
            listOf()
        }
    }

    fun getPolicies(accessMode: String): List<Policy> {
        return getPolicies().filter {
            it.accessMode == accessMode
        }
    }

    fun savePolicies(policies: List<Policy>) {
        CoroutineScope(Dispatchers.IO).launch {
            val acl = getACL()

            val timestamp = Date().time
            val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val lastChanged: String = sdf.format(timestamp)

            acl.put(LAST_CHANGED, lastChanged)
            acl.put(POLICIES, toJSONArray(policies))

            val fos = FileOutputStream (aclFile)
            fos.write(acl.toString().toByteArray())
        }
    }

    private fun newACL(): JSONObject {
        val newACL = JSONObject()
        newACL.put(VERSION, AP_VERSION)
        newACL.put(RESOURCE, file.absolutePath)
        val timestamp = Date().time
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        newACL.put(LAST_CHANGED, sdf.format(timestamp))
        newACL.put(POLICIES, JSONArray())
        return newACL

//        return JSONObject(TEST_ACL)
    }

    fun isPublic(): Boolean {
//        TODO
        if (1==1) return true

        val acl = File(file.absolutePath + ".acl")
        if (acl.exists()) {
            val aclContent = acl.readText()
            return aclContent == TRUE
        }
        return false
    }

    fun setPublic(isPublic: Boolean) {
        Log.e(logTag, "setPublic: $isPublic")
        /*val acl = File(file.absolutePath + ".acl")
        val fos = FileOutputStream (acl)
        if (isPublic) {
            fos.write(TRUE.toByteArray())
        } else {
            fos.write(FALSE.toByteArray())
        }
        fos.close()*/
    }

    companion object {
        const val AP_VERSION = "1.0"
        const val DATE_FORMAT = "dd-MM-yyyy HH:mm:ss"

        const val TRUE = "TRUE"
        const val FALSE = "FALSE"

        const val VERSION = "version"
        const val RESOURCE = "resource "
        const val LAST_CHANGED = "last_changed"
        const val POLICIES = "policies"

        const val TEST_ACL = """{
	"version": 1.0,
	"resource": "photo2.jpg",
	"last_changed": "01-02-2022",
	"policies": [
		{
			"mode": "read",
			"rules": [{"issuer": "IG-SSI:123456789"}]
		},
		{
            "active": true,
			"mode": "read+write",
			"rules": [
				[{"attribute": "IG-SSI:id_metadata_range_18plus:ageIG-SSI:id_metadata_range_18plus:age", "value": "18+", "trusted_authority": true}],
				"AND",
				[
                {"attribute": "IG-SSI:id_metadata:city", "value": "Delft", "trusted_authority": false},
                "OR",
                [{"issuer": "IG-SSI:123456789"}]]
			]
		},
	]
}
"""
        fun filterAttestations(peer: Peer, attestations: List<AttestationBlob>?): List<AttestationBlob>? {

            return attestations?.filter {
                if (it.metadata == null || it.attestorKey == null || it.signature == null) {
                    Log.e("PerfTest", "Missing data")
                    false
                } else {
                    val parsedMetadata = JSONObject(it.metadata!!)
                    val attesteeKeyHash = parsedMetadata.optString("trustchain_address_hash")
                    val isOwner = peer.publicKey.keyToHash().toHex() == attesteeKeyHash
                    val isSignatureValid = it.attestorKey!!.verify(it.signature!!,
                        sha1(it.attestationHash + it.metadata!!.toByteArray())
                    )

                    isOwner && isSignatureValid
                }
            }
        }

        private fun toJSONArray(policies: List<Policy>): JSONArray {
            val jsonArray = JSONArray()
            policies.forEach {
                jsonArray.put(it.getPolicyJSON())
            }
            return jsonArray
        }
    }
}

/*
{
	"version": 1.0,
	"resource": "photo2.jpg",
	"last_changed": "01-02-2022",
	"policies": [
		{
			"mode": "read+write",
			"rules": [
				{"attribute": "IG-SSI:id_metadata_range_18plus:age", "value": "18+", "trusted_authority": true},
				"AND",
				{"attribute": "IG-SSI:id_metadata:city", "value": "Delft", "trusted_authority": false}
			]
		},
		{
			"mode": "read",
			"rules": [{"issuer": "IG-SSI:123456789"}]
		},
	]
}
 */
