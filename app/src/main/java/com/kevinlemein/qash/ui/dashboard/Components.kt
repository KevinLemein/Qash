package com.kevinlemein.qash.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun IncomeExpensePieChart(
    income: Double,
    expense: Double,
    size: Dp = 100.dp,
    strokeWidth: Dp = 12.dp
) {
    val total = income + expense
    // Avoid division by zero
    val incomeSweep = if (total == 0.0) 0f else ((income / total) * 360f).toFloat()
    val expenseSweep = if (total == 0.0) 360f else ((expense / total) * 360f).toFloat()

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            // Draw Expense Arc (Red)
            drawArc(
                color = Color(0xFFD50000), // Red
                startAngle = -90f,
                sweepAngle = 360f, // Fill background with Red first
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
            )

            // Draw Income Arc (Green) on top
            drawArc(
                color = Color(0xFF00C853), // Green
                startAngle = -90f,
                sweepAngle = incomeSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}