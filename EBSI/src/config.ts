//import { DIDResolver, Resolver } from "did-resolver";
import { getResolver } from "@cef-ebsi/ebsi-did-resolver";
import {Resolver} from "@cef-ebsi/ebsi-did-resolver/node_modules/did-resolver"

export const DID_RESOLVER = "https://api.preprod.ebsi.eu/did-registry/v2/identifiers"
export const TRUSTED_ISSUERS_REGISTRY = "https://api.preprod.ebsi.eu/trusted-issuers-registry/v2/issuers"

// You must set the address of the DID Registry to be used
const resolverConfig = {
  registry: DID_RESOLVER,
};

// getResolver will return an object with a key/value pair of { "ebsi": resolver } where resolver is a function used by the generic did resolver.
const ebsiDidResolver = getResolver(resolverConfig);
//const resolver: DIDResolver = ebsiDidResolver.ebsi
export const didResolver = new Resolver({ebsi: ebsiDidResolver.ebsi});

/* didResolver
  .resolve("did:ebsi:znHeZWvhAK2FK2Dk1jXNe7m")
  .then((doc) => console.log(doc.didDocument?.verificationMethod));
 */