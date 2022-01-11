import { EbsiWallet, KeyPair } from '@cef-ebsi/wallet-lib';

export function generateKeyPair(): KeyPair {
    return EbsiWallet.generateKeyPair({format: 'hex', keyType: 'EC', keyCurve: 'secp256k1'})
}

export function generateDid(): string {
    return EbsiWallet.createDid()
}

export function generateDidDocument(did: string, keyPair: KeyPair) {
    return {
        '@context': 'https://w3id.org/did/v1',
        id: did,
        verificationMethod:[{
            id: did + '#keys-1',
            type:'Secp256k1VerificationKey2018',
            controller: did,
            publicKeyHex: keyPair.publicKey}],
        authentication:[
            did + '#keys-1'],
        assertionMethod:[
            did + '#keys-1']
    }
}


if (require.main === module){

}