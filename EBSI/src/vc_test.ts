import {
  createCredential,
  createVerifiableCredential,
  validateVerifiableCredential,
  RequiredProof,
  SignatureValue,
  Options,
} from "@cef-ebsi/verifiable-credential";
import { Resolver } from "did-resolver";
import { getResolver } from "@cef-ebsi/did-resolver";

const credential = createCredential({
  credentialSubject: { name: "alice" },
  issuer: "did:ebsi:0x03c56Aa9553922b6dEBef63d966975e11b1Df113",
});

console.log("CREDENTIAL")
console.log(credential);
/*
{
  "@context": [
    "https://www.w3.org/2018/credentials/v1",
    "https://EBSI-WEBSITE.EU/schemas/vc/2019/v1#",
    "https://EBSI-WEBSITE.EU/schemas/eidas/2019/v1#",
  ],
  type: "VerifiableCredential",
  issuanceDate: "2020-11-23T00:00:00Z",
  credentialSubject: {
    name: "alice"
  },
  issuer: "did:ebsi:0x03c56Aa9553922b6dEBef63d966975e11b1Df113"
}
*/

const requiredProof: RequiredProof = {
  type: "EcdsaSecp256k1Signature2019",
  proofPurpose: "assertionMethod",
  verificationMethod:
    "did:ebsi:0xdE3d8e8f30B425ACe6F6549D3188Ae9F0047Ea1A#eidasKey",
};

const signatureValue: SignatureValue = {
  proofValue:
    "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9..xSfnO21PsmCPgLkE34wZQ5mLOcRmb4NPiBSKw3EULCBZiVSWrR_VhddwMuyydBwkimA04f2NJQofD3gF3t5tuAE",
  proofValueName: "jws",
  iat: 0
};

const vc = createVerifiableCredential(
  credential,
  requiredProof,
  signatureValue
);

console.log("VERIFIABLE CREDENTIAL")
console.log(vc);

/*
{
  "@context": [
    "https://www.w3.org/2018/credentials/v1",
    "https://EBSI-WEBSITE.EU/schemas/vc/2019/v1#",
    "https://EBSI-WEBSITE.EU/schemas/eidas/2019/v1#",
  ],
  type: "VerifiableCredential",
  issuanceDate: "2020-11-23T00:00:00Z",
  credentialSubject: {
    name: "alice"
  },
  issuer: "did:ebsi:0x03c56Aa9553922b6dEBef63d966975e11b1Df113",
  "proof": {
    "type": "EcdsaSecp256k1Signature2019",
    "created": "2020-11-12T12:08:08.163Z",
    "proofPurpose": "assertionMethod",
    "verificationMethod": "did:ebsi:0xdE3d8e8f30B425ACe6F6549D3188Ae9F0047Ea1A#eidasKey",
    "jws": "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9..xSfnO21PsmCPgLkE34wZQ5mLOcRmb4NPiBSKw3EULCBZiVSWrR_VhddwMuyydBwkimA04f2NJQofD3gF3t5tuAE"
  }
}
*/

const providerConfig = {
  rpcUrl: "https://api.intebsi.xyz/blockchain/besu",
};

// getResolver will return an object with a key/value pair of { "ebsi": resolver } where resolver is a function used by the generic DID resolver.
const ebsiDidResolver = getResolver(providerConfig);
const didResolver = new Resolver(ebsiDidResolver);

const options = {
  tirUrl: "https://api.test.intebsi.xyz/trusted-issuers-registry/v2/issuers",
  resolver: "https://api.test.intebsi.xyz/did-registry/v2/identifiers",
};

console.log("VC VALIDATION")
validateVerifiableCredential(vc, options); // should not throw
