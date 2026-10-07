package com.cybernerd404.fairsplit.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybernerd404.fairsplit.domain.model.CategoryBreakdown
import com.cybernerd404.fairsplit.domain.model.ExpenseCategory
import kotlin.math.atan2

@Composable
fun EpicCategoryDonutRing(
    breakdowns: List<CategoryBreakdown>,
    totalSpent: Double,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var animationPlayed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 900)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SPENDING BY CATEGORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Category breakdown for this Epic",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${breakdowns.size} Categories",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Donut Canvas
        Box(
            modifier = Modifier
                .size(220.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(breakdowns) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val touchVec = offset - center
                            val dist = touchVec.getDistance()
                            var angle = (atan2(touchVec.y.toDouble(), touchVec.x.toDouble()) * (180.0 / kotlin.math.PI)).toFloat()
                            if (angle < 0) angle += 360f
                            val normalizedAngle = (angle + 90f) % 360f

                            val outerRadius = size.width / 2f
                            val innerRadius = outerRadius - 34.dp.toPx()

                            if (dist in innerRadius..outerRadius) {
                                var currentAngle = 0f
                                for (item in breakdowns) {
                                    val sweep = item.percentage * 360f
                                    if (normalizedAngle >= currentAngle && normalizedAngle <= currentAngle + sweep) {
                                        selectedCategory = if (selectedCategory == item.category) null else item.category
                                        break
                                    }
                                    currentAngle += sweep
                                }
                            }
                        }
                    }
            ) {
                val baseStroke = 24.dp.toPx()
                val selectedStroke = 30.dp.toPx()
                val strokePadding = selectedStroke / 2f
                val ringSize = Size(size.width - strokePadding * 2, size.height - strokePadding * 2)
                val topLeft = Offset(strokePadding, strokePadding)

                // Background track ring
                drawArc(
                    color = Color.Gray.copy(alpha = 0.1f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = ringSize,
                    style = Stroke(width = baseStroke)
                )

                var startAngle = -90f
                breakdowns.forEach { item ->
                    val sweep = (item.percentage * 360f) * animatedProgress
                    val isSelected = selectedCategory == item.category
                    val catColor = Color(item.category.colorHex)

                    drawArc(
                        color = if (selectedCategory == null || isSelected) catColor else catColor.copy(alpha = 0.35f),
                        startAngle = startAngle,
                        sweepAngle = (sweep - 2.5f).coerceAtLeast(0f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = ringSize,
                        style = Stroke(
                            width = if (isSelected) selectedStroke else baseStroke,
                            cap = StrokeCap.Round
                        )
                    )
                    startAngle += sweep
                }
            }

            // Center Stat Readout
            val activeCat = breakdowns.firstOrNull { it.category == selectedCategory }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                if (activeCat != null) {
                    Text(
                        text = activeCat.category.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(activeCat.category.colorHex)
                    )
                    Text(
                        text = "$currencySymbol${activeCat.amount.toInt()}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${(activeCat.percentage * 100).toInt()}% of total",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "TOTAL SPENT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currencySymbol${totalSpent.toInt()}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap slice to inspect",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Category Filter Pills
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            breakdowns.forEach { item ->
                val isSelected = selectedCategory == item.category
                val catColor = Color(item.category.colorHex)

                Row(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) catColor.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) catColor else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            selectedCategory = if (selectedCategory == item.category) null else item.category
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(catColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${item.category.displayName} ${(item.percentage * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
