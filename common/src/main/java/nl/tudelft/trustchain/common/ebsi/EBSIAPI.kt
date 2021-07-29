package nl.tudelft.trustchain.common.ebsi

import android.content.Context
import android.util.Base64
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

open class EBSIAPI {
    companion object {
        val server = "https://api.ebsi.xyz"
        private lateinit var queue: RequestQueue

        fun get(api: String, path: String?, params: String?, onSuccess: (String) -> Unit, onError: (VolleyError) -> Unit) {
            var url = "$server/$api"
            if (path != null) {
                url += "/$path"
            }
            if (params != null) {
                url += "?$params"
            }

            val stringRequest = StringRequest(Request.Method.GET, url,
                { response ->
                    onSuccess(response)
                },
                {
                    onError(it)
                })

            queue.add(stringRequest)
        }

        fun post(api: String, path: String?, body: String, onSuccess: (String) -> Unit, onError: (VolleyError) -> Unit) {
            var url = "$server/$api"
            if (path != null) {
                url += "/$path"
            }

            val stringRequest = object: StringRequest(Method.POST, url,
                { response ->
                    onSuccess(response)
                },
                {
                    onError(it)
                }){
                override fun getBody(): ByteArray {
                    return body.toByteArray(Charsets.UTF_8)
                }

                override fun getBodyContentType(): String {
                    return "application/json"
                }
            }
            queue.add(stringRequest)
        }

        fun put(api: String, path: String?, body: String, onSuccess: (String) -> Unit, onError: (VolleyError) -> Unit) {
            var url = "$server/$api"
            if (path != null) {
                url += "/$path"
            }

            val stringRequest = object: StringRequest(Method.PUT, url,
                { response ->
                    onSuccess(response)
                },
                {
                    onError(it)
                }){
                override fun getBody(): ByteArray {
                    return body.toByteArray(Charsets.UTF_8)
                }

                override fun getBodyContentType(): String {
                    return "application/json"
                }
            }
            queue.add(stringRequest)
        }

        fun init(context: Context) {
            if (!::queue.isInitialized) {
                queue = Volley.newRequestQueue(context)!!
            }
        }

        fun logError(tag: String, error: VolleyError) {
            Log.e("$tag (data)", String(error.networkResponse.data, Charsets.UTF_8))
            error.networkResponse.allHeaders?.forEach {
                Log.e("$tag (header)", "${it.name}: ${it.value}")
            }
            error.printStackTrace()
        }

        fun URLEncodeParams(paramsMap: Map<String, String?>) : String {
            return paramsMap.filterValues { s -> !s.isNullOrEmpty() }.
            entries.joinToString(separator="&", transform = {pair ->
                "${URLEncoder.encode(pair.component1(), "UTF-8")}=${URLEncoder.encode(pair.component2(), "UTF-8")}" })
        }

        fun listToJSONArray(list: List<String>) : JSONArray {
            val jsonArray = JSONArray()
            list.forEach{s -> jsonArray.put(s)}
            return jsonArray
        }

        fun mapToJSONObject(map: Map<String, String>) : JSONObject{
            val jsonObject = JSONObject()
            map.forEach{ (k, v) -> jsonObject.put(k, v)}
            return jsonObject
        }

        fun test(context: Context) {
            init(context)

            //DID.getDID("did:ebsi:0xec457d0a974c48d5685a7efa03d137dc8bbde7e3")

            /*Wallet.authenticate("urn:ietf:params:oauth:grant-type:jwt-bearer",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
                "ebsi profile user")
            val jws = Wallet.sign("did:ebsi:0xB551b70d650892d23dE3Be201A95c1FcBea98A3D",
                "{payload-for-signing}",
                "EcdsaSecp256k1Signature2019")
            if (jws != null) {
                Wallet.validate(jws)
            }*/

            /*IdentityHub.getAttributes("did:ebsi:0x416e6e6162656c2e4c65652e452d412d506f652e", "%5B%22EssifVerifiableID%22%2C%22EuropassDiploma%22%5D")
            IdentityHub.storeAttribute("0x7ee0d94aab0e4f36eae85127056de08433591304a83ff8801bb9212b624f1321",
                "cred-7ee0d94aab0e4f36eae85127056de084",
                listOf("VerifiableCredential", "EssifVerifiableID"),
                "Verifiable ID",
                "<bytes for base64>".toByteArray(Charsets.UTF_8))
            IdentityHub.getAttribute("0x7ee0d94aab0e4f36eae85127056de08433591304a83ff8801bb9212b624f1321")*/

            /*TrustedIssuersRegistry.getIssuers()
            TrustedIssuersRegistry.getIssuer("did:ebsi:0x464190367BE948210608a46847bed183607f685A")*/

            /*VerifiableCredential.createCredential(listOf("VerifiableCredential", "EssifVerifiableID"),
                "did:ebsi:0xec457d0a974c48d5685a7efa03d137dc8bbde7e3",
                mapOf(
                    "id" to "did:ebsi:evas-did",
                    "personIdentifier" to "BE/BE/02635542Y",
                    "currentFamilyName" to "Eva",
                    "currentGivenName" to "Adams",
                    "birthName" to "Eva",
                    "dateOfBirth" to "1998-02-14",
                    "placeOfBirth" to "Brussels",
                    "currentAddress" to "44, rue de Fame",
                    "gender" to "Female"))

            VerifiableCredential.validateCredential(listOf("VerifiableCredential", "EssifVerifiableID"),
                "did:ebsi:0xec457d0a974c48d5685a7efa03d137dc8bbde7e3",
                mapOf(
                    "id" to "did:ebsi:evas-did",
                    "personIdentifier" to "BE/BE/02635542Y",
                    "currentFamilyName" to "Eva",
                    "currentGivenName" to "Adams",
                    "birthName" to "Eva",
                    "dateOfBirth" to "1998-02-14",
                    "placeOfBirth" to "Brussels",
                    "currentAddress" to "44, rue de Fame",
                    "gender" to "Female"),
                mapOf(
                    "type" to "EidasSeal2019",
                    "created" to "2019-11-17T14:00:00Z",
                    "proofPurpose" to "assertionMethod",
                    "verificationMethod" to "did:ebsi:0x454253492d52554c45532d4d414c494e41524148#eidasKey",
                    "jws" to "eyJhbGciOiJFUzI1NksifQ.eyJzdWIiOiJFQlNJIDIwMTkifQ.oggE3ft3kJYPGGa9eBibpbjgeJXw4fLbVMouVoM2NfcDxsl_UUUIarsS1VpBoYEs7s9cBlc4uC0EbnJCHfVJIw"))*/

            /*VerifiablePresentation.createPresentation(listOf("CREDENTIAL-A", "CREDENTIAL-B"), "did:ebsi:0x416e6e6162656c2e4c65652e452d412d506f652e")
            VerifiablePresentation.validatePresentation("VerifiablePresentation", listOf("CREDENTIAL-A", "CREDENTIAL-B"),
                mapOf(
                    "type" to "EcdsaSecp256k1Signature2019",
                    "created" to "2019-06-22T14:11:44Z",
                    "proofPurpose" to "assertionMethod",
                    "verificationMethod" to "did:ebsi:0x16048B83FAdaCdCB20198ABc45562Df1A3e289aF#key-1",
                    "challenge" to "1f44d55f-f161-4938-a659-f8026467f126",
                    "domain" to "4jt78h47fh47",
                    "jws" to "eyJhbGciOiJFUzI1NksifQ.eyJzdWIiOiJFQlNJIDIwMTkifQ.oggE3ft3kJYPGGa9eBibpbjgeJXw4fLbVMouVoM2NfcDxsl_UUUIarsS1VpBoYEs7s9cBlc4uC0EbnJCHfVJIw"
                ))*/
        }
    }

    class DID: EBSIAPI() {
        companion object {
            const val api = "did/v1/identifiers"

            fun getDID(did: String) : JSONObject? {
                var DID: JSONObject? = null
                get(api, did, null, { response ->
                    //JSON response
                    Log.e("DIDAPI", response)
                    DID = JSONObject(response)
                }, { error ->
                    logError("DID.getDID", error)
                })
                return DID
            }
        }
    }

    class Wallet : EBSIAPI() {
        companion object {
            fun authenticate(grantType: String, assertion: String, scope: String) {
                val api = "wallet/v1/sessions"

                val jsonBody = JSONObject()
                jsonBody.put("grantType", grantType)
                jsonBody.put("assertion", assertion)
                jsonBody.put("scope", scope)
                
                post(api, null, jsonBody.toString(), { response ->
                    Log.e("Wallet.authenticate", response)
                }, { error ->
                    logError("Wallet.authenticate", error)
                })
            }

            fun sign(issuer: String, payload: String, type: String) : String?{
                val api = "wallet/v1/signatures"

                val jsonBody = JSONObject()
                jsonBody.put("issuer", issuer)
                jsonBody.put("payload", payload)
                jsonBody.put("type", type)

                var jws : String? = null

                post(api, null, jsonBody.toString(), { response ->
                    Log.e("Wallet.sign", response)
                    val jsonResponse = JSONObject(response)
                    jws = jsonResponse.getString("jws")
                }, { error ->
                    logError("Wallet.sign", error)
                })

                return jws
            }

            fun validate(signature: String) {
                val api = "wallet/v1/signature-validations"

                val jsonBody = JSONObject()
                jsonBody.put("jws", signature)

                post(api, null, jsonBody.toString(), { response ->
                    Log.e("Wallet.validate", response)
                }, { error ->
                    logError("Wallet.validate", error)
                })
            }
        }
    }

    class IdentityHub : EBSIAPI() {
        companion object {
            fun login(grantType: String, assertion: String) {
                val api = "identity-hub/v1/sessions"

                val jsonBody = JSONObject()
                jsonBody.put("grantType", grantType)
                jsonBody.put("assertion", assertion)

                post(api, null, jsonBody.toString(), { response ->
                    Log.e("IdentityHub.login", response)
                }, { error ->
                    logError("IdentityHub.login", error)
                })
            }

            /***
             * @param type must be url encoded
             */
            fun getAttributes(did: String, type: String? = null, pageBefore: String? = null, pageAfter: String? = null, pageSize: String? = null) : JSONObject? {
                val api = "identity-hub/v1/attributes"
                var attributes: JSONObject? = null

                var paramsMap = mapOf(
                    "did" to did,
                    "type" to type,
                    "page[before]" to pageBefore,
                    "page[after]" to pageAfter,
                    "page[size]" to pageSize)

                val params = URLEncodeParams(paramsMap)

                get(api, null, params, { response ->
                    //JSON response
                    Log.e("IH.getAttributes", response)
                    attributes = JSONObject(response)
                }, { error ->
                    logError("IH.getAttributes", error)
                })
                return attributes
            }

            fun storeAttribute(hash: String, id: String, types: List<String>, name: String, data: ByteArray) {
                val api = "identity-hub/v1/attributes"

                val jsonBody = JSONObject()
                jsonBody.put("id", id)
                jsonBody.put("type", listToJSONArray(types))
                jsonBody.put("name", name)

                val jsonData = JSONObject()
                // Newer base64 encode api
                jsonData.put("base64", String(Base64.encode(data, Base64.DEFAULT), Charsets.UTF_8))
                jsonBody.put("data", jsonData)

                put(api, hash, jsonBody.toString(), { response ->
                    //JSON response
                    Log.e("IH.storeAttribute", response)
                }, { error ->
                    logError("IH.storeAttribute", error)
                })
            }

            fun getAttribute(hash: String) : JSONObject? {
                val api = "identity-hub/v1/attributes"
                var attribute: JSONObject? = null

                get(api, hash, null, { response ->
                    //JSON response
                    Log.e("IH.getAttribute", response)
                    attribute = JSONObject(response)
                }, { error ->
                    logError("IH.getAttribute", error)
                })

                return attribute
            }
        }
    }

    class TrustedIssuersRegistry : EBSIAPI() {
        companion object {
            const val api = "trusted-issuers-registry/v1/issuers"

            fun getIssuers(pageBefore: String? = null, pageAfter: String? = null, pageSize: String? = null) : JSONObject? {
                var paramsMap = mapOf(
                    "page[before]" to pageBefore,
                    "page[after]" to pageAfter,
                    "page[size]" to pageSize)

                // Keep an eye out if empty params is a problem (? unnecessarily appended to url)
                val params = URLEncodeParams(paramsMap)

                var issuers: JSONObject? = null

                get(api, null, params, { response ->
                    //JSON response
                    Log.e("TIR.getIssuers", response)
                    issuers = JSONObject(response)
                }, { error ->
                    logError("TIR.getIssuers", error)
                })
                return issuers
            }

            fun getIssuer(did: String) :JSONObject? {
                var issuer: JSONObject? = null

                get(api, did, null, { response ->
                    //JSON response
                    Log.e("TIR.getIssuer", response)
                    issuer = JSONObject(response)
                }, { error ->
                    logError("TIR.getIssuer", error)
                })
                return issuer
            }
        }
    }

    class VerifiableCredential: EBSIAPI() {
        companion object {
            fun createCredential(types: List<String>, issuer: String, credentialSubject: Map<String, String>) {
                val api = "verifiable-credential/v1/credentials"

                val jsonBody = JSONObject()
                jsonBody.put("type", listToJSONArray(types))
                jsonBody.put("issuer", issuer)
                jsonBody.put("credentialSubject", mapToJSONObject(credentialSubject))

                post(api, null, jsonBody.toString(), { response ->
                    Log.e("VC.createCredential", response)
                }, { error ->
                    logError("VC.createCredential", error)
                })
            }

            fun validateCredential(types: List<String>, issuer: String, credentialSubject: Map<String, String>, proof: Map<String, String>) {
                val api = "verifiable-credential/v1/verifiable-credential-validations"

                val jsonBody = JSONObject()
                jsonBody.put("type", listToJSONArray(types))
                jsonBody.put("issuer", issuer)
                jsonBody.put("credentialSubject", mapToJSONObject(credentialSubject))
                jsonBody.put("proof", mapToJSONObject(proof))

                post(api, null, jsonBody.toString(), { response ->
                    Log.e("VC.validateCredential", response)
                }, { error ->
                    logError("VC.validateCredential", error)
                })
            }
        }
    }

    class VerifiablePresentation : EBSIAPI() {
        companion object {
            fun createPresentation(credentials: List<String>, issuer: String) {
                val api = "verifiable-presentation/v1/presentations"

                val jsonBody = JSONObject()
                jsonBody.put("credentials", listToJSONArray(credentials))
                jsonBody.put("issuer", issuer)

                post(api, null, jsonBody.toString(), { response ->
                    Log.e("VP.createPresentation", response)
                }, { error ->
                    logError("VP.createPresentation", error)
                })
            }

            fun validatePresentation(type: String, verifiableCredentials: List<String>, proof: Map<String, String>) {
                val api = "verifiable-presentation/v1/verifiable-presentation-validations"
                // Check if escaped slashes (\/) in string body is an issue
                val context = "https://www.w3.org/2018/credentials/v1"

                val jsonBody = JSONObject()
                jsonBody.put("@context", listToJSONArray(listOf(context)))
                jsonBody.put("type", type)
                jsonBody.put("verifiableCredential", listToJSONArray(verifiableCredentials))
                jsonBody.put("proof", mapToJSONObject(proof))

                post(api, null, jsonBody.toString(), { response ->
                    Log.e("VP.validatePresentation", response)
                }, { error ->
                    logError("VP.validatePresentation", error)
                })
            }
        }
    }
}
