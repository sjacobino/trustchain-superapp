# EBSI Libraries setup

First install *nodejs* + *npm* (platform dependent).

1. Run `npm init -y` in the application directory to create *package.json*
2. Install TypeScript

`npm install typescript`

3. Run `npx tsc --init` in the application directory to init TypeScript
4. Install EBSI libraries

e.g. `npm i @cef-ebsi/verifiable-credential`

If package.json already exists and contains the necessary packages, simply run `npm install`.

Use `ts-node file.ts` to run a file.
