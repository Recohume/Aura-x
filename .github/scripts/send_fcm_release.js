#!/usr/bin/env node

/**
 * Sends a real-time FCM v1 release broadcast to all devices subscribed to topic 'releases'.
 * Uses native Node.js crypto (zero external npm dependencies required).
 */

const crypto = require("crypto");

async function main() {
  const serviceAccountJson = process.env.FIREBASE_SERVICE_ACCOUNT || process.env.FIREBASE_KEY;
  if (!serviceAccountJson || !serviceAccountJson.trim()) {
    console.log("::notice::FIREBASE_SERVICE_ACCOUNT secret not provided. Skipping FCM release push broadcast.");
    process.exit(0);
  }

  let serviceAccount;
  try {
    serviceAccount = JSON.parse(serviceAccountJson);
  } catch (err) {
    console.error("::warning::Failed to parse FIREBASE_SERVICE_ACCOUNT secret as JSON:", err.message);
    process.exit(0);
  }

  const clientEmail = serviceAccount.client_email;
  const privateKey = serviceAccount.private_key;
  const projectId = serviceAccount.project_id || "aura-1e2ea";

  if (!clientEmail || !privateKey) {
    console.error("::warning::Service account JSON missing client_email or private_key. Skipping FCM push.");
    process.exit(0);
  }

  const rawVersion = (process.env.RAW_VERSION || "1.4.6").trim().replace(/^v/i, "");
  const versionTag = (process.env.VERSION || `v${rawVersion}`).trim();

  console.log(`Generating Google OAuth2 token for service account: ${clientEmail}`);

  // Create JWT for Google OAuth2
  const now = Math.floor(Date.now() / 1000);
  const header = Buffer.from(JSON.stringify({ alg: "RS256", typ: "JWT" })).toString("base64url");
  const claims = Buffer.from(JSON.stringify({
    iss: clientEmail,
    scope: "https://www.googleapis.com/auth/firebase.messaging",
    aud: "https://oauth2.googleapis.com/token",
    exp: now + 3600,
    iat: now
  })).toString("base64url");

  const signer = crypto.createSign("RSA-SHA256");
  signer.update(`${header}.${claims}`);
  const signature = signer.sign(privateKey, "base64url");
  const jwt = `${header}.${claims}.${signature}`;

  // Request OAuth2 access token from Google
  const tokenResponse = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion: jwt
    })
  });

  if (!tokenResponse.ok) {
    const errorBody = await tokenResponse.text();
    console.error(`::warning::Failed to obtain Google OAuth2 access token (HTTP ${tokenResponse.status}): ${errorBody}`);
    process.exit(0);
  }

  const tokenData = await tokenResponse.json();
  const accessToken = tokenData.access_token;

  console.log(`Successfully acquired Google OAuth token. Broadcasting release ${versionTag} to topic 'releases'...`);

  // Send high-priority data-only payload so AuraFirebaseMessagingService is always invoked
  // (even in background/doze) to render the custom monochrome smallIcon, right-side circular
  // mascot largeIcon, and direct PendingIntent to MainActivity's App Update Modal.
  const fcmPayload = {
    message: {
      topic: "releases",
      data: {
        type: "release",
        version: rawVersion,
        title: `Aura ${versionTag} is available!`,
        body: "A new version of Aura is ready. Tap to download and install!"
      },
      android: {
        priority: "high"
      }
    }
  };

  const fcmResponse = await fetch(`https://fcm.googleapis.com/v1/projects/${projectId}/messages:send`, {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${accessToken}`,
      "Content-Type": "application/json"
    },
    body: JSON.stringify(fcmPayload)
  });

  const fcmResult = await fcmResponse.json();
  if (fcmResponse.ok) {
    console.log(`Successfully dispatched FCM release push notification for ${versionTag}! Name:`, fcmResult.name);
  } else {
    console.error(`::warning::FCM dispatch error (HTTP ${fcmResponse.status}):`, fcmResult);
  }
}

main().catch(err => {
  console.error("::warning::Unexpected error dispatching FCM release broadcast:", err);
  process.exit(0);
});
