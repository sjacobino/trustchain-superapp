import * as didJWT from '@cef-ebsi/did-jwt';
import { createHash } from 'crypto'
const jsonld = require('jsonld');

export function sha256(data: string): string {
    const hash = createHash('sha256');
    hash.update(data);
    return hash.digest('hex')
}

export function epochTime(): number {
    return Math.round(Date.now() / 1000)
}

export function randomHex(max: number): string {
    return Math.floor(Math.random() * max).toString(16)
}

export function printJWT(JWT: any) {
    if (typeof JWT === 'object') {
        console.log(JSON.stringify(JWT, null, 2))
    } else {
        console.log(JSON.stringify(didJWT.decodeJWT(JWT), null, 2))
    }
}

export async function canonized(doc: {}) {
    return jsonld.canonize(doc, {
        algorithm: 'URDNA2015',
        format: 'application/n-quads'
      });
}

if (require.main === module){
    console.log(sha256("hello world"))
  }