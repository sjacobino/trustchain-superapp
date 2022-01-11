import {myDid, myWallet, myKeyPair, myAgent, verifiableAuthorisationPresentation, verifiableAuthorisation} from './my_wallet'
import { Agent, DidAuthRequestPayload, DidAuthResponseCall, DidAuthResponseMode } from '@cef-ebsi/siop-auth'
import KeyEncoder from 'key-encoder'
import { EbsiWallet, KeyPair } from '@cef-ebsi/wallet-lib'

const keyEncoder = new KeyEncoder('secp256k1')
//const pemPrivateKey = keyEncoder.encodePrivate(keyPair.privateKey, 'raw', 'pem')

async function verifiyAuthRequest(uri: string, agent: Agent) {
    console.log("=====Verifying onboarding auth request=====")
    const params = new URLSearchParams(uri);
    const didAuthRequestJwt = params.get("request");

    return agent.verifyAuthenticationRequest(didAuthRequestJwt!)
}

async function createAuthenticationResponse(requestPayload: DidAuthRequestPayload, wallet: EbsiWallet,
    did: string, keyPair: KeyPair, agent: Agent) {
    console.log("=====Creating auth response=====")
    const publicKeyJwk = wallet.getPublicKey({ format: "jwk" })
   
    const verifiablePresentation = await verifiableAuthorisationPresentation()
    
    const didAuthResponseCall: DidAuthResponseCall = {
        did: did,
        redirectUri: requestPayload.client_id,
        nonce: requestPayload.nonce,
        responseMode: DidAuthResponseMode.FORM_POST,
        /*claims: {
            //encryption_key: publicKeyJwk,
            //verified_claims: JSON.stringify(verifiablePresentation),
            verified_claims: JSON.stringify({
                trust_framework: "eidas_ial_substantial",
                claims: {
                    verifiable_credential: [verifiableAuthorisation]
                } 
            })
          },*/
        hexPrivatekey: "0x" + keyPair.privateKey,
    }
    
    return agent.createAuthenticationResponse(didAuthResponseCall)
}

function authorize() {
    const response = {
        "uri": "openid://?response_type=id_token&client_id=https%3A%2F%2Fapi.preprod.ebsi.eu%2Fauthorisation%2Fv1%2Fsiop-sessions&scope=openid%20did_authn&nonce=a3841ec5-cd6c-4a13-aa2b-3480a0503952&request=eyJhbGciOiJFUzI1NksiLCJ0eXAiOiJKV1QiLCJraWQiOiJodHRwczovL2FwaS5wcmVwcm9kLmVic2kuZXUvdHJ1c3RlZC1hcHBzLXJlZ2lzdHJ5L3YyL2FwcHMvMHgwOGMyNTg1NmZiY2JkZDA3NmM5YzM5NTEyYWJlZjYzMDk3NDk5MTBhMTEwZDlkMWE5YzlhN2QyYjI3N2I2ZDIwIn0.eyJpYXQiOjE2Mzk1OTYwNzgsImV4cCI6MTYzOTU5NjM3OCwiaXNzIjoiZGlkOmVic2k6em5IZVpXdmhBSzJGSzJEazFqWE5lN20iLCJzY29wZSI6Im9wZW5pZCBkaWRfYXV0aG4iLCJyZXNwb25zZV90eXBlIjoiaWRfdG9rZW4iLCJyZXNwb25zZV9tb2RlIjoicG9zdCIsImNsaWVudF9pZCI6Imh0dHBzOi8vYXBpLnByZXByb2QuZWJzaS5ldS9hdXRob3Jpc2F0aW9uL3YxL3Npb3Atc2Vzc2lvbnMiLCJyZWRpcmVjdF91cmkiOiJodHRwczovL2FwaS5wcmVwcm9kLmVic2kuZXUvYXV0aG9yaXNhdGlvbi92MS9zaW9wLXNlc3Npb25zIiwibm9uY2UiOiJhMzg0MWVjNS1jZDZjLTRhMTMtYWEyYi0zNDgwYTA1MDM5NTIiLCJjbGFpbXMiOnsiaWRfdG9rZW4iOnsidmVyaWZpZWRfY2xhaW1zIjp7InZlcmlmaWNhdGlvbiI6eyJ0cnVzdF9mcmFtZXdvcmsiOiJFQlNJIiwiZXZpZGVuY2UiOnsidHlwZSI6eyJ2YWx1ZSI6InZlcmlmaWFibGVfY3JlZGVudGlhbCJ9LCJkb2N1bWVudCI6eyJ0eXBlIjp7ImVzc2VudGlhbCI6dHJ1ZSwidmFsdWUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiLCJWZXJpZmlhYmxlQXV0aG9yaXNhdGlvbiJdfSwiY3JlZGVudGlhbFNjaGVtYSI6eyJpZCI6eyJlc3NlbnRpYWwiOnRydWUsInZhbHVlIjoiaHR0cHM6Ly9hcGkucHJlcHJvZC5lYnNpLmV1L3RydXN0ZWQtc2NoZW1hcy1yZWdpc3RyeS92MS9zY2hlbWFzLzB4MzEyZTMzMmUzNjJlMzEyZTM0MmUzMTJlMzEzNjM2MzQyZTMxMzAyZTMxMzgzNzJlMzEyZTMyMmUzMjJlMzMzMyJ9fX19fX19fX0.jXd457DYbRi4Si6rR2vfzmDaK_l7Vn7iSytgeY3ZM7RG62sc30hRsdk4hEiIzm7E9Vooea23fS8fIEGS4BgmPg"
      }
    verifiyAuthRequest(response["uri"], myAgent)
        .then((requestPayload) => {
            console.log("=====Authentication Request=====")
            console.log(requestPayload)
            //console.log(requestPayload.claims?.id_token?.verified_claims)
            console.log("==========================")
            createAuthenticationResponse(requestPayload, myWallet, myDid, myKeyPair, myAgent)
                .then((uriResponse) => {
                    console.log("=====Authentication Response=====")
                    //console.log(uriResponse)
                    const params = new URLSearchParams(uriResponse.bodyEncoded);
                    const responseIdToken = params.get("id_token")
                    console.log(responseIdToken)
                    console.log("==========================")
                }).
                catch((error) => console.log(error))
            }).
        catch((error) => console.log("Auth request invalid or unable to be verified"))
}

if (require.main === module){
    authorize()
}