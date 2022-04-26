package nl.tudelft.trustchain.common.ebsi

import android.util.Log
import com.android.volley.Response
import org.json.JSONObject
import java.net.URI

object OnboardingTools {

    private const val TAG  = "OnbrdngTools"

    private fun getVerifiableAuthorisationListener(wallet: EBSIWallet): Response.Listener<JSONObject> {
        return Response.Listener<JSONObject> { response ->
            Log.e(TAG, "Verifiable Authorisation: $response")
            wallet.storeVerifiableAuthorisation(response)
        }
    }

    private fun getAuthenticationVerificationListener(wallet: EBSIWallet, sessionToken: String, errorListener: Response.ErrorListener): VerificationListener {
        // =====ONBOARD_01_A Requests Verifiable Authorisation (VA)=====

        val authenticationVerificationListener = VerificationListener { payload ->
            if (payload != null) {
                val clientId = payload["client_id"]?.toString()
                val iss = payload["iss"].toString()
                val filteredPayload = payload.filter { JWTHelper.stringClaims.contains(it.key) }
                val responseJWT = JWTHelper.createJWT(wallet, iss, filteredPayload, null)

                Log.e(TAG, "Response jwt: $responseJWT")

                if (clientId.isNullOrEmpty()) {
                    errorListener.onErrorResponse(MyVolleyError("No client id to redirect to"))
                    return@VerificationListener
                }


                // =====ONBOARD_02A Proves control of DID key=====
                EBSIAPI.getVerifiableAuthorisation(sessionToken,
                    clientId,
                    responseJWT,
                    getVerifiableAuthorisationListener(wallet),
                    errorListener
                )
            } else {
                Log.e(TAG, "Auth request verification failed")
            }
        }

        return authenticationVerificationListener
    }

    private fun getAuthenticationRequestListener(wallet: EBSIWallet, sessionToken: String, errorListener: Response.ErrorListener): Response.Listener<JSONObject> {
        return Response.Listener<JSONObject> { response ->
            val token = response.getString("session_token")
            val params = URI(token).splitQuery()
            val authRequestJWT = params.firstOrNull {
                it.first == "request"
            }?.second

            if (authRequestJWT != null) {
                JWTHelper.verifyJWT(authRequestJWT, getAuthenticationVerificationListener(wallet, sessionToken, errorListener))
            }
        }
    }

    fun getVerifiableAuthorisation(wallet: EBSIWallet, sessionToken: String, errorListener: Response.ErrorListener) {
        val did = wallet.did
        val didDocument = wallet.didDocument()

        Log.e(TAG, "did: $did")
        Log.e(TAG, "didDocument: $didDocument")

        val api = "users-onboarding/v1/authentication-requests"
        val scope = "ebsi users onboarding"
        EBSIAPI.getAuthenticationRequest(api, scope, getAuthenticationRequestListener(wallet, sessionToken, errorListener), errorListener)
    }
}
