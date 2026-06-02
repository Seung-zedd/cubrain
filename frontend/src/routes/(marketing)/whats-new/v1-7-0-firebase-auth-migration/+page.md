---
layout: changelog
title: "Infrastructure Upgrade: Migration to Firebase Auth"
description: "Transitioned Cubrain's authentication system to Firebase Auth for faster logins, enhanced reliability, and robust social sign-in integration."
date: "2026-05-28"
version: "v1.7.0"
type: "Major Infrastructure"
---

## 🔒 Authentication Provider Migration

We have successfully completed a major infrastructure migration of our authentication backend from Supabase to **Firebase Auth**. This update provides a more reliable and responsive sign-in experience using Google Social Login.

### 🛡️ What actions were taken?

- **Firebase Google Sign-In**: Integrated native Google authentication using the Firebase Web Client SDK, providing standard OAuth login popups and seamless token handshakes.
- **Backend Token Validation**: Updated our Spring Boot backend token filters (`FirebaseUserSyncFilter`) to verify Firebase ID tokens using official OIDC public key certificates dynamically.
- **Seamless User Sync**: Standardized user mapping between Firebase and our core PostgreSQL database. Existing users are matched by their verified Google email address, meaning all decks, card progress, and billing tiers are preserved perfectly.
- **Secure Account Deletion**: Leveraged the Firebase Identity Toolkit REST APIs to enable secure, server-side account deletions.
- **Legacy Cleanup**: Completely removed all Supabase SDK libraries (`@supabase/supabase-js`) and configuration parameters from both the frontend and backend.

### ⚙️ Action Required for Users

Because we have retired the Supabase auth provider, **all existing user sessions have been logged out**. To resume using Cubrain:

1. **Sign In Again**: Simply click the "Sign In" button and authenticate using your Google Account.
2. **Account Linking**: If you already had an active account, signing in with the same Google email address will seamlessly restore all your previous decks and profile data.
