export async function httpPost(url: string, body: string): Promise<any> {
    const response = await fetch(url, {
        method: 'POST',
        body: body,
        headers: {'Content-Type': 'application/json; charset=UTF-8'} });
      
      if (!response.ok) { /* Handle */ }
      
      // If you care about a response:
      if (response.body !== null) {
        // body is ReadableStream<Uint8Array>
        // parse as needed, e.g. reading directly, or
    
        //const asString = new TextDecoder("utf-8").decode(response.body);
        // and further:
        //const asJSON = JSON.parse(asString);  // implicitly 'any', make sure to verify type on runtime.
        return response.json()
      }

      return {}
}

if (require.main === module){
    httpPost("https://api.preprod.ebsi.eu/authorisation/v1/authentication-requests", JSON.stringify({
        "scope": "openid did_authn"
      })).then((response) => {
          console.log(response)
      })
}