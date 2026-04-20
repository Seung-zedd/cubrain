---
layout: changelog
title: "Security Infrastructure Upgrade: Key Rotation"
description: "A proactive security measure following the April 2026 Vercel incident. All internal API keys have been rotated to ensure maximum user data protection."
date: "2026-04-20"
version: "v1.6.0"
type: "Emergency"
---

## 🚑 Emergency Security Update

In response to the industry-wide security incident reported by Vercel in April 2026, we have successfully completed a comprehensive rotation of all sensitive environment variables and API keys for the Cubrain platform.

### 🛡️ What actions were taken?

- **Full Key Rotation**: We have revoked and regenerated all Supabase API keys, JWT secrets, and third-party integration tokens.
- **Modernized Auth**: Transitioned our backend security to use OIDC (OpenID Connect) discovery with asymmetric ECC (P-256) signing keys, significantly increasing our resistance to supply chain vulnerabilities.
- **Zero-Trust Hardening**: Environment variables on Vercel have been migrated to the new "Sensitive" storage layer.

### ⚙️ Action Required for Users

To ensure your session is secure and synchronized with our new infrastructure, please perform the following steps:

1. **Re-login**: If you are currently logged in, please sign out and sign back into Cubrain. This will issue a fresh, secure session token.
2. **Refresh Google Connection**: If you use Google Login, we recommend disconnecting Cubrain from your Google Account and then re-signing in from our app to establish a fresh, secure bond.
   - **[Manage & Disconnect Google Permissions](https://myaccount.google.com/permissions)**

### 🚀 Why This Matters?

While we have no evidence of any data breach within Cubrain specifically, we believe in **Total Transparency and Proactive Security**. By rotating our keys immediately, we ensure that even the theoretical possibility of unauthorized access is eliminated, keeping your study data and personal information safe under the highest modern standards.

### 💅 Minor UX Polish

- **Promotional Banner**: Fixed an issue where the "Early Bird" discount counter appeared unauthorized for guest users due to strictly applied security filters.
