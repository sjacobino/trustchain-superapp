import EbsiWallet from "@cef-ebsi/wallet-lib"
import { sha256, canonized } from "./util"
import { generateDidDocument } from "./wallet_tools"
import { myDid, myKeyPair, myWallet } from "./my_wallet"

// UNFINISHED
export async function insertDidDocument(didDocument: {}, wallet: EbsiWallet) {
    const hash = sha256(JSON.stringify(didDocument))

    const canDoc = await canonized(didDocument)
    const canDoc64 = Buffer.from(canDoc) //.toString("base64")
    
    const body = {
        jsonrpc: "2.0",
        method: "insertDidDocument",
        id: 1,
        params: [
            {
                from: "0x" + wallet.getEthereumAddress(),
                identifier: null,
                hashAlgorithmId: 0,
                hashValue: null,
                didVersionInfo: "0x" + canDoc64,
                timestampData: null,
                didVersionMetadata: null
            }
        ]
    }

    return body
}

if (require.main === module){
    const didDocument = generateDidDocument(myDid, myKeyPair)
    insertDidDocument(didDocument, myWallet).then((body) => {
        console.log(body)
    })
}