# Common API

Shared response model and status codes for all B2B Platform REST APIs.

## Build order

Install this module first so other services can resolve it:

```bash
cd common-api
mvn clean install
```

Then build and run any service (e.g. authentication-service, session-service, wallet-service, bet-service).

## Usage

- **APIResponse**: All APIs return `ResponseEntity<APIResponse>`. Success: `APIResponse.success(result)`. Error: `APIResponse.get(StatusCode.INVALID_REQUEST, message)`.
- **StatusCode**: Enum with numeric codes (e.g. `SUCCESS(0)`, `INVALID_CREDENTIALS(4024)`, `INSUFFICIENT_BALANCE(130)`). Client checks `response.getCode()` or `response.getStatus()`.
- **No exceptions**: Controllers and handlers return `APIResponse` with an error status instead of throwing.
