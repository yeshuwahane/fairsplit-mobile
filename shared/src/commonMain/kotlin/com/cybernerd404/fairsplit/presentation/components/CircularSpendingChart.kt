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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cybernerd404.fairsplit.domain.model.CategoryBreakdown
import com.cybernerd404.fairsplit.domain.model.ExpenseCategory
import com.cybernerd404.fairsplit.theme.FairSplitColors
import kotlin.math.atan2

@Composable
fun CircularSpendingChart(
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
        animationSpec = tween(durationMillis = 1000)
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
        Text(
            text = "SPENDING BREAKDOWN",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Donut Ring Canvas
        Box(
            modifier = Modifier
                .size(220.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(breakdowns) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val touchVec = offset - center
                            var angle = (atan2(touchVec.y.toDouble(), touchVec.x.toDouble()) * (180.0 / kotlin.math.PI)).toFloat()
                            if (angle < 0) angle += 360f
                            // Adjust for startAngle (-90 deg = top)
                            val normalizedAngle = (angle + 90f) % 360f

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
            ) {
                val strokeWidth = 24.dp.toPx()
                val selectedStrokeWidth = 32.dp.toPx()
                val arcSize = Size(size.width - selectedStrokeWidth, size.height - selectedStrokeWidth)
                val topLeft = Offset(selectedStrokeWidth / 2f, selectedStrokeWidth / 2f)

                // Background track
                drawArc(
                    color = Color.Gray.copy(alpha = 0.12f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )

                var startAngle = -90f
                breakdowns.forEach { item ->
                    val sweepAngle = item.percentage * 360f * animatedProgress
                    val isSelected = selectedCategory == item.category
                    val itemColor = Color(item.category.colorHex)

                    drawArc(
                        color = if (selectedCategory == null || isSelected) itemColor else itemColor.copy(alpha = 0.35f),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle - 2f, // small gap between segments
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(
                            width = if (isSelected) selectedStrokeWidth else strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )
                    startAngle += sweepAngle
                }
            }

            // Center Content
            val currentSelectedItem = breakdowns.firstOrNull { it.category == selectedCategory }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                if (currentSelectedItem != null) {
                    Text(
                        text = currentSelectedItem.category.displayName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(currentSelectedItem.category.colorHex)
                    )
                    Text(
                        text = "$currencySymbol${currentSelectedItem.amount.toInt()}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${(currentSelectedItem.percentage * 100).toInt()}% of total",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "$currencySymbol${totalSpent.toInt()}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Total spent",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category breakdown pills
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
                        .padding(horizontal = 4.dp)
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
