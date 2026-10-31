# Login test cases TC01-TC30: application changes

This is an implementation note for the existing Spring Boot E-Commerce application. No Selenium/TestNG test class was added.

## Updated files

- `src/main/java/com/example/ecommerce/service/AuthService.java`
  - Required username/email and password validation.
  - Email format validation when the login identifier contains `@`.
  - Username boundary validation: 3–50 characters, matching the current `USERS.USERNAME` column length.
  - Password validation: 8–72 characters to match the application's registration minimum and BCrypt's 72-byte input limit for ordinary ASCII test data.
  - Keeps BCrypt password verification and Spring Data repository lookups; no SQL is constructed from user input.
- `src/main/java/com/example/ecommerce/controller/ApiExceptionHandler.java`
  - Returns useful JSON `message` fields with the correct HTTP status for validation/authentication errors.
- `src/main/resources/static/index.html`
  - Password remains `type="password"`; adds autocomplete and accessibility attributes and enables app-specific validation messages.
- `src/main/resources/static/app.js`
  - Validates the login form, shows field-specific errors, disables the button during the request, and inserts messages using `textContent` so response text is not executed as HTML.
- `src/main/resources/application.properties`
  - Changes the server port from 8082 to 8083 because 8082 was reported as already in use. Oracle URL, schema, and credentials are otherwise unchanged.

## TC01–TC30 coverage notes

| Test case | Coverage in this update |
|---|---|
| TC01 Valid username/email and password | Supported by existing login endpoint and session creation. |
| TC02 Valid email login | Supported; email lookup is case-normalized. |
| TC03 Login button submits form | Supported; button submits the existing form. |
| TC04 Password masked | Supported by `type="password"`. |
| TC05 Registered username | Supported by repository lookup and password verification. |
| TC06 Username blank | Explicit validation message. |
| TC07 Password blank | Explicit validation message. |
| TC08 Both fields blank | Username-required message appears first; after correcting it, password-required message appears. |
| TC09 Incorrect password | Login rejected with invalid-credentials response. |
| TC10 Invalid username | Login rejected with invalid-credentials response. |
| TC11 Invalid username and password | Login rejected. |
| TC12 Username spaces only | Rejected as required/invalid username. |
| TC13 Password spaces only | Rejected as required password. |
| TC14 Invalid email format | Rejected when the identifier contains `@` but is not a valid email format. |
| TC15 Inactive account | Not supported by the current `USERS` entity/schema; no account-status field exists. |
| TC16 Locked account | Not supported as a persistent account state in the current schema. |
| TC17 Disabled account | Not supported by the current `USERS` entity/schema; no disabled flag exists. |
| TC18 Expired password | Not supported; no password-expiry field or reset flow exists. |
| TC19 SQL injection | Repository-derived queries are used; no raw SQL concatenation is used for login. |
| TC20 XSS payload | Login messages are inserted using `textContent`, not `innerHTML`. |
| TC21 Password not visible | Supported by `type="password"`. |
| TC22 Copy/paste password | Paste remains enabled to support password managers; no special paste restriction was added. |
| TC23 Multiple failed attempts | Account lockout/CAPTCHA is not implemented in this update. |
| TC24 Session after login | Supported by setting `userId` in the HTTP session after successful authentication. |
| TC25 Unauthorized dashboard URL | There is no separate protected dashboard route in this single-page app. The existing `/api/auth/me` endpoint returns 401 without a session. |
| TC26 Username minimum length | Backend/frontend validation uses 3 characters; a valid registered username of that length can log in. |
| TC27 Username below minimum | Rejected with a validation message. |
| TC28 Username maximum length | Backend accepts up to 50 characters, matching the current column. A registered username at that length can log in. |
| TC29 Username over maximum | Rejected with a validation message. |
| TC30 Password minimum length | Minimum accepted length is 8 characters; a correct password at that length can log in. |

## Important limitations

The supplied PDF does not specify numeric username/password limits, so this implementation uses the current database username column length (50) and the existing registration minimum (8). Account status, account lockout, and password expiry require additional persistence/schema and workflow work. They are explicitly not claimed as implemented. No database tables were dropped or altered.

## Running in Eclipse

1. Import the extracted folder as an **Existing Maven Project**.
2. Verify your Oracle listener/service is reachable using the existing JDBC URL in `application.properties`.
3. Run `EcommerceProjectApplication.java` as **Spring Boot App**.
4. Open `http://localhost:8083/`.

If port 8083 is also occupied, change `server.port` to another free port.
