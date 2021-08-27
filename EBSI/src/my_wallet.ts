import { EbsiWallet } from '@cef-ebsi/wallet-lib';
import * as didJWT from '@cef-ebsi/did-jwt';
import { calculateThumbprint } from 'jose/jwk/thumbprint'
import {epochTime, randomHex} from './util'

export const keyPair = {
    keyOptions: { format: 'hex', keyType: 'EC', keyCurve: 'secp256k1' },
    publicKey: '0404c21b866716cfd5d9a8051b78d775f0c9e8faafeff129b2894654110e8b2f5a656d5a1529dc594ddc091287612c1eab625b54b2d4b7a612ba9b327f667839dd',
    privateKey: '449298bd4e730aa0c22d53b502f4bc651cdbfb6f1169e2dc7ce177b160c1be6d'
}

export const did = 'did:ebsi:UMyr8vsGgEcmDeYoPUo7xGchUPiiUYj6DAt11YT9MDK'

export const wallet = new EbsiWallet(keyPair.privateKey);

// Taken from real response from preprod test api
export const didDocument = {
    '@context': 'https://w3id.org/did/v1',
    id: did,
    verificationMethod:[{
        id: did + '#key-1',
        type:'Secp256k1VerificationKey2018',
        controller: did,
        publicKeyHex: keyPair.publicKey}],
    authentication:[
        did],
    assertionMethod:[
        did + '#key-1']
}

export const verifiableAuthorisation = {
  "@context": [
    "https://www.w3.org/2018/credentials/v1",
    "https://www.w3.org/2018/credentials/examples/v1",
    "https://w3c-ccg.github.io/lds-jws2020/contexts/lds-jws2020-v1.json"
  ],
  "id": "vc:ebsi:authentication#117e0c8a-b9a7-46fc-b4df-a350c4c0cb6b",
  "issuer": "did:ebsi:4jPxcigvfifZyVwym5zjxaKXGJTt7YwFtpg6AXtsR4d5",
  "validFrom": "2021-08-27T07:57:12Z",
  "credentialSubject": {
    "id": "did:ebsi:UMyr8vsGgEcmDeYoPUo7xGchUPiiUYj6DAt11YT9MDK"
  },
  "credentialSchema": {
    "id": "https://api.preprod.ebsi.eu/trusted-schemas-registry/v1/schemas/0x312e332e362e312e342e312e313636342e31302e3138372e312e322e322e3333",
    "type": "OID"
  },
  "issuanceDate": "2021-08-27T07:57:12Z",
  "expirationDate": "2022-02-25T07:57:12Z",
  "type": [
    "VerifiableCredential",
    "VerifiableAuthorisation"
  ],
  "proof": {
    "type": "EcdsaSecp256k1Signature2019",
    "created": "2021-08-27T07:57:12Z",
    "proofPurpose": "assertionMethod",
    "verificationMethod": "did:ebsi:4jPxcigvfifZyVwym5zjxaKXGJTt7YwFtpg6AXtsR4d5#keys-1",
    "jws": "eyJhbGciOiJFUzI1NksiLCJ0eXAiOiJKV1QifQ..3lPV2XFTB5FILohG37x8YvNCES7xdYHWzzQGkDTEbauQH1Zknu48nKaYVM-CMbNkyNYV0w80pfixJGWlIOouUg"
  }
}

async function createAuthenticationResponse(authenticationRequest: string){
  console.log('VERIFYING AUTHENTICATION REQUEST...')
  const authenticationJWT = await didJWT.verifyEbsiJWT(authenticationRequest, {
    didRegistry: 'https://api.preprod.ebsi.eu/did-registry/v2/identifiers',
    audience: did, // DID of the recipient of the JWT
    proofPurpose: 'assertionMethod',
  })

  console.log(authenticationJWT)

  const signer = didJWT.ES256KSigner(keyPair.privateKey)
  const timeOffset = 30 * 60 //Seems to be a time offset error with the server
  const offsetCreationTime = epochTime()
  const expTime = offsetCreationTime + (15 * 60)

  const authenticationIssuer = authenticationJWT.payload.iss
  const authenticationRequestNonce = authenticationJWT.payload.nonce

  const publicKeyJwk = wallet.getPublicKey({ format: "jwk" });
  const thumbprint = await calculateThumbprint(publicKeyJwk)

  console.log('CREATING AUTHENTICATION RESPONSE...')
  const authenticationResponse = await didJWT.createJWT(
    { aud: authenticationIssuer,
      exp: expTime,
      nonce: authenticationRequestNonce,
      sub: thumbprint,
      sub_jwk: publicKeyJwk,
      sub_did_verification_method_uri: did + '#key-1'
    },
    { issuer: did, signer },
    { alg: 'ES256K', kid: did + '#key-1' }
  )

  return authenticationResponse
}

function main() {
  const authenticationRequest = 'eyJhbGciOiJFUzI1NksiLCJ0eXAiOiJKV1QiLCJraWQiOiJodHRwczovL2FwaS5wcmVwcm9kLmVic2kuZXUvdHJ1c3RlZC1hcHBzLXJlZ2lzdHJ5L3YyL2FwcHMvMHgwNmQzNGI0ZWExYzdhYjk1OGE4ZmE3ZmQ1MmE4NzNjNjhmY2Q1ZTFhNjU1YzU0YTQyMDlhMGM1NzVmOGRjMjljIn0.eyJpYXQiOjE2MzAwNDk4OTEsImV4cCI6MTYzMDA1MDE5MSwiaXNzIjoiZGlkOmVic2k6NGpQeGNpZ3ZmaWZaeVZ3eW01emp4YUtYR0pUdDdZd0Z0cGc2QVh0c1I0ZDUiLCJzY29wZSI6Im9wZW5pZCBkaWRfYXV0aG4iLCJyZXNwb25zZV90eXBlIjoiaWRfdG9rZW4iLCJjbGllbnRfaWQiOiJodHRwczovL2FwaS5wcmVwcm9kLmVic2kuZXUvdXNlcnMtb25ib2FyZGluZy92MS9hdXRoZW50aWNhdGlvbi1yZXNwb25zZXMiLCJub25jZSI6IjY1MTAwMzFiLTBlZTktNGNmMy1iODQ3LTQ5YTAyMmU2MDQ5YSJ9.mEwbGs4iuOziMiVleF7n76eggyFqAYdOwYAmKkG2CwiGw8aaS3t6LavT2YnsqSgLAig9cxXUUnnGBcL330MRMw'

  // https://openid.net/specs/openid-connect-core-1_0.html
  createAuthenticationResponse(authenticationRequest).then( (authenticationResponse) => {
    console.log('AUTHENTICATION RESPONSE:')
    console.log(didJWT.decodeJWT(authenticationResponse))
    console.log(authenticationResponse)
  }).catch( (error) =>{
    console.log('Authentication Response creation failed')
    console.log(error)
  })
}