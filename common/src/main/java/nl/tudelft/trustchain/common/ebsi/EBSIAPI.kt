package nl.tudelft.trustchain.common.ebsi

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.nio.charset.Charset

open class EBSIAPI {
    companion object {
        val server = "https://api.ebsi.xyz"
        private lateinit var queue: RequestQueue

        fun get(api: String, params: String, onSuccess: (String) -> Unit, onError: (VolleyError) -> Unit) {
            val url = "$server/$api/$params"
            val stringRequest = StringRequest(Request.Method.GET, url,
                { response ->
                    onSuccess(response)
                },
                {
                    onError(it)
                })

            queue.add(stringRequest)
        }

        fun post(api: String, body: String, onSuccess: (String) -> Unit, onError: (VolleyError) -> Unit) {
            val url = "$server/$api"
            val stringRequest = object: StringRequest(Method.POST, url,
                { response ->
                    onSuccess(response)
                },
                {
                    onError(it)
                }){
                override fun getBody(): ByteArray {
                    return body.toByteArray(Charset.forName("UTF-8"))
                }

                override fun getBodyContentType(): String {
                    return "application/json"
                }
            }
            queue.add(stringRequest)
        }

        fun put(api: String, params: String, body: String, onSuccess: (String) -> Unit, onError: (VolleyError) -> Unit) {
            val url = "$server/$api/$params"
            val stringRequest = object: StringRequest(Method.PUT, url,
                { response ->
                    onSuccess(response)
                },
                {
                    onError(it)
                }){
                override fun getBody(): ByteArray {
                    return body.toByteArray(Charset.forName("UTF-8"))
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
            Log.e("$tag (data)", String(error.networkResponse.data, Charset.forName("UTF-8")))
            error.networkResponse.allHeaders?.forEach {
                Log.e("$tag (header)", "${it.name}: ${it.value}")
            }
            error.printStackTrace()
        }

        fun test(context: Context) {
            init(context)

            //DID.getDID("did:ebsi:0xec457d0a974c48d5685a7efa03d137dc8bbde7e3")

            Wallet.authenticate("urn:ietf:params:oauth:grant-type:jwt-bearer",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
                "ebsi profile user")
            val jws = Wallet.sign("did:ebsi:0xB551b70d650892d23dE3Be201A95c1FcBea98A3D",
                "{payload-for-signing}",
                "EcdsaSecp256k1Signature2019")
            if (jws != null) {
                Wallet.validate(jws)
            }
        }
    }

    class DID: EBSIAPI() {
        companion object {
            const val api = "did/v1/identifiers"

            fun getDID(did: String) : String? {
                var DID: String? = null
                get(api, did, {response ->
                    //JSON response
                    Log.e("DIDAPI", response)
                    DID = response
                }, { error ->
                    logError("DID.getDID", error)
                })
                return DID
            }
        }
    }

    class Wallet() : EBSIAPI() {
        companion object {
            fun authenticate(grantType: String, assertion: String, scope: String) {
                val api = "wallet/v1/sessions"

                val jsonBody = JSONObject()
                jsonBody.put("grantType", grantType)
                jsonBody.put("assertion", assertion)
                jsonBody.put("scope", scope)

                Log.e("Wallet.authenticate", jsonBody.toString())

                post(api, jsonBody.toString(), { response ->
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

                post(api, jsonBody.toString(), { response ->
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

                post(api, jsonBody.toString(), { response ->
                    Log.e("Wallet.validate", response)
                }, { error ->
                    logError("Wallet.validate", error)
                })
            }
        }
    }
}
