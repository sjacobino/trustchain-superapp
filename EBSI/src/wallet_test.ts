import { EbsiWallet } from "@cef-ebsi/wallet-lib";
import { JsonRpcProvider } from "@ethersproject/providers";
import { BigNumber } from "@ethersproject/bignumber";

// Public Key pair generation
/*
const keyOptions = {
  format: "hex",
  keyType: "EC",
  keyCurve: "secp256k1"
}

const keyPair = EbsiWallet.generateKeyPair(keyOptions)
console.log(keyPair)
*/

// Create a new random EBSI DID
/*
const did = EbsiWallet.createDid();
console.log(did);
*/

// Instantiating a new instance of EBSI Wallet
//const privateKey = "0a9a229c18f1777949243bbe875b754b77fb9cb3612c8b5c37876888f54f9731";
//const wallet = new EbsiWallet(privateKey);

// Get wallet's public key (different formats)
//const publicKey = wallet.getPublicKey();
//console.log(publicKey)
/*
046a6457531aefa19d1840cca004f9ef1cf50d30c508d960a63fceedf336da829501b3c54c16b2d4f325c602e23f17da353ad0ccba6397f082be1e3eaab07cf3c1
*/

//const publicKeyPem = wallet.getPublicKey({ format: "pem" });
/*
-----BEGIN PUBLIC KEY-----
MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEamRXUxrvoZ0YQMygBPnvHPUNMMUI2WCm
P87t8zbagpUBs8VMFrLU8yXGAuI/F9o1OtDMumOX8IK+Hj6qsHzzwQ==
-----END PUBLIC KEY-----
*/

//const publicKeyJwk = wallet.getPublicKey({ format: "jwk" });
/*
{
  kty: "EC",
  crv: "secp256k1",
  x: "amRXUxrvoZ0YQMygBPnvHPUNMMUI2WCmP87t8zbagpU",
  y: "AbPFTBay1PMlxgLiPxfaNTrQzLpjl_CCvh4-qrB888E",
}
*/

// Get wallet's Ethereum address
//const ethereumAddress = wallet.getEthereumAddress();
// returns 0xA2aa80C25AebE7d3b9984ef45179b4F39737fFBa

// Prepare an Ethereum transaction
//const accessToken = ""; // This is an OAuth2 access token required by Ledger API, see @cef-ebsi/oauth2-auth library
/*
const provider = new JsonRpcProvider({
  url: "https://api.test.intebsi.xyz/ledger/v2/blockchains/besu",
  headers: {
    authorization: `Bearer ${accessToken}`,
  },
});
*/