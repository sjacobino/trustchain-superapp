import { Agent, DidAuthRequestPayload, DidAuthResponseCall, UriResponse } from '@cef-ebsi/siop-auth'
import { KeyPair } from '@cef-ebsi/wallet-lib';
import { myAgent, myWallet, myKeyPair, myDid } from './my_wallet';
import { calculateThumbprint, JWK } from 'jose/jwk/thumbprint'
import { epochTime } from './util'
import * as didJWT from '@cef-ebsi/did-jwt';

async function verifiyAuthRequest(uri: string, agent: Agent): Promise<DidAuthRequestPayload> {
    console.log("=====Verifying onboarding auth request=====")
    const params = new URLSearchParams(uri);
    const authRequestJwt = params.get("request");

    const authRequestPayloadPromise: Promise<DidAuthRequestPayload> = agent.verifyAuthenticationRequest(authRequestJwt!)
    return authRequestPayloadPromise
}

async function createAuthenticationResponse(requestPayload: DidAuthRequestPayload,
    publicKeyJwk: JWK, keyPair: KeyPair, did: string, agent: Agent) {
    console.log("=====Creating auth response=====")

    const signer = didJWT.ES256KSigner(keyPair.privateKey)
    const expTime = epochTime() + (15 * 60)

    const thumbprint = await calculateThumbprint(publicKeyJwk)

    const authenticationResponse = didJWT.createJWT(
        {aud: requestPayload.iss,
            exp: expTime,
            nonce: requestPayload.nonce,
            sub: thumbprint,
            sub_jwk: publicKeyJwk,
            sub_did_verification_method_uri: did + '#keys-1'
        },
        { issuer: did, signer },
        { alg: 'ES256K', kid: did + '#keys-1' }
    )

    return authenticationResponse
    
    /* const authResponseCall: DidAuthResponseCall = {
        did: "",
        redirectUri: requestPayload.redirect_uri,
        aud: requestPayload.iss,
        exp: epochTime() + (15 * 60),
        nonce: requestPayload.nonce,
        sub: thumbprint,
        sub_jwk: publicKeyJwk,
        sub_did_verification_method_uri: did + '#keys-1'
    } 
    
    const uriResponsePromise: Promise<UriResponse> = agent.createAuthenticationResponse(authResponseCall)
    return uriResponsePromise
    */
}

function onboard() {
    // CHANGE REQUEST RESPONSE FOR EACH ONBOARDING
    const response = {
        "session_token": "openid://?response_type=id_token&client_id=https%3A%2F%2Fapi.preprod.ebsi.eu%2Fusers-onboarding%2Fv1%2Fauthentication-responses&scope=openid%20did_authn&nonce=e2d71705-0a72-4438-8133-928dde2636d4&request=eyJhbGciOiJFUzI1NksiLCJ0eXAiOiJKV1QiLCJraWQiOiJodHRwczovL2FwaS5wcmVwcm9kLmVic2kuZXUvdHJ1c3RlZC1hcHBzLXJlZ2lzdHJ5L3YyL2FwcHMvMHgwNmQzNGI0ZWExYzdhYjk1OGE4ZmE3ZmQ1MmE4NzNjNjhmY2Q1ZTFhNjU1YzU0YTQyMDlhMGM1NzVmOGRjMjljIn0.eyJpYXQiOjE2Mzk1MjAyODUsImV4cCI6MTYzOTUyMDU4NSwiaXNzIjoiZGlkOmVic2k6emNHdnFnWlRIQ3Rramd0Y0tSTDdIOGsiLCJzY29wZSI6Im9wZW5pZCBkaWRfYXV0aG4iLCJyZXNwb25zZV90eXBlIjoiaWRfdG9rZW4iLCJyZXNwb25zZV9tb2RlIjoicG9zdCIsImNsaWVudF9pZCI6Imh0dHBzOi8vYXBpLnByZXByb2QuZWJzaS5ldS91c2Vycy1vbmJvYXJkaW5nL3YxL2F1dGhlbnRpY2F0aW9uLXJlc3BvbnNlcyIsInJlZGlyZWN0X3VyaSI6Imh0dHBzOi8vYXBpLnByZXByb2QuZWJzaS5ldS91c2Vycy1vbmJvYXJkaW5nL3YxL2F1dGhlbnRpY2F0aW9uLXJlc3BvbnNlcyIsIm5vbmNlIjoiZTJkNzE3MDUtMGE3Mi00NDM4LTgxMzMtOTI4ZGRlMjYzNmQ0In0.xc5kTdWrZul9UzCexT5xb0yLteSDaNxmWMbEjrT_hDUswD82kXO60b8ehfGS5z-PmmUYWWasu--7O78Ptrh9tQ"
      }
    verifiyAuthRequest(response["session_token"], myAgent).then((requestPayload) => {
        console.log(requestPayload)

        const publicKeyJwk = myWallet.getPublicKey({ format: "jwk" })

        createAuthenticationResponse(requestPayload, publicKeyJwk, myKeyPair, myDid, myAgent)
            .then((authenticationResponse) => {
                console.log("=====Authentication Response=====")
                console.log(authenticationResponse)
                console.log("==========================")
            })
    })
}

if (require.main === module){
    onboard()
}