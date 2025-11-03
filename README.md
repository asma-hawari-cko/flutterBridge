# 🧩 Flutter Bridge for Checkout.com Flow & Remeber Me 

**Last updated:** October 15, 2025  

This project provides a **Flutter bridge** for integrating **Checkout.com’s Flow SDK** into mobile applications.  
It enables seamless payment flows including **Apple Pay**, **Google Pay**, and other local payment methods directly in your Flutter app.

---

## 🚀 Overview

Flow allows you to set up both **client-side** and **server-side** integrations to enable secure payments in your mobile app.

### 🔁 Flow Payment Process

1. The customer loads the checkout screen in your app.  
2. Your backend securely creates a **Payment Session**.  
3. The client (mobile app) uses the session data to **initialize Flow for mobile**.  
4. Flow renders available payment methods and collects any required customer information.  
5. When the customer taps **Pay**, Flow handles the payment and any required authentication (e.g., 3D Secure).  
6. If the customer chooses to save their card, Flow automatically creates a **Remember Me** profile.  
7. Your system receives a **webhook notification** for the payment status.

---

## 🧠 Prerequisites

Before starting:

- A **Checkout.com test account**
- A **Public key** and **Secret key** from your [Checkout Dashboard](https://hub.checkout.com/)
  - **Public key scopes:** `payment-sessions:pay`, `vault-tokenization`
  - **Secret key scopes:** `payment-sessions`
- A **webhook receiver** to capture payment event updates

---

## 🧾 Create a Payment Session (Server-side)

When the customer is ready to pay, make a **server-side** API call to create a Payment Session.

> ⚠️ Never embed your secret key in client-side code.

**Endpoint:**
POST https://api.checkout.com/payment-sessions


**Example request:**

{
  "amount": 1000,
  "currency": "GBP",
  "reference": "ORD-123A",
  "billing": {
    "address": {
      "country": "GB"
    }
  },
  "customer": {
    "name": "Jia Tsang",
    "email": "jia.tsang@example.com"
  },
  "success_url": "https://example.com/payments/success",
  "failure_url": "https://example.com/payments/failure"
}



**Example response:**

{
  "id": "ps_2Un6I6lRpIAiIEwQIyxWVnV9CqQ",
  "payment_session_secret": "pss_e21237b4-214a-4e35-a35d-f33wkwo158f6"
}

--- 
## ⚙️ Initialize and Render Flow (Client-side)
## 🧩 Android setup
Project-level build.gradle
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://maven.fpregistry.io/releases") }
}

App-level build.gradle
dependencies {
    implementation 'com.checkout:checkout-android-components:$latest_version'
}

Kotlin example
val configuration = CheckoutComponentConfiguration(
    context = context,
    paymentSession = PaymentSessionResponse(
        id = paymentSessionID,
        paymentSessionToken = paymentSessionToken,
        paymentSessionSecret = paymentSessionSecret,
    ),
    publicKey = "YOUR_PUBLIC_KEY",
    environment = Environment.SANDBOX,
)

CoroutineScope(Dispatchers.IO).launch {
    try {
        val checkoutComponents = CheckoutComponentsFactory(config = configuration).create()
    } catch (checkoutError: CheckoutError) {
        handleError(checkoutError)
    }
}

--- 

## 💳 Accept Wallet Payments
#🍎 Apple Pay

To add Apple Pay support:

Create an Apple Merchant ID and a Payment Processing Certificate in the Apple Developer Portal
.

Generate a CSR from Checkout.com:

curl --location --request POST 'https://api.checkout.com/applepay/signing-requests' \
--header 'Authorization: Bearer pk_xxx' \
| jq -r '.content' > ~/Desktop/cko.csr


Upload the CSR to Apple, download the resulting certificate, and upload it back to Checkout:

curl --location --request POST 'https://api.checkout.com/applepay/certificates' \
--header 'Authorization: Bearer pk_xxx' \
--header 'Content-Type: application/json' \
--data-raw '{
  "content": "'"$(openssl x509 -inform der -in apple_pay.cer | base64)"'"
}'


Add Apple Pay capability in Xcode and select your merchant ID.

Render the Apple Pay button:

try checkoutComponentsSDK
  .create(
    .flow(options: [
      .applePay(merchantIdentifier: "YOUR_MERCHANT_ID_HERE")
    ])
  )

#🤖 Google Pay

To add Google Pay support in Android:

Add the following to your AndroidManifest.xml inside <application>:

<meta-data
    android:name="com.google.android.gms.wallet.api.enabled"
    android:value="true" />


Set up your GooglePayFlowCoordinator:

val coordinator = GooglePayFlowCoordinator(
    context = activity,
    handleActivityResult = { resultCode, data ->
        viewModel.handleActivityResult(resultCode, data)
    }
)

val flowCoordinators = mapOf(PaymentMethodName.GooglePay to coordinator)


Initialize and render the Google Pay component:

val config = CheckoutComponentConfiguration(
    context = context,
    paymentSession = paymentSession,
    publicKey = publicKey,
    environment = environment,
    flowCoordinators = flowCoordinators,
)

val checkoutComponents = CheckoutComponentsFactory(config).create()
val googlePayComponent = checkoutComponents.create(PaymentMethodName.GooglePay)
googlePayComponent.Render()

## 🧾 Handling Payment Responses

Payments may be synchronous (no redirect) or asynchronous (redirect required).

Use the onSuccess and onError callbacks to handle client-side payment status.

⚠️ Always wait for the webhook callback before confirming an order.

Example:
val config = CheckoutComponentConfiguration(
  paymentSession = paymentSession,
  publicKey = publicKey,
  environment = environment,
  componentCallback = ComponentCallback(
    onSuccess = { _, result -> /* handle success */ },
    onError = { _, error -> /* handle error */ }
  )
)


You can also verify the payment using the API:

GET https://api.checkout.com/payments/{cko-payment-id}

--- 

## 🧪 Testing

Use Checkout.com test cards to simulate different payment outcomes.
You can view payment statuses in your Dashboard under:
Payments → Processing → All payments

## ⚠️ Important Note

In this Flutter bridge implementation, the Payment Session is currently hardcoded inside
FlowPlatformView.

🔧 To test your integration:

Use Postman to create a Payment Session via the Checkout.com API.

Copy the returned payment_session_secret and paste it into the FlowPlatformView file.

Replace the publicKey with your own Checkout.com public key.

## 📚 Resources

Checkout.com Flow SDK Docs

Flutter Documentation

Apple Pay Setup Guide

Google Pay API Docs

## 🧑‍💻 Author

Asma Hawari
Flutter Bridge for Checkout.com Flow SDK
📧 asma.hawari@checkout.com
