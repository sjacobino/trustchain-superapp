// ONBOARDING SERVICE JWT
const OS_EU_JWT = "eyJhbGciOiJFUzI1NksiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjE2Mzk2NjI3MzcsImlhdCI6MTYzOTY2MTgzNywiaXNzIjoiZGlkOmVic2k6emNHdnFnWlRIQ3Rramd0Y0tSTDdIOGsiLCJvbmJvYXJkaW5nIjoiZXUtbG9naW4iLCJ2YWxpZGF0ZWRJbmZvIjp7InZhbGlkYXRlZFVzZXIiOnsiYXNzdXJhbmNlbGV2ZWwiOiIxMCIsImF1dGhlbnRpY2F0aW9uZmFjdG9ycyI6eyIkIjp7Im51bWJlciI6IjEifSwibW9uaWtlciI6InNoYXJpZi5qYWNvYmlub0BydmlnLm5sIn0sImF1dGhlbnRpY2F0aW9ubGV2ZWwiOiJCQVNJQyIsImRvbWFpbiI6ImV4dGVybmFsIiwiZG9tYWludXNlcm5hbWUiOiJuMDA3YWRqZSIsImVtYWlsIjoic2hhcmlmLmphY29iaW5vQHJ2aWcubmwiLCJlbXBsb3llZXR5cGUiOiJuIiwiZmlyc3RuYW1lIjoiU2hhcmlmIiwiZ3JvdXBzIjp7IiQiOnsibnVtYmVyIjoiMCJ9fSwibGFzdG5hbWUiOiJKYWNvYmlubyIsImxvY2FsZSI6ImVuIiwibG9naW5kYXRlIjoiMjAyMS0xMi0xNlQxNDozNzoxNC43ODErMDE6MDAiLCJzc28iOiJmYWxzZSIsInN0cmVuZ3RocyI6eyIkIjp7Im51bWJlciI6IjEifSwic3RyZW5ndGgiOiJTVFJPTkcifSwidGVsZXdvcmtpbmdwcmlvcml0eSI6ImZhbHNlIiwidGlja2V0dHlwZSI6IlNFUlZJQ0UiLCJ1aWQiOiJuMDA3YWRqZSIsInVzZXIiOiJuMDA3YWRqZSJ9fX0.vFFVFxBoTSYgMp9Xe0NWn-YcTGpdcV18gTxxP8Dqd6NroiWyNy54lzQtimGde8-dGhvcWf26MEdAtBYVxBJwgg"
const OS_EU_JWT_JSON = [{
    "alg": "ES256K",
    "typ": "JWT"
    },
    {
    "exp": 1639662737,
    "iat": 1639661837,
    "iss": "did:ebsi:zcGvqgZTHCtkjgtcKRL7H8k",
    "onboarding": "eu-login",
    "validatedInfo": {
        "validatedUser": {
        "assurancelevel": "10",
        "authenticationfactors": {
            "$": {
            "number": "1"
            },
            "moniker": "sharif.jacobino@rvig.nl"
        },
        "authenticationlevel": "BASIC",
        "domain": "external",
        "domainusername": "n007adje",
        "email": "sharif.jacobino@rvig.nl",
        "employeetype": "n",
        "firstname": "Sharif",
        "groups": {
            "$": {
            "number": "0"
            }
        },
        "lastname": "Jacobino",
        "locale": "en",
        "logindate": "2021-12-16T14:37:14.781+01:00",
        "sso": "false",
        "strengths": {
            "$": {
            "number": "1"
            },
            "strength": "STRONG"
        },
        "teleworkingpriority": "false",
        "tickettype": "SERVICE",
        "uid": "n007adje",
        "user": "n007adje"
        }
    }
}]

const OS_CAPTCHA_JWT = "eyJhbGciOiJFUzI1NksiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjE2Mzk2NjI5NzcsImlhdCI6MTYzOTY2MjA3NywiaXNzIjoiZGlkOmVic2k6emNHdnFnWlRIQ3Rramd0Y0tSTDdIOGsiLCJvbmJvYXJkaW5nIjoicmVjYXB0Y2hhIiwidmFsaWRhdGVkSW5mbyI6eyJhY3Rpb24iOiJsb2dpbiIsImNoYWxsZW5nZV90cyI6IjIwMjEtMTItMTZUMTM6NDE6MTVaIiwiaG9zdG5hbWUiOiJhcHAucHJlcHJvZC5lYnNpLmV1Iiwic2NvcmUiOjAuOSwic3VjY2VzcyI6dHJ1ZX19.cNaKM14WtQ7Gr5ZABVOPBvOjEVklEql6DezdPjQ9tfhd0bForRhVdqCFpGbdtlcGqAV35XYdEB2Ozn14P_6wrA"
const OS_CAPTCHA_JWT_JSON = [{
    "alg": "ES256K",
    "typ": "JWT"
    },
    {
    "exp": 1639662977,
    "iat": 1639662077,
    "iss": "did:ebsi:zcGvqgZTHCtkjgtcKRL7H8k",
    "onboarding": "recaptcha",
    "validatedInfo": {
        "action": "login",
        "challenge_ts": "2021-12-16T13:41:15Z",
        "hostname": "app.preprod.ebsi.eu",
        "score": 0.9,
        "success": true
    }
}]