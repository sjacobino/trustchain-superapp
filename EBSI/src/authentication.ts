import { DidAuthRequestPayload, DidAuthResponseCall, EbsiDidAuth } from "@cef-ebsi/did-auth";
import {did, keyPair} from './my_wallet'
import {DID_RESOLVER} from './EBSI'
import * as didJWT from '@cef-ebsi/did-jwt';

async function getAccessToken(didAuthUri: string) {
    const params = new URLSearchParams(didAuthUri);
    const redirectUri = params.get("client_id")!;
    const didAuthRequestJwt = params.get("request")!;
    const didResolver = DID_RESOLVER;

    const authenticationJWT = await didJWT.verifyEbsiJWT(didAuthRequestJwt, {
      didRegistry: 'https://api.preprod.ebsi.eu/did-registry/v2/identifiers',
      audience: did, // DID of the recipient of the JWT
      proofPurpose: 'assertionMethod',
    })
    console.log(authenticationJWT)

    // const requestPayload: DidAuthRequestPayload = await EbsiDidAuth.verifyDidAuthRequest(
    //     didAuthRequestJwt,
    //     didResolver
    //   );

      const didAuthResponseCall: DidAuthResponseCall = {
        hexPrivatekey: keyPair.privateKey, // private key managed by the user. Should be passed in hexadecimal format
        did: did, // User DID
        //nonce: requestPayload.nonce, // same nonce received as a Request Payload after verifying it
        nonce: authenticationJWT.payload.nonce,
        redirectUri, // parsed URI from the DID Auth Request payload
      };
      
      const didAuthResponseJwt = await EbsiDidAuth.createDidAuthResponse(
        didAuthResponseCall
      );

      console.log(didAuthRequestJwt)
}

const didAuthUri = "openid://?response_type=id_token&client_id=https%3A%2F%2Fapi.preprod.ebsi.eu%2Fauthorisation%2Fv1%2Fsiop-sessions&scope=openid%20did_authn&nonce=1431b243-fcf3-44e8-a714-b819e22396cd&request=eyJhbGciOiJFUzI1NksiLCJ0eXAiOiJKV1QiLCJraWQiOiJodHRwczovL2FwaS5wcmVwcm9kLmVic2kuZXUvdHJ1c3RlZC1hcHBzLXJlZ2lzdHJ5L3YyL2FwcHMvMHgwOGMyNTg1NmZiY2JkZDA3NmM5YzM5NTEyYWJlZjYzMDk3NDk5MTBhMTEwZDlkMWE5YzlhN2QyYjI3N2I2ZDIwIn0.eyJpYXQiOjE2MzAwNTQ1MDYsImV4cCI6MTYzMDA1NDgwNiwiaXNzIjoiZGlkOmVic2k6SEM5dG1paXRXNFM5ZllBYWo2Ullzb3FiVDNzN1Rjd3loeXJzYlNpem5zZFoiLCJzY29wZSI6Im9wZW5pZCBkaWRfYXV0aG4iLCJyZXNwb25zZV90eXBlIjoiaWRfdG9rZW4iLCJjbGllbnRfaWQiOiJodHRwczovL2FwaS5wcmVwcm9kLmVic2kuZXUvYXV0aG9yaXNhdGlvbi92MS9zaW9wLXNlc3Npb25zIiwibm9uY2UiOiIxNDMxYjI0My1mY2YzLTQ0ZTgtYTcxNC1iODE5ZTIyMzk2Y2QiLCJjbGFpbXMiOnsiaWRfdG9rZW4iOnsidmVyaWZpZWRfY2xhaW1zIjp7InZlcmlmaWNhdGlvbiI6eyJ0cnVzdF9mcmFtZXdvcmsiOiJFQlNJIiwiZXZpZGVuY2UiOnsidHlwZSI6eyJ2YWx1ZSI6InZlcmlmaWFibGVfY3JlZGVudGlhbCJ9LCJkb2N1bWVudCI6eyJ0eXBlIjp7ImVzc2VudGlhbCI6dHJ1ZSwidmFsdWUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiLCJWZXJpZmlhYmxlQXV0aG9yaXNhdGlvbiJdfSwiY3JlZGVudGlhbFNjaGVtYSI6eyJpZCI6eyJlc3NlbnRpYWwiOnRydWUsInZhbHVlIjoiaHR0cHM6Ly9hcGkucHJlcHJvZC5lYnNpLmV1L3RydXN0ZWQtc2NoZW1hcy1yZWdpc3RyeS92MS9zY2hlbWFzLzB4MzEyZTMzMmUzNjJlMzEyZTM0MmUzMTJlMzEzNjM2MzQyZTMxMzAyZTMxMzgzNzJlMzEyZTMyMmUzMjJlMzMzMyJ9fX19fX19fX0.-k9zii49a186jMrAzlEaYbuQydTYGZTmu9K4E2NlGS_BeLHnCkigsdqeVCuertRpJEdDcOCtefy1EXIMer0jNg"
getAccessToken(didAuthUri)