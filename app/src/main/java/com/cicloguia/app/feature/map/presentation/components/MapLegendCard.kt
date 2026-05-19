package com.cicloguia.app.feature.map.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cicloguia.app.feature.map.presentation.model.CyclewayLegendUi

@Composable
fun MapLegendCard(
    legend: CyclewayLegendUi,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .animateContentSize(animationSpec = spring())
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        AnimatedContent(
            targetState = expanded,
            label = "MapLegendAnimation"
        ) { isExpanded ->
            if (isExpanded) {
                ExpandedLegend(legend = legend)
            } else {
                CollapsedLegend()
            }
        }
    }
}

@Composable
private fun CollapsedLegend() {
    Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Layers,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = "Leyenda",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ExpandedLegend(
    legend: CyclewayLegendUi
) {
    Column(
        modifier = Modifier
            .width(300.dp)
            .padding(
                start = 18.dp,
                top = 16.dp,
                end = 18.dp,
                bottom = 16.dp
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = "LEYENDA",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = "Contraer leyenda",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LegendItem(
            color = Color(0xFF00B37A),
            label = "Existente",
            description = "Ciclovías construidas",
            count = legend.existingCount.toString(),
            countContainerColor = Color(0xFFD8F5E6),
            countContentColor = Color(0xFF008F5D)
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 6.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        LegendItem(
            color = Color(0xFFFF7A00),
            label = "En proyecto",
            description = "Ciclovías planificadas",
            count = legend.plannedCount.toString(),
            countContainerColor = Color(0xFFFFE7CC),
            countContentColor = Color(0xFFE86A00),
            dashed = true
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 6.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        LegendItem(
            color = Color(0xFF1976D2),
            label = "En ejecución",
            description = "Ciclovías en construcción",
            count = legend.underConstructionCount.toString(),
            countContainerColor = Color(0xFFDCEEFF),
            countContentColor = Color(0xFF1976D2)
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    description: String,
    count: String,
    countContainerColor: Color,
    countContentColor: Color,
    dashed: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendLine(
            color = color,
            dashed = dashed
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = description,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            shape = CircleShape,
            color = countContainerColor
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                text = count,
                style = MaterialTheme.typography.labelLarge,
                color = countContentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LegendLine(
    color: Color,
    dashed: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (dashed) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}