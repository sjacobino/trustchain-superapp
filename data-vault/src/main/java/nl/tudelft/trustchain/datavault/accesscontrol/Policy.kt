package nl.tudelft.trustchain.datavault.accesscontrol

import nl.tudelft.ipv8.attestation.wallet.AttestationBlob
import nl.tudelft.ipv8.attestation.wallet.AttestationCommunity
import org.json.JSONObject

class Policy(
    private var policy: JSONObject,
    private val attestationCommunity: AttestationCommunity?
) {
    private val logTag = "Policy"

    fun evaluate(accessMode: String, credentialTokens: Any): Boolean {
        if (!checkAccessMode(accessMode)) return false

        return rules.evaluate(credentialTokens)
    }

    private fun checkAccessMode(requestMode: String): Boolean {
        val policyModes = accessMode.split("+")
        return policyModes.contains(requestMode)
    }

    fun getPolicyJSON(): JSONObject {
        return policy
    }

    fun setActive(active: Boolean) {
        policy.put(ACTIVE, active)
    }

    fun setRules(rules: Rules) {
        policy.put(RULES, rules.rulesContainer)
    }

    val isActive: Boolean get() {
        return policy.optBoolean(ACTIVE)
    }

    val accessMode: String get() {
        return policy.getString(MODE)
    }

    val rules: Rules get() {
        return Rules(policy.getJSONArray(RULES).noLastNull(), attestationCommunity)
    }

    companion object {
        const val ACTIVE = "active"
        const val MODE = "mode"
        const val RULES = "rules"

        const val READ = "read"
        const val WRITE = "write"
        const val APPEND = "append"
    }

    enum class AccessTokenType {
        SESSION_TOKEN, TCID, JWT, JSONLD
    }

    object ATT {
        const val SESSION_TOKEN = "session_token"
        const val TCID = "tcid"
        const val JWT = "jwt"
        const val JSONLD = "JSONLD"
    }
}
