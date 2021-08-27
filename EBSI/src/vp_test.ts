import { createPresentation } from "@cef-ebsi/verifiable-presentation";

const presentation = createPresentation({
  verifiableCredential: [
    {
      "@context": [
        "https://www.w3.org/2018/credentials/v1",
        "https://EBSI-WEBSITE.EU/schemas/vc/2019/v1#",
        "https://EBSI-WEBSITE.EU/schemas/eidas/2019/v1#",
      ],
      type: ["VerifiableCredential"],
      issuanceDate: "2020-11-23T00:00:00Z",
      credentialSubject: {
        name: "alice",
      },
      issuer: "did:ebsi:9rEQU8uHDJKomKaqMNors9NU7bkAvWiHWGB4dAD8PWPF",
      proof: {
        type: "EcdsaSecp256k1Signature2019",
        created: "2020-11-12T12:08:08.163Z",
        proofPurpose: "assertionMethod",
        verificationMethod:
          "did:ebsi:9rEQU8uHDJKomKaqMNors9NU7bkAvWiHWGB4dAD8PWPF#keys-1",
        jws: "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9..xSfnO21PsmCPgLkE34wZQ5mLOcRmb4NPiBSKw3EULCBZiVSWrR_VhddwMuyydBwkimA04f2NJQofD3gF3t5tuAE",
      },
    },
  ],
  holder: "did:ebsi:AaEkn73secFMUTSg4vTLkhX79kJE8oaAK748ToS8Ys9e",
});

console.log(presentation);

/*
  {
    "@context": ["https://www.w3.org/2018/credentials/v1"],
    type: "VerifiablePresentation",
    verifiableCredential: [
      {
        "@context": [
          "https://www.w3.org/2018/credentials/v1",
          "https://EBSI-WEBSITE.EU/schemas/vc/2019/v1#",
          "https://EBSI-WEBSITE.EU/schemas/eidas/2019/v1#",
        ],
        type: ["VerifiableCredential"],
        issuanceDate: "2020-11-23T00:00:00Z",
        credentialSubject: {
          name: "alice",
        },
        issuer: "did:ebsi:9rEQU8uHDJKomKaqMNors9NU7bkAvWiHWGB4dAD8PWPF",
        proof: {
          type: "EcdsaSecp256k1Signature2019",
          created: "2020-11-12T12:08:08.163Z",
          proofPurpose: "assertionMethod",
          verificationMethod:
            "did:ebsi:9rEQU8uHDJKomKaqMNors9NU7bkAvWiHWGB4dAD8PWPF#keys-1",
          jws:
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NkstUiJ9..xSfnO21PsmCPgLkE34wZQ5mLOcRmb4NPiBSKw3EULCBZiVSWrR_VhddwMuyydBwkimA04f2NJQofD3gF3t5tuAE",
        },
      },
    ],
    holder: "did:ebsi:AaEkn73secFMUTSg4vTLkhX79kJE8oaAK748ToS8Ys9e"
  }
*/