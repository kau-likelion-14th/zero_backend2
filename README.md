# Zero_Backend2

## Local environment

Copy `.env.example` to `.env` and fill in the database, Kakao, JWT, and AWS S3 values.
The `.env` file is intentionally ignored by Git and must never be committed.

## Profile image API

- `PUT /api/profile` accepts a multipart field named `image`, uploads a normalized PNG to S3, and updates the logged-in user's profile image.
- `DELETE /api/profile` deletes the logged-in user's profile image from S3 and clears the stored image URL and key.
- Both endpoints identify the user from the access-token JWT; they do not accept `userId` from the client.
