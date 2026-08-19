package com.maarifa.app.ui.teacher

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maarifa.app.data.model.Payout
import com.maarifa.app.di.SimpleViewModelFactory
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.ui.common.EmptyState
import com.maarifa.app.ui.common.SectionHeader
import com.maarifa.app.ui.common.StatusPill
import com.maarifa.app.util.DateUtils

@Composable
fun TeacherEarningsScreen() {
    val container = maarifaContainer()
    val vm: TeacherEarningsViewModel = viewModel(
        factory = SimpleViewModelFactory {
            TeacherEarningsViewModel(
                container.teacherRepository,
                container.payoutRepository,
                container.authRepository
            )
        }
    )
    val state by vm.state.collectAsState()

    // Maarifa Brand Colors
    val primaryGreen = Color(0xFF1E7F55)
    val darkGreen = Color(0xFF1B5E20)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFA5D6A7))
    )
    val balanceCardGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF1E7F55), Color(0xFF2E7D32))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Earnings",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = darkGreen
            )

            // Balance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(balanceCardGradient)
                        .padding(20.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Current Balance",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${state.teacher?.earningsBalanceTzs ?: 0} TZS",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Calculated automatically each period from your share of verified student engagement.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.75f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            SectionHeader("Payout History")
            Spacer(modifier = Modifier.height(8.dp))

            if (state.payouts.isEmpty()) {
                EmptyState("No payouts generated yet.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.payouts, key = { it.payoutId }) { payout ->
                        PayoutRow(payout = payout)
                    }
                }
            }
        }
    }
}

@Composable
private fun PayoutRow(payout: Payout) {
    val primaryGreen = Color(0xFF1E7F55)
    val lightGreenBg = Color(0xFFE8F5E9)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(lightGreenBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = primaryGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = payout.period,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = DateUtils.formatDisplay(payout.createdAt),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                StatusPill(
                    text = payout.status.replace("_", " "),
                    containerColor = lightGreenBg,
                    contentColor = primaryGreen
                )
            }

            Text(
                text = "${payout.calculatedAmountTzs} TZS",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = primaryGreen
            )
        }
    }
}
