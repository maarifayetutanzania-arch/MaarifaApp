import * as functions from "firebase-functions/v2/https";
import { db, COLLECTIONS, FieldValue, PLAN_DURATIONS_DAYS, PLAN_AMOUNTS_TZS } from "./admin";
import { notifyUser } from "./notifications";

interface InitiatePaymentRequest {
  userId: string;
  planType: string;
  channel: "BANK" | "MOBILE_MONEY";
  payerAccountOrPhone: string;
}

/**
 * ============================================================================
 *  PAYMENT GATEWAY INTEGRATION POINT — this is the one piece you add.
 * ============================================================================
 * Replace the body of this function with a real call to your chosen Tanzanian
 * payment aggregator / bank API / mobile money API (e.g. Selcom, Azampay,
 * ClickPesa, a direct mobile-money USSD-push integration, etc).
 *
 * Contract this function must honour so the rest of the system keeps working
 * unchanged:
 *   - Return `providerReference`: a string you can later match against the
 *     provider's confirmation callback/webhook payload.
 *   - Return `instructions`: a short user-facing string (e.g. "Enter your PIN
 *     on the USSD prompt to confirm") shown immediately in the app.
 *   - Do NOT mark the subscription active here — only `paymentWebhook` below
 *     (called by the provider once money has actually moved) may do that.
 */
async function callPaymentGatewayStub(
  channel: string,
  payerAccountOrPhone: string,
  amountTzs: number,
  subscriptionId: string
): Promise<{ providerReference: string; instructions: string }> {
  console.log(
    `[STUB] Would charge ${amountTzs} TZS via ${channel} from ${payerAccountOrPhone} ` +
      `for subscription ${subscriptionId}. Wire your real gateway call here.`
  );
  return {
    providerReference: `STUB-${subscriptionId}`,
    instructions:
      "Payment gateway is not yet connected. Once configured, you'll receive a prompt on your phone/bank app to confirm.",
  };
}

/** PRD 8.5: single verified payment entry point. Client calls this; server owns every write. */
export const initiatePayment = functions.onCall(async (request) => {
  const data = request.data as InitiatePaymentRequest;
  if (!request.auth || request.auth.uid !== data.userId) {
    throw new functions.HttpsError("permission-denied", "You can only start a payment for your own account.");
  }
  const durationDays = PLAN_DURATIONS_DAYS[data.planType];
  const amountTzs = PLAN_AMOUNTS_TZS[data.planType]; // server-computed — never trust a client-sent amount
  if (!durationDays || !amountTzs) {
    throw new functions.HttpsError("invalid-argument", "Unknown plan type.");
  }
  if (!data.payerAccountOrPhone || data.payerAccountOrPhone.trim().length < 6) {
    throw new functions.HttpsError("invalid-argument", "A valid payer account/phone is required.");
  }

  // Renewal should stack on remaining time if the current plan hasn't expired yet.
  const existingSnap = await db
    .collection(COLLECTIONS.SUBSCRIPTIONS)
    .where("userId", "==", data.userId)
    .orderBy("createdAt", "desc")
    .limit(1)
    .get();
  const existing = existingSnap.docs[0]?.data();
  const now = new Date();
  const existingEnd = existing?.endDate?.toDate?.() as Date | undefined;
  const startDate = existingEnd && existingEnd > now ? existingEnd : now;

  const subscriptionRef = db.collection(COLLECTIONS.SUBSCRIPTIONS).doc();
  await subscriptionRef.set({
    subscriptionId: subscriptionRef.id,
    userId: data.userId,
    planType: data.planType,
    amountTzs,
    durationDays,
    provider: "", // filled in once your gateway integration names itself
    channel: data.channel,
    transactionId: "",
    startDate: null,
    endDate: null,
    status: "PENDING_PAYMENT",
    verifiedAt: null,
    createdAt: FieldValue.serverTimestamp(),
    _pendingStartDate: startDate.toISOString(), // internal bookkeeping used by the webhook below
  });

  const gatewayResult = await callPaymentGatewayStub(data.channel, data.payerAccountOrPhone, amountTzs, subscriptionRef.id);
  await subscriptionRef.update({ transactionId: gatewayResult.providerReference });

  return {
    subscriptionId: subscriptionRef.id,
    providerReference: gatewayResult.providerReference,
    instructions: gatewayResult.instructions,
  };
});

interface WebhookBody {
  subscriptionId: string;
  providerReference: string;
  success: boolean;
  failureReason?: string;
}

/**
 * Called by your payment gateway once a transaction is confirmed (success or
 * failure). This is the ONLY place a subscription is ever marked ACTIVE —
 * satisfying PRD 8.5/12 ("shall not activate a subscription based on user
 * claim alone" / "shall validate all payment events on the backend").
 *
 * Wire your gateway's webhook signature verification at the top of this
 * function before trusting req.body — left as a clearly marked TODO since
 * the verification scheme is specific to whichever gateway you choose.
 */
export const paymentWebhook = functions.onRequest(async (req, res) => {
  // TODO: verify req.headers signature against your gateway's shared secret before trusting req.body.
  const body = req.body as WebhookBody;
  if (!body?.subscriptionId) {
    res.status(400).send("Missing subscriptionId");
    return;
  }

  const ref = db.collection(COLLECTIONS.SUBSCRIPTIONS).doc(body.subscriptionId);
  const snap = await ref.get();
  if (!snap.exists) {
    res.status(404).send("Subscription not found");
    return;
  }
  const sub = snap.data()!;

  if (!body.success) {
    await ref.update({ status: "FAILED" });
    await notifyUser(sub.userId, "PAYMENT_ISSUE", "Payment failed", body.failureReason ?? "Your payment could not be verified. Please try again.");
    res.status(200).send("ok");
    return;
  }

  const startDate = sub._pendingStartDate ? new Date(sub._pendingStartDate) : new Date();
  const endDate = new Date(startDate.getTime() + sub.durationDays * 24 * 60 * 60 * 1000);

  await ref.update({
    status: "ACTIVE",
    startDate,
    endDate,
    verifiedAt: FieldValue.serverTimestamp(),
    provider: body.providerReference,
  });

  await notifyUser(
    sub.userId,
    "SUBSCRIPTION_EXPIRY",
    "Subscription activated",
    `Your ${sub.planType} plan is now active until ${endDate.toDateString()}.`
  );

  res.status(200).send("ok");
});
