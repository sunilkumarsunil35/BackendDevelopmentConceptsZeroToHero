# 13 · Spring Security and JWT

> **Phase:** Production-ready · **Week:** 7 · **Code:** [`security/`](../src/main/java/com/zerotohero/security) · **Tests:** [`SecurityIntegrationTest.java`](../src/test/java/com/zerotohero/security/SecurityIntegrationTest.java), [`BookControllerWebTest.java`](../src/test/java/com/zerotohero/book/BookControllerWebTest.java)

Security answers two questions on every request: who are you (authentication) and are you allowed
to do this (authorization)? Spring Security answers them with a chain of servlet filters. This
module builds stateless JWT authentication for the bookstore.

## Concepts

### Authentication vs authorization
**Level:** Beginner

Authentication proves identity (password, token, certificate) and produces an `Authentication`
stored in the `SecurityContext`. Authorization checks that identity's roles or permissions against
the request. Failing the first gives 401; failing the second gives 403.

### The security filter chain
**Level:** Intermediate · **Code:** [SecurityConfig.java](../src/main/java/com/zerotohero/security/SecurityConfig.java)

Every request passes through a `SecurityFilterChain` bean: CORS, CSRF, the bearer-token filter,
exception translation, then authorization. Configure it with the lambda DSL. Rules in
`authorizeHttpRequests` are checked in order and the first match wins, so put specific rules
before general ones and finish with `anyRequest().authenticated()`.

### Password storage
**Level:** Beginner · **Code:** [SecurityConfig.java](../src/main/java/com/zerotohero/security/SecurityConfig.java)

Never store passwords; store a slow, salted hash. `PasswordEncoderFactories.createDelegatingPasswordEncoder()`
uses bcrypt and prefixes hashes with `{bcrypt}`, so you can upgrade algorithms later. Load users
from your database by implementing `UserDetailsService`.

### JWT: structure and trade-offs
**Level:** Intermediate · **Code:** [TokenService.java](../src/main/java/com/zerotohero/security/TokenService.java)

A JWT is `header.payload.signature`, base64url-encoded. The payload holds claims (`sub`, `iss`,
`iat`, `exp`, and custom `roles`); it's signed, not encrypted, so never put secrets in it. Any
server with the key can verify it without a database lookup, which makes it stateless. The cost:
you can't easily revoke a token before it expires, so keep access tokens short-lived (minutes to an
hour) and use refresh tokens for long sessions.

### Issuing tokens
**Level:** Intermediate · **Code:** [AuthController.java](../src/main/java/com/zerotohero/security/AuthController.java)

`POST /api/auth/token` authenticates the username and password with the `AuthenticationManager`
and signs a JWT with `JwtEncoder` (HS256 and a shared secret here). Return the same error for "no
such user" and "wrong password" so attackers can't discover usernames.

### Validating tokens: OAuth2 resource server
**Level:** Intermediate · **Code:** [SecurityConfig.java](../src/main/java/com/zerotohero/security/SecurityConfig.java)

`.oauth2ResourceServer(rs -> rs.jwt(...))` validates the signature, expiry and issuer on every
request, and a `JwtAuthenticationConverter` turns the `roles` claim into `ROLE_ADMIN` authorities.
`SecurityIntegrationTest.tamperedTokenIsRejected` changes one character and gets 401.

### Method security
**Level:** Intermediate · **Code:** [BookController.java](../src/main/java/com/zerotohero/book/BookController.java)

`@EnableMethodSecurity` enables `@PreAuthorize("hasRole('ADMIN')")` and expressions such as
`@PreAuthorize("#customer == authentication.name")`. Use URL rules for broad access and method
rules for fine-grained checks. Ownership checks (a user may only read their own orders) usually
belong in the query itself, as `OrderService.get` does.

### CSRF and CORS
**Level:** Intermediate

CSRF attacks abuse cookies the browser sends automatically. A stateless API that only accepts
`Authorization: Bearer` headers isn't exposed, so CSRF protection is disabled here. If you use
cookies for auth, keep it on. CORS (module 02) is configured with a `CorsConfigurationSource` bean.

### OAuth2 and OpenID Connect with an identity provider
**Level:** Advanced

In production, don't build login yourself: let Keycloak, Auth0, Okta, Cognito or Entra ID issue
tokens (signed with RS256 and a public JWKS endpoint). Your API stays a resource server with
`spring.security.oauth2.resourceserver.jwt.issuer-uri=...`, and the code above barely changes.
Learn the authorization code flow with PKCE for browser and mobile clients.

### Security checklist (OWASP API Top 10)
**Level:** Advanced

Broken object-level authorization (check ownership on every id), broken authentication, excessive
data exposure (DTOs), lack of rate limiting (module 23), mass assignment (DTOs again), security
misconfiguration (actuator exposure, verbose errors), injection (parameterized queries; JPA does
this), and secrets in code or logs.

## Practice

### Users in the database
**Level:** Intermediate

Replace the in-memory users with a `users` table (Flyway), a `User` entity and a
`UserDetailsService` that loads from it. Add `POST /api/auth/register` that stores a bcrypt hash.

### Refresh tokens
**Level:** Advanced

Issue a 15-minute access token and a 7-day refresh token stored in a table. Add
`POST /api/auth/refresh` that rotates the refresh token (old one becomes invalid) and
`POST /api/auth/logout` that revokes it.

### Use Keycloak
**Level:** Advanced

Run Keycloak in Docker, create a realm, client and roles, and switch the resource server to
`issuer-uri`. Map Keycloak's `realm_access.roles` claim to authorities.

## Interview questions

- What is the difference between 401 and 403 in Spring Security?
- How does a JWT get validated, and what can't it do that a server session can?
- Why is CSRF protection off here, and when must it be on?
- Where should you check that a user owns the resource they ask for?
- HS256 vs RS256: when would you use each?

## Further reading

- [Spring Security reference](https://docs.spring.io/spring-security/reference/)
- [Spring Security: OAuth 2.0 Resource Server JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [OWASP API Security Top 10](https://owasp.org/API-Security/)
- [jwt.io debugger](https://jwt.io)
