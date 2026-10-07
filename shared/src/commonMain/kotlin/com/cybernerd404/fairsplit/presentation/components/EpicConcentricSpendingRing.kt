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
import com.cybernerd404.fairsplit.domain.model.Member
import kotlin.math.atan2

@Composable
fun EpicConcentricSpendingRing(
    breakdowns: List<CategoryBreakdown>,
    members: List<Member>,
    totalSpent: Double,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var selectedMember by remember { mutableStateOf<Member?>(null) }
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DUAL-RING SPENDING BREAKDOWN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Inner: Categories • Outer: Members",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Concentric Donut Canvas
        Box(
            modifier = Modifier
                .size(240.dp)
                .padding(6.dp),
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

                            // Inner Ring: distance around 50..85 dp
                            val innerMin = 40.dp.toPx()
                            val innerMax = 85.dp.toPx()

                            if (dist in innerMin..innerMax) {
                                var currentAngle = 0f
                                for (item in breakdowns) {
                                    val sweep = item.percentage * 360f
                                    if (normalizedAngle >= currentAngle && normalizedAngle <= currentAngle + sweep) {
                                        selectedCategory = if (selectedCategory == item.category) null else item.category
                                        selectedMember = null
                                        break
                                    }
                                    currentAngle += sweep
                                }
                            }
                        }
                    }
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)

                // 1. Outer Ring: Member Contributions
                val outerStroke = 10.dp.toPx()
                val outerSelectedStroke = 14.dp.toPx()
                val outerSize = Size(size.width - outerSelectedStroke, size.height - outerSelectedStroke)
                val outerTopLeft = Offset(outerSelectedStroke / 2f, outerSelectedStroke / 2f)

                var outerStartAngle = -90f
                val totalMemberSpent = members.sumOf { it.totalSpent }.coerceAtLeast(1.0)
                members.forEach { member ->
                    val sweep = ((member.totalSpent / totalMemberSpent).toFloat() * 360f) * animatedProgress
                    val isSelected = selectedMember?.id == member.id
                    val mColor = Color(member.avatarColorHex)

                    drawArc(
                        color = if (selectedMember == null || isSelected) mColor else mColor.copy(alpha = 0.3f),
                        startAngle = outerStartAngle,
                        sweepAngle = (sweep - 3f).coerceAtLeast(0f),
                        useCenter = false,
                        topLeft = outerTopLeft,
                        size = outerSize,
                        style = Stroke(
                            width = if (isSelected) outerSelectedStroke else outerStroke,
                            cap = StrokeCap.Round
                        )
                    )
                    outerStartAngle += sweep
                }

                // 2. Inner Ring: Spending Categories
                val innerStroke = 20.dp.toPx()
                val innerSelectedStroke = 26.dp.toPx()
                val innerInset = 34.dp.toPx()
                val innerSize = Size(size.width - (innerInset * 2), size.height - (innerInset * 2))
                val innerTopLeft = Offset(innerInset, innerInset)

                // Inner track
                drawArc(
                    color = Color.Gray.copy(alpha = 0.12f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = innerTopLeft,
                    size = innerSize,
                    style = Stroke(width = innerStroke)
                )

                var innerStartAngle = -90f
                breakdowns.forEach { item ->
                    val sweep = (item.percentage * 360f) * animatedProgress
                    val isSelected = selectedCategory == item.category
                    val catColor = Color(item.category.colorHex)

                    drawArc(
                        color = if (selectedCategory == null || isSelected) catColor else catColor.copy(alpha = 0.35f),
                        startAngle = innerStartAngle,
                        sweepAngle = (sweep - 2f).coerceAtLeast(0f),
                        useCenter = false,
                        topLeft = innerTopLeft,
                        size = innerSize,
                        style = Stroke(
                            width = if (isSelected) innerSelectedStroke else innerStroke,
                            cap = StrokeCap.Round
                        )
                    )
                    innerStartAngle += sweep
                }
            }

            // Center Content
            val currentCat = breakdowns.firstOrNull { it.category == selectedCategory }
            val currentM = selectedMember

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                if (currentCat != null) {
                    Text(
                        text = currentCat.category.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(currentCat.category.colorHex)
                    )
                    Text(
                        text = "$currencySymbol${currentCat.amount.toInt()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${(currentCat.percentage * 100).toInt()}% of total",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (currentM != null) {
                    Text(
                        text = currentM.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(currentM.avatarColorHex)
                    )
                    Text(
                        text = "$currencySymbol${currentM.totalSpent.toInt()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Contributed",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "$currencySymbol${totalSpent.toInt()}",
                        fontSize = 22.sp,
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

        Spacer(modifier = Modifier.height(14.dp))

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
                            selectedMember = null
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
