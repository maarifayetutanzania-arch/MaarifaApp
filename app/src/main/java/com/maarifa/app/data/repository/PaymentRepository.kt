package com.maarifa.app.data.repository

import com.google.firebase.functions.FirebaseFunctions
import com.maarifa.app.data.model.PaymentChannel
import com.maarifa.app.data.model.PlanType
import com.maarifa.app.util.CloudFunctions
import com.maarifa.app.util.Resource
import kotlinx.coroutines.tasks.await

/**
 * PRD 8.5: "one central verified payment receiving flow for Tanzania... accept payments
 * from banks and mobile money channels through the selected payment integration layer."
 *
 * This class only calls the `initiatePayment` Cloud Function callable. The callable itself
 * (functions/src/payments.ts) contains a clearly marked stub where the actual bank/mobile
 * money gateway call goes — that integration, plus the gateway account/API keys, is the
 * one piece you add. Everything downstream (subscription activation, verification
 * bookkeeping, status logging) is already implemented.
 */
class PaymentRepository(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
) {
    data class InitiateResult(val subscriptionId: String, val providerReference: String?, val instructions: String?)

    suspend fun initiatePayment(
        userId: String,
        plan: PlanType,
        channel: PaymentChannel,
        payerAccountOrPhone: String
    ): Resource<InitiateResult> = try {
        val payload = hashMapOf(
            "userId" to userId,
            "planType" to plan.name,
            "amountTzs" to plan.amountTzs,
            "durationDays" to plan.durationDays,
            "channel" to channel.name,
            "payerAccountOrPhone" to payerAccountOrPhone
        )
        val response = functions.getHttpsCallable(CloudFunctions.INITIATE_PAYMENT).call(payload).await()
        @Suppress("UNCHECKED_CAST")
        val data = response.data as? Map<String, Any?> ?: emptyMap()
        Resource.Success(
            InitiateResult(
                subscriptionId = data["subscriptionId"] as? String ?: "",
                providerReference = data["providerReference"] as? String,
                instructions = data["instructions"] as? String
            )
        )
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Could not start payment. Please try again.", e)
    }
}
