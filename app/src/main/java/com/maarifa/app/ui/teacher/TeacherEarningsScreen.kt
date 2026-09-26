package com.maarifa.app.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maarifa.app.data.model.Payout
import com.maarifa.app.ui.common.EmptyState
import com.maarifa.app.ui.common.ErrorState
import com.maarifa.app.ui.common.LoadingState
import com.maarifa.app.ui.common.StatusPill
import org.koin.androidx.compose.koinViewModel

@Composable
fun TeacherEarningsScreen(
    viewModel: TeacherEarningsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> {
            LoadingState()
        }
        state.errorMessage != null -> {
            ErrorState(message = state.errorMessage ?: "Hitilafu imetokea")
        }
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Mapato Yangu",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E7F55))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Jumla ya Mapato (Total Earned)",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "TZS ${state.teacher?.totalEarnings ?: 0.0}",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Historia ya Malipo (Payouts)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (state.payouts.isEmpty()) {
                    EmptyState(message = "Hujafanya malipo yoyote bado.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.payouts) { payout ->
                            PayoutRow(payout = payout)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PayoutRow(payout: Payout) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TZS ${payout.amount}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = payout.provider.ifEmpty { payout.paymentMethod },
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            val (bgColor, textColor) = when (payout.status.uppercase()) {
                "COMPLETED", "PAID" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
                "PENDING" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
                else -> Color(0xFFFFEBEE) to Color(0xFFC62828)
            }

            StatusPill(
                text = payout.status,
                containerColor = bgColor,
                contentColor = textColor
            )
        }
    }
}
