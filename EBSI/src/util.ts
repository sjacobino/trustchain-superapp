import * as didJWT from '@cef-ebsi/did-jwt';

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