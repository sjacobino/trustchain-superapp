package nl.tudelft.trustchain.common.ebsi

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.android.volley.Response
import id.walt.auditor.Auditor
import id.walt.auditor.JsonSchemaPolicy
import id.walt.auditor.SignaturePolicy
import id.walt.custodian.Custodian
import id.walt.model.DidMethod
import id.walt.servicematrix.ServiceMatrix
import id.walt.services.did.DidService
import id.walt.signatory.ProofConfig
import id.walt.signatory.ProofType
import id.walt.signatory.Signatory
import kotlinx.coroutines.*
import nl.tudelft.ipv8.util.toHex
import nl.tudelft.trustchain.common.R
import org.json.JSONArray
import org.json.JSONObject
import java.security.Security
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class ConformanceTest(
    private val context: Context,
    private val uuid: UUID
) {
    val TAG = ConformanceTest::class.simpleName!!
    val wallet = EBSIWallet(context)
    val defaultListener = Response.Listener<JSONObject> {
        Log.e(TAG, it.toString())
    }

    private val errorListener = Response.ErrorListener {
        val myVolleyError = it as MyVolleyError
        Log.e(TAG, "Api error during conformance test (${myVolleyError.url})")
//        Log.e(TAG, myVolleyError.volleyError?.networkResponse?.allHeaders?.map { h -> "${h.name}: ${h.value}, " }?.reduce { acc, s -> acc + s } ?: "No headers")
        Log.e(TAG, myVolleyError.volleyError?.networkResponse?.data?.toString(Charsets.UTF_8) ?: "No network response")
//        Log.e(TAG, myVolleyError.volleyError?.message ?: "No message")
    }

    fun waltIdTest() {
        ServiceMatrix(context, "service-matrix.properties")

        val issuerDid = DidService.create(DidMethod.ebsi)
        val holderDid = DidService.create(DidMethod.key)

        Log.e("WaltID Test", "Issuer: $issuerDid\nHolder: $holderDid")

        val expiration = Instant.now().plus(30, ChronoUnit.DAYS)

        Log.e("WaltID Test", "expiration: $expiration")

        // Issue VC in JSON-LD and JWT format (for show-casing both formats)
        val vcJsonLd = Signatory.getService().issue("VerifiableId", ProofConfig(issuerDid = issuerDid, subjectDid = holderDid, proofType = ProofType.LD_PROOF, expirationDate = expiration))
        val vcJwt = Signatory.getService().issue("VerifiableId", ProofConfig(issuerDid = issuerDid, subjectDid = holderDid, proofType = ProofType.JWT, expirationDate = expiration))

        Log.e("WaltID Test", "VC JsonLD: $vcJsonLd")
        Log.e("WaltID Test", "VC JWT: $vcJwt")

        // Present VC in JSON-LD and JWT format (for show-casing both formats)
        // expiration date is not needed when JSON-LD format
        val vpJsonLd = Custodian.getService().createPresentation(listOf(vcJsonLd), holderDid, expirationDate = null)
        val vpJwt = Custodian.getService().createPresentation(listOf(vcJwt), holderDid, expirationDate = expiration)

        Log.e("WaltID Test", "VP JsonLD: $vpJsonLd")
        Log.e("WaltID Test", "VP JWT: $vpJwt")

        // Verify VPs, using Signature, JsonSchema and a custom policy
//        val resJsonLd = Auditor.getService().verify(vpJsonLd, listOf(SignaturePolicy(), JsonSchemaPolicy()))
        val resJsonLd = Auditor.getService().verify(vpJsonLd, listOf(SignaturePolicy()))
//        val resJwt = Auditor.getService().verify(vpJwt, listOf(SignaturePolicy(), JsonSchemaPolicy()))
        val resJwt = Auditor.getService().verify(vpJwt, listOf(SignaturePolicy()))

        Log.e("Ver result", "JSON-LD verification result: ${resJsonLd.valid}")
        Log.e("Ver result", "JWT verification result: ${resJwt.valid}")
    }

    fun run(){
        Log.e(TAG, "Conformance Test: $uuid")
        // https://ec.europa.eu/digital-building-blocks/wikis/display/EBSIDOC/EBSI+Wallet+Conformance+Testing
        EBSIRequest.testSetup(uuid)

        waltIdTest()

//        onboardTest12() // get verifiable authorisation

        // onboarding 1&2 can be skipped if already performed and move on to onboarding 5
//        onboardTest5() // get access token

        // wait for access token
        CoroutineScope(Dispatchers.IO).launch {
            for (i in 1 until 5) {
                delay(2000)
//                Log.e("RegDID", "Waited ${i * 2} seconds on access token: ${wallet.accessToken}")
                if (wallet.accessToken != null) {
                    withContext(Dispatchers.Main) {
//                        onboardTest6()
                    }
                    break
                }
            }
        }

//         vaRequestTest1()

    }

    private fun onboardTest12() {
        // Let the user scan the mobile authentication token on the onboarding service page
        val onboardSessionToken = "eyJhbGciOiJFUzI1NksiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjE2NTU4Mzc1OTcsImlhdCI6MTY1NTgzNjY5NywiaXNzIjoiZGlkOmVic2k6emNHdnFnWlRIQ3Rramd0Y0tSTDdIOGsiLCJvbmJvYXJkaW5nIjoicmVjYXB0Y2hhIiwidmFsaWRhdGVkSW5mbyI6eyJhY3Rpb24iOiJsb2dpbiIsImNoYWxsZW5nZV90cyI6IjIwMjItMDYtMjFUMTg6Mzg6MTZaIiwiaG9zdG5hbWUiOiJhcHAuY29uZm9ybWFuY2UuaW50ZWJzaS54eXoiLCJzY29yZSI6MC45LCJzdWNjZXNzIjp0cnVlfX0.r0fk38pGIXYkseBeouXgGwxRtrSehLV_m4H_Vv-qQeRuROGFuyrQm8kO1_gS9uRLJGryYn2M_1KseE2WFs-HXg"
        OnboardingTools.getVerifiableAuthorisation(wallet, onboardSessionToken, errorListener)
    }

    private fun onboardTest5() {
        AuthorisationTools.getAccessToken(wallet, errorListener)
    }

    private fun onboardTest6() {
        DIDTools.registerDID(wallet, errorListener)
    }

    private fun vaRequestTest1() {
        val api = "conformance/v1/issuer-mock/authorize"

        val credentials = JSONArray().apply {
            put(JSONObject().apply {
                put("type", "https://api.preprod.ebsi.eu/trusted-schemas-registry/v1/schemas/0x61cc4bd80d14c81778b3eb59905c8929f44a7d45e9ae6930f3bd63b98bd0557f")
                put("format", "jwt_vc")
            })

            put(JSONObject().apply {
                put("type", "https://api.preprod.ebsi.eu/trusted-schemas-registry/v1/schemas/0x1ee207961aba4a8ba018bacf3aaa338df9884d52e993468297e775a585abe4d8")
                put("format", "jwt_vc")
            })
        }

        val claims = JSONObject().put("credential", credentials)

        val params = mutableMapOf<String, String>().apply {
            put("scope", "openid")
            put("response_type", "id_token")
            put("redirect_uri", "") // empty string is regular json response
            put("client_id", "")
            put("state", "12345") //
            put("nonce", kotlin.random.Random.nextBytes(8).toHex())
            put("claims", claims.toString())
        }

        // ISSUE_011
        val conformanceServer = "https://api.conformance.intebsi.xyz"
        Log.e(TAG, "Requesting VA")
        EBSIAPI.requestVerifiableCredentialAuthorisation(api, params,
            {
                val tokenApi = "conformance/v1/issuer-mock/token"

                // val state = it.getString("state")
                val code = it.getString("code")

                // ISSUE_021
                EBSIAPI.getToken(tokenApi, code, defaultListener, errorListener, redirect = conformanceServer)
            }, errorListener, redirect = conformanceServer)
    }

    companion object {

        fun addLeading0x(s: String): String {
            return "0x$s"
        }

        fun removeLeading0x(s: String): String {
            return if (s.startsWith("0x")) {
                s.substring(2)
            } else {
                s
            }
        }

        fun printJSONObjectItems(tag: String, jsonObject: JSONObject) {
            for (key in jsonObject.keys()) {
                Log.e(tag, "$key: ${jsonObject.get(key)}")
            }
        }
    }
}
