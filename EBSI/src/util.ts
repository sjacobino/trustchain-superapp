export function epochTime(): number {
    return Math.round(Date.now() / 1000)
}

export function randomHex(max: number): string {
    return Math.floor(Math.random() * max).toString(16)
}