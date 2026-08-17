package com.maarifa.app.ui.student

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maarifa.app.data.model.PaymentChannel
import com.maarifa.app.data.model.PlanType
import com.maarifa.app.di.SimpleViewModelFactory
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.domain.SubscriptionRules
import com.maarifa.app.ui.common.GradientButton
import com.maarifa.app.util.DateUtils

@Composable
fun SubscriptionScreen() {
    val container = maarifaContainer()
    val vm: SubscriptionViewModel = viewModel(
        factory = SimpleViewModelFactory { SubscriptionViewModel(container.subscriptionRepository, container.paymentRepository, container.authRepository) }
    )
    val state by vm.state.collectAsState()

    var selectedPlan by remember { mutableStateOf(PlanType.MONTHLY) }
    var channel by remember { mutableStateOf(PaymentChannel.MOBILE_MONEY) }
    var payerInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Subscription", style = MaterialTheme.typography.headlineMedium)

        state.subscription?.let { sub ->
            val active = SubscriptionRules.isActive(sub)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(if (active) "Active plan: ${sub.planType}" else "No active plan", style = MaterialTheme.typography.titleMedium)
                    if (active) Text("Renews / expires ${DateUtils.formatDisplay(sub.endDate)} (${SubscriptionRules.daysRemaining(sub)} days left)")
                    else if (sub.status == com.maarifa.app.data.model.SubscriptionStatus.PENDING_PAYMENT.name) Text("Payment pending verification…")
                }
            }
        }

        Text("Choose a plan", style = MaterialTheme.typography.titleMedium)
        PlanType.entries.forEach { plan ->
            val isSelected = selectedPlan == plan
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(14.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 0.dp else 0.dp),
                onClick = { selectedPlan = plan }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("${plan.label} — ${plan.amountTzs} TZS", style = MaterialTheme.typography.titleMedium)
                    Text("${plan.durationDays} days access", style = MaterialTheme.typography.bodyMedium)
                    if (isSelected) Text("Selected", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Text("Payment channel", style = MaterialTheme.typography.titleMedium)
        androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = channel == PaymentChannel.MOBILE_MONEY, onClick = { channel = PaymentChannel.MOBILE_MONEY }, label = { Text("Mobile Money") })
            FilterChip(selected = channel == PaymentChannel.BANK, onClick = { channel = PaymentChannel.BANK }, label = { Text("Bank") })
        }

        OutlinedTextField(
            value = payerInput,
            onValueChange = { payerInput = it },
            label = { Text(if (channel == PaymentChannel.MOBILE_MONEY) "Mobile money number" else "Bank account number") },
            modifier = Modifier.fillMaxWidth()
        )

        GradientButton(
            text = "Pay ${selectedPlan.amountTzs} TZS",
            onClick = { vm.subscribe(selectedPlan, channel, payerInput) },
            enabled = payerInput.isNotBlank() && !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        )

        state.statusMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (state.isSubmitting) CircularProgressIndicator()

        Text(
            "Payments are verified on our server before your plan activates — this can take a moment after you complete the prompt on your phone.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
