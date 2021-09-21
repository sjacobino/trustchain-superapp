import { DidAuthRequestPayload, DidAuthResponseCall, EbsiDidAuth } from "@cef-ebsi/did-auth";
import {did, wallet, keyPair, verifiableAuthorisation} from './my_wallet'
import {DID_RESOLVER} from './EBSI'
import * as didJWT from '@cef-ebsi/did-jwt';
import {epochTime, printJWT} from './util'
import { calculateThumbprint } from 'jose/jwk/thumbprint'
import { SignJWT } from 'jose/jwt/sign'
import KeyEncoder from 'key-encoder'
import { createPrivateKey } from 'crypto'

async function getAccessToken(didAuthUri: string) {
    const params = new URLSearchParams(didAuthUri);
    const redirectUri = params.get("client_id")!;
    const didAuthRequestJwtStr = params.get("request")!;

    const authRequestJWT = await didJWT.verifyEbsiJWT(didAuthRequestJwtStr, {
      didRegistry: DID_RESOLVER,
      audience: did, // DID of the recipient of the JWT
      proofPurpose: 'assertionMethod',
    })
    //console.log("~~~ Authentication Request ~~~\n\n")
    //printJWT(authRequestJWT)
    
    
    /* const requestPayload: DidAuthRequestPayload = await EbsiDidAuth.verifyDidAuthRequest(
      didAuthRequestJwtStr,
      DID_RESOLVER
    ); */

    //const didAuthResponseCall: DidAuthResponseCall = {
    const didAuthResponseCall = {
      hexPrivatekey: keyPair.privateKey, // private key managed by the user. Should be passed in hexadecimal format
      did: did, // User DID
      //nonce: requestPayload.nonce, // same nonce received as a Request Payload after verifying it
      nonce: authRequestJWT.payload.nonce,
      redirectUri, // parsed URI from the DID Auth Request payload
      claims: {
        verifiable_credential: encodeURI(JSON.stringify(verifiableAuthorisation)),
      }
    };
    
    /* const didAuthResponseJwt = await EbsiDidAuth.createDidAuthResponse(
      didAuthResponseCall
    ); */
    

    
    const signer = didJWT.ES256KSigner(keyPair.privateKey)
    const expTime = epochTime() + (15 * 60)
    const publicKeyJwk = wallet.getPublicKey({ format: "jwk" });
    const thumbprint = await calculateThumbprint(publicKeyJwk)
    const keyEncoder = new KeyEncoder('secp256k1')
    const pemPrivateKey = keyEncoder.encodePrivate(keyPair.privateKey, 'raw', 'pem')
    const privateKey = createPrivateKey(pemPrivateKey)

    /* const didAuthResponseJwt = await didJWT.createJWT(
      { aud: authRequestJWT.payload.iss,
        exp: expTime,
        nonce: authRequestJWT.payload.nonce,
        sub: did,
        //sub: thumbprint,
        sub_jwk: publicKeyJwk,
        sub_did_verification_method_uri: did + '#keys-1',
        claims: {
          verified_claims: JSON.stringify(verifiableAuthorisation),
        },
      },
      { issuer: 'https://self-issued.me', signer },
      //{ issuer: did, signer },
      //{ alg: 'ES256K', kid: did + '#keys-1' }
      { alg: 'ES256K', kid: did }
    ) */

    const payload = {
      sub: did,
      sub_jwk: publicKeyJwk,
      sub_did_verification_method_uri: did + '#keys-1',
      nonce: authRequestJWT.payload.nonce,
      claims: {
        encryption_key: publicKeyJwk,
        verified_claims: verifiableAuthorisation,
      },
    };

    console.log(payload)

    const idToken = await new SignJWT(payload)
    .setProtectedHeader({
      alg: 'ES256K',
      typ: "JWT",
      kid: did,
    })
    .setIssuedAt()
    .setIssuer("https://self-issued.me")
    .setAudience(redirectUri)
    .setExpirationTime("15s")
    .sign(privateKey);

   

    //console.log("~~~ Authentication Response ~~~\n\n")
    //printJWT(didAuthResponseJwt)
    console.log('=========================')
    //console.log(didAuthResponseJwt)
    console.log(idToken)
}

const didAuthUri = "openid://?response_type=id_token&client_id=https%3A%2F%2Fapi.preprod.ebsi.eu%2Fauthorisation%2Fv1%2Fsiop-sessions&scope=openid%20did_authn&nonce=23a65bda-4faa-4bf8-b980-e3e4147b2342&request=eyJhbGciOiJFUzI1NksiLCJ0eXAiOiJKV1QiLCJraWQiOiJodHRwczovL2FwaS5wcmVwcm9kLmVic2kuZXUvdHJ1c3RlZC1hcHBzLXJlZ2lzdHJ5L3YyL2FwcHMvMHgwOGMyNTg1NmZiY2JkZDA3NmM5YzM5NTEyYWJlZjYzMDk3NDk5MTBhMTEwZDlkMWE5YzlhN2QyYjI3N2I2ZDIwIn0.eyJpYXQiOjE2MzIyMjQ4NDksImV4cCI6MTYzMjIyNTE0OSwiaXNzIjoiZGlkOmVic2k6SEM5dG1paXRXNFM5ZllBYWo2Ullzb3FiVDNzN1Rjd3loeXJzYlNpem5zZFoiLCJzY29wZSI6Im9wZW5pZCBkaWRfYXV0aG4iLCJyZXNwb25zZV90eXBlIjoiaWRfdG9rZW4iLCJjbGllbnRfaWQiOiJodHRwczovL2FwaS5wcmVwcm9kLmVic2kuZXUvYXV0aG9yaXNhdGlvbi92MS9zaW9wLXNlc3Npb25zIiwibm9uY2UiOiIyM2E2NWJkYS00ZmFhLTRiZjgtYjk4MC1lM2U0MTQ3YjIzNDIiLCJjbGFpbXMiOnsiaWRfdG9rZW4iOnsidmVyaWZpZWRfY2xhaW1zIjp7InZlcmlmaWNhdGlvbiI6eyJ0cnVzdF9mcmFtZXdvcmsiOiJFQlNJIiwiZXZpZGVuY2UiOnsidHlwZSI6eyJ2YWx1ZSI6InZlcmlmaWFibGVfY3JlZGVudGlhbCJ9LCJkb2N1bWVudCI6eyJ0eXBlIjp7ImVzc2VudGlhbCI6dHJ1ZSwidmFsdWUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiLCJWZXJpZmlhYmxlQXV0aG9yaXNhdGlvbiJdfSwiY3JlZGVudGlhbFNjaGVtYSI6eyJpZCI6eyJlc3NlbnRpYWwiOnRydWUsInZhbHVlIjoiaHR0cHM6Ly9hcGkucHJlcHJvZC5lYnNpLmV1L3RydXN0ZWQtc2NoZW1hcy1yZWdpc3RyeS92MS9zY2hlbWFzLzB4MzEyZTMzMmUzNjJlMzEyZTM0MmUzMTJlMzEzNjM2MzQyZTMxMzAyZTMxMzgzNzJlMzEyZTMyMmUzMjJlMzMzMyJ9fX19fX19fX0.rtup6wkTi4W7OclN5XRkZd1B7YIM9-jqcTS-mRwldcv7u7umGvPQM76Tebo-HRgkbCSA7Sj_Tyozd3v_UqAvdw"
getAccessToken(didAuthUri)