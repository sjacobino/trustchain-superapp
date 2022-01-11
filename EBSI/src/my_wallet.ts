import { EbsiWallet } from '@cef-ebsi/wallet-lib';
import { epochTime } from './util'
import { Presentation, createPresentation, InputPresentation, createVerifiablePresentation, VerifiablePresentation } from '@cef-ebsi/verifiable-presentation';
import { RequiredProof, SignatureValue, Options, VerifiableCredential } from '@cef-ebsi/verifiable-credential';
import { DID_RESOLVER, TRUSTED_ISSUERS_REGISTRY } from './config';
import * as joseJws from 'jose/jws/general/sign'
import KeyEncoder from 'key-encoder'
import { createPrivateKey } from 'crypto'
import { Agent } from '@cef-ebsi/siop-auth'

export const myKeyPair = {
    keyOptions: { format: 'hex', keyType: 'EC', keyCurve: 'secp256k1' },
    publicKey: '0404c21b866716cfd5d9a8051b78d775f0c9e8faafeff129b2894654110e8b2f5a656d5a1529dc594ddc091287612c1eab625b54b2d4b7a612ba9b327f667839dd',
    privateKey: '449298bd4e730aa0c22d53b502f4bc651cdbfb6f1169e2dc7ce177b160c1be6d'
}

export const myDid =  'did:ebsi:zhnJEipuEzftgqzXJmPJHGs'

export const myWallet = new EbsiWallet(myKeyPair.privateKey);

export const myAgent = new Agent({privateKey: "0x" + myKeyPair.privateKey,
                            didRegistry: DID_RESOLVER})

export const verifiableAuthorisation: VerifiableCredential = {
  "id": "vc:ebsi:authentication#6eeacce0-1b3a-4df3-b9ac-051d84aa394b",
  "issuer": "did:ebsi:zcGvqgZTHCtkjgtcKRL7H8k",
  "validFrom": "2021-12-14T22:19:37Z",
  "credentialSubject": {
    "id": "did:ebsi:zhnJEipuEzftgqzXJmPJHGs"
  },
  "credentialSchema": {
    "id": "https://api.preprod.ebsi.eu/trusted-schemas-registry/v1/schemas/0x312e332e362e312e342e312e313636342e31302e3138372e312e322e322e3333",
    "type": "OID"
  },
  "issuanceDate": "2021-12-14T22:19:37Z",
  "expirationDate": "2022-06-14T22:19:37Z",
  "@context": [
    "https://www.w3.org/2018/credentials/v1",
    "https://www.w3.org/2018/credentials/examples/v1",
    "https://w3c-ccg.github.io/lds-jws2020/contexts/lds-jws2020-v1.json"
  ],
  "type": [
    "VerifiableCredential",
    "VerifiableAuthorisation"
  ],
  "proof": {
    "type": "EcdsaSecp256k1Signature2019",
    "created": "2021-12-14T22:19:37Z",
    "proofPurpose": "assertionMethod",
    "verificationMethod": "did:ebsi:zcGvqgZTHCtkjgtcKRL7H8k#keys-1",
    "jws": "eyJhbGciOiJFUzI1NksiLCJ0eXAiOiJKV1QifQ..bUi8g_Hn2Xoq3d9xkbVOiyY-WhGswJk75zWiK6jLVkdThcWDXELOh3QQ8vKlUZ8QHIkDXqGcX0rckUX_qR4XRA"
  }
}

export async function verifiableAuthorisationPresentation(): Promise<VerifiablePresentation> {
  const inputPresentation: InputPresentation = {
      //"@context": verifiableAuthorisation["@context"],
      //id: verifiableAuthorisation["id"],
      //type: "VerifiablePresentation",
      //holder: did,
      verifiableCredential: [verifiableAuthorisation]
  }

  const presentation: Presentation = createPresentation(inputPresentation)
  const uint8Presentation = new TextEncoder().encode(JSON.stringify(presentation));

  const keyEncoder = new KeyEncoder('secp256k1')
  const pemPrivateKey = keyEncoder.encodePrivate(myKeyPair.privateKey, 'raw', 'pem')
  const privateKey = createPrivateKey(pemPrivateKey)
  const generalSign = new joseJws.GeneralSign(uint8Presentation)
  generalSign.addSignature(privateKey).setProtectedHeader({ alg: 'ES256K' })
  const jws: joseJws.GeneralJWS = await generalSign.sign()
  const signature = jws.signatures[0]

  const requiredProof: RequiredProof = {
    type: "EcdsaSecp256k1Signature2019",
    proofPurpose: "assertionMethod",
    verificationMethod: myDid + '#keys-1'
  }

  const signatureValue: SignatureValue = {
    proofValue: signature.protected + ".." + signature.signature,
    proofValueName: "jws",
    iat: epochTime()
  }

  const options: Options = {
    resolver: DID_RESOLVER,
    tirUrl: TRUSTED_ISSUERS_REGISTRY
  }

  const verifiablePresentation: Promise<VerifiablePresentation> = createVerifiablePresentation(presentation, requiredProof, signatureValue, options)

  return verifiablePresentation
}

if (require.main === module){
  //verifiableAuthorisationPresentation()
  console.log(myWallet.getEthereumAddress())
}