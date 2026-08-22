package com.maarifa.app.ui.student

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.maarifa.app.di.SimpleViewModelFactory
import com.maarifa.app.di.maarifaContainer
import com.maarifa.app.domain.AccessControlUseCase
import com.maarifa.app.ui.common.ErrorState
import com.maarifa.app.ui.common.GradientButton
import com.maarifa.app.ui.common.LoadingState

@Composable
fun MaterialDetailScreen(materialId: String, onOpenReader: (String) -> Unit, onNeedsSubscription: () -> Unit) {
    val container = maarifaContainer()
    val vm: MaterialDetailViewModel = viewModel(
        factory = SimpleViewModelFactory {
            MaterialDetailViewModel(
                materialId,
                container.materialRepository,
                container.subscriptionRepository,
                container.downloadRepository,
                container.authRepository
            )
        }
    )
    val state by vm.state.collectAsState()
    var wantsToRead by remember { mutableStateOf(false) }

    // Colors
    val primaryGreen = Color(0xFF1E7F55)
    val lightGreenBg = Color(0xFFE8F5E9)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFA5D6A7))
    )

    // Once a just-in-time download finishes, jump straight into the reader.
    LaunchedEffect(state.isDownloaded, state.downloadProgress) {
        if (state.isDownloaded && state.downloadProgress == null && wantsToRead) {
            wantsToRead = false
            onOpenReader(materialId)
        }
    }

    when {
        state.isLoading -> LoadingState()
        state.errorMessage != null && state.material == null -> ErrorState(state.errorMessage!!)
        state.material != null -> {
            val material = state.material!!

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundGradient)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = material.title,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(lightGreenBg)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = material.subject,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryGreen
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF0F0F0))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = material.form.replace("_", " "),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.DarkGray
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BookOnline,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = material.topic,
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Content & Description Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Overview",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )

                            Text(
                                text = material.description,
                                fontSize = 14.sp,
                                color = Color(0xFF424242),
                                lineHeight = 20.sp
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFAFAFA))
                                    .padding(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(lightGreenBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = primaryGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Prepared By",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = material.teacherName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }

                    // Actions Section Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            when (state.accessDecision) {
                                AccessControlUseCase.AccessDecision.Allowed -> {
                                    if (state.downloadProgress != null) {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = "Downloading... ${(state.downloadProgress!! * 100).toInt()}%",
                                                fontSize = 12.sp,
                                                color = primaryGreen,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            LinearProgressIndicator(
                                                progress = { state.downloadProgress!! },
                                                color = primaryGreen,
                                                trackColor = lightGreenBg,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                                    .clip(CircleShape)
                                            )
                                        }
                                    }

                                    GradientButton(
                                        text = if (state.isDownloaded) "Read Now" else "Download & Read",
                                        onClick = {
                                            wantsToRead = true
                                            if (state.isDownloaded) onOpenReader(materialId) else vm.download()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                    )
                                }

                                AccessControlUseCase.AccessDecision.SubscriptionRequired,
                                AccessControlUseCase.AccessDecision.SubscriptionExpired -> {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = Color(0xFFD32F2F),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "A subscription plan is required to access this learning material.",
                                                color = Color(0xFFD32F2F),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    GradientButton(
                                        text = "View Subscription Plans",
                                        onClick = onNeedsSubscription,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                    )
                                }

                                AccessControlUseCase.AccessDecision.MaterialNotApproved -> {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "This material is currently under review and not available.",
                                            color = Color(0xFFE65100),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = vm::save,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "Save for later",
                                    tint = primaryGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Save for Later",
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
