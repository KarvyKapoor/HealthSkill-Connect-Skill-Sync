# Security notes

- BCrypt password hashing is applied when demo users are loaded and when public users register.
- Password hashes are not included in public profile maps.
- Public registration DTO has no role field; it always creates STUDENT accounts.
- JWT uses HMAC signing with a minimum 32-byte secret and expiry.
- Frontend access token stays in module memory only; a page refresh requires login again.
- Backend checks the role for dashboard and admin role mutation endpoints. Do not trust frontend route visibility.
- CORS is restricted to `FRONTEND_ORIGIN`.
- Input validation and generic centralized errors are provided. Never put real secrets in committed files.

This is a development MVP. Before production, review JWT claim/user revocation handling, rate limiting, brute-force protections, CSRF and secure cookie strategy, audit trails, dependency scanning, logging redaction, privacy requirements, and healthcare-specific compliance. The sample data and demo credentials are public by design.
